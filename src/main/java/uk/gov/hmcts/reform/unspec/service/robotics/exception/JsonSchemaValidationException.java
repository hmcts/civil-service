package uk.gov.hmcts.reform.unspec.service.robotics.exception;

import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class JsonSchemaValidationException extends RuntimeException {

    public JsonSchemaValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonSchemaValidationException(String message, Set<ValidationMessage> errors) {
        super(message);
        log.error("Schema validation errors: {}", errors);
    }
}
