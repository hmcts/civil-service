package uk.gov.hmcts.reform.civil.service.robotics.exception;

import com.networknt.schema.Error;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonSchemaValidationException extends RuntimeException {

    public JsonSchemaValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonSchemaValidationException(String message, List<Error> errors) {
        super(message);
        log.error("Schema validation errors: {}", errors);
    }
}
