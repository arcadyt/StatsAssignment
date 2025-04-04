package com.kanevsky.stats.grpc.interceptors;

import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.exceptions.ValidationException;
import com.kanevsky.stats.grpc.validations.ProtoValidationService;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                        String errorMessage = formatValidationErrors(result);
                        log.debug("Validation failed: {}", errorMessage);
                        call.close(Status.INVALID_ARGUMENT.withDescription(errorMessage), new Metadata());
                        return;
                    }

                    // Message is valid, proceed with normal processing
                    super.onMessage(message);
                } catch (ValidationException e) {
                    log.debug("Validation error: {}", e.getMessage());
                    call.close(Status.INVALID_ARGUMENT.withDescription(e.getMessage()), new Metadata());
                } catch (Exception e) {
                    log.error("Unexpected error during validation: {}", e.getMessage());
                    call.close(Status.INTERNAL.withDescription("Internal validation error"), new Metadata());
                }
            }

            private String formatValidationErrors(ValidationResult result) {
                return result.getViolations().toString();
            }
        };
    }
}