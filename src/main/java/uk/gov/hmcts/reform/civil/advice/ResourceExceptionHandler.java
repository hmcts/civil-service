package uk.gov.hmcts.reform.civil.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;

@Slf4j
@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(value = CallbackException.class)
    public ResponseEntity<Object> notFound(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = StateFlowException.class)
    public ResponseEntity<Object> incorrectStateFlow(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<Object> badRequest(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

}
