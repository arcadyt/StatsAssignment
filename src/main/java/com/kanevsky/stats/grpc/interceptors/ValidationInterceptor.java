package com.kanevsky.stats.grpc.interceptors;

import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.exceptions.ValidationException;
import build.buf.validate.FieldPathElement;
import com.kanevsky.stats.grpc.ErrorCode;
import com.kanevsky.stats.grpc.IngestResponse;
import com.kanevsky.stats.grpc.ProcessingError;
import com.kanevsky.stats.grpc.StatsBatchRequest;
import com.kanevsky.stats.grpc.validations.ProtoValidationService;
import io.grpc.*;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ValidationInterceptor implements ServerInterceptor {

    private final ProtoValidationService validationService;

    @Autowired
    public ValidationInterceptor(ProtoValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(call, headers)) {

            @Override
            public void onMessage(ReqT message) {
                try {
                    ValidationResult result = validationService.validate((com.google.protobuf.Message)message);

                    if (!result.isSuccess()) {
                        Metadata trailers = new Metadata();

                        // Define metadata keys
                        Metadata.Key<String> errorsKey = Metadata.Key.of(
                                "validation-errors", Metadata.ASCII_STRING_MARSHALLER);

                        StringBuilder errorsJson = new StringBuilder("[");
                        boolean firstError = true;

                        for (var v : result.getViolations()) {
                            if (!firstError) {
                                errorsJson.append(",");
                            }
                            firstError = false;

                            // Get the field path using the tested approach
                            String fieldPath = v.toProto().getField().getElementsList().stream()
                                    .map(FieldPathElement::getFieldName)
                                    .collect(Collectors.joining("."));

                            // Get the error message using the tested approach
                            String errorMessage = v.toProto().getMessage();

                            // Build JSON-like error entry
                            errorsJson.append("{")
                                    .append("\"field\":\"").append(fieldPath).append("\",")
                                    .append("\"message\":\"").append(errorMessage).append("\"")
                                    .append("}");
                        }

                        errorsJson.append("]");

                        // Add error information to metadata
                        trailers.put(errorsKey, errorsJson.toString());

                        log.debug("Validation failed: {}", errorsJson);

                        // Close the call with INVALID_ARGUMENT status and error details in metadata
                        call.close(Status.INVALID_ARGUMENT
                                        .withDescription("Validation failed: " + result.getViolations().size() + " error(s)"),
                                trailers);
                        return;
                    }

                    // Message is valid, proceed with normal processing
                    super.onMessage(message);
                } catch (ValidationException e) {
                    log.debug("Validation error: {}", e.getMessage());

                    Metadata trailers = new Metadata();
                    Metadata.Key<String> errorKey = Metadata.Key.of(
                            "validation-exception", Metadata.ASCII_STRING_MARSHALLER);
                    trailers.put(errorKey, e.getMessage());

                    call.close(Status.INVALID_ARGUMENT.withDescription("Validation exception"), trailers);
                } catch (Exception e) {
                    log.error("Unexpected error during validation: {}", e.getMessage());
                    call.close(Status.INTERNAL.withDescription("Internal validation error"), new Metadata());
                }
            }
        };
    }
}