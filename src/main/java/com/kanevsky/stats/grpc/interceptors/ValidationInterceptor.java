package com.kanevsky.stats.grpc.interceptors;

import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.exceptions.ValidationException;
import build.buf.validate.FieldPathElement;
import com.kanevsky.stats.grpc.validations.ProtoValidationService;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class ValidationInterceptor implements ServerInterceptor {

    private static final String VALIDATION_ERRORS_KEY = "validation-errors";
    private static final String VALIDATION_EXCEPTION_KEY = "validation-exception";
    private static final String VALIDATION_FAILED_MSG = "Validation failed: {} error(s)";
    private static final String VALIDATION_ERROR_LOG_MSG = "Validation error: {}";
    private static final String UNEXPECTED_ERROR_LOG_MSG = "Unexpected error during validation: {}";
    private static final String INTERNAL_ERROR_MSG = "Internal validation error";
    private static final String VALIDATION_EXCEPTION_MSG = "Validation exception";

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
                    ValidationResult result = validationService.validate((com.google.protobuf.Message) message);

                    if (!result.isSuccess()) {
                        handleValidationFailure(call, result);
                        return;
                    }

                    super.onMessage(message);
                } catch (ValidationException e) {
                    handleValidationException(call, e);
                } catch (Exception e) {
                    handleUnexpectedException(call, e);
                }
            }
        };
    }

    private <ReqT, RespT> void handleValidationFailure(ServerCall<ReqT, RespT> call, ValidationResult result) {
        Metadata trailers = new Metadata();
        Metadata.Key<String> errorsKey = Metadata.Key.of(
                VALIDATION_ERRORS_KEY, Metadata.ASCII_STRING_MARSHALLER);

        String errorsJson = formatViolationsAsJson(result);
        trailers.put(errorsKey, errorsJson);

        log.debug("Validation failed: {}", errorsJson);

        call.close(Status.INVALID_ARGUMENT
                        .withDescription(String.format(VALIDATION_FAILED_MSG, result.getViolations().size())),
                trailers);
    }

    private <ReqT, RespT> void handleValidationException(ServerCall<ReqT, RespT> call, ValidationException e) {
        log.debug(VALIDATION_ERROR_LOG_MSG, e.getMessage());

        Metadata trailers = new Metadata();
        Metadata.Key<String> errorKey = Metadata.Key.of(
                VALIDATION_EXCEPTION_KEY, Metadata.ASCII_STRING_MARSHALLER);
        trailers.put(errorKey, e.getMessage());

        call.close(Status.INVALID_ARGUMENT.withDescription(VALIDATION_EXCEPTION_MSG), trailers);
    }

    private <ReqT, RespT> void handleUnexpectedException(ServerCall<ReqT, RespT> call, Exception e) {
        call.close(Status.INTERNAL.withDescription(INTERNAL_ERROR_MSG), new Metadata());
    }

    private String formatViolationsAsJson(ValidationResult result) {
        return "[" + result.getViolations().stream()
                .map(violation -> {
                    String fieldPath = violation.toProto().getField().getElementsList().stream()
                            .map(FieldPathElement::getFieldName)
                            .collect(Collectors.joining("."));

                    String errorMessage = violation.toProto().getMessage();

                    return String.format("{\"field\":\"%s\",\"message\":\"%s\"}", fieldPath, errorMessage);
                })
                .collect(Collectors.joining(",")) + "]";
    }
}