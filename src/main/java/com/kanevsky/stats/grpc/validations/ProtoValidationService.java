package com.kanevsky.stats.grpc.validations;

import build.buf.protovalidate.Validator;
import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.exceptions.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class ProtoValidationService {
    private final Validator validator;

    public ProtoValidationService() {
            this.validator = new Validator();
    }

    public ValidationResult validate(com.google.protobuf.Message message) throws ValidationException {
        return validator.validate(message);
    }
}