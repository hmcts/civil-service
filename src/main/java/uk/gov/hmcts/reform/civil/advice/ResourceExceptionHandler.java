package uk.gov.hmcts.reform.civil.advice;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;

import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

@Slf4j
@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(value = CallbackException.class)
    public ResponseEntity<Object> notFound(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
        StateFlowException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<Object> incorrectStateFlowOrIllegalArgument(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.PRECONDITION_FAILED);
    }


    @ExceptionHandler(value = UnknownHostException.class)
    public ResponseEntity<Object> unknownHost(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value =  FeignException.Unauthorized.class)
    public ResponseEntity<Object> unauthorizedFeign(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value =  FeignException.Forbidden.class)
    public ResponseEntity<Object> forbiddenFeign(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = NoSuchMethodError.class)
    public ResponseEntity<Object> noSuchMethodError(Throwable error) {
        log.debug(error.getMessage(), error);
        return new ResponseEntity<>(error.getMessage(), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);

    @ExceptionHandler({FeignException.GatewayTimeout.class, SocketTimeoutException.class})
    public ResponseEntity<String> handleFeignExceptionGatewayTimeout(Exception exception) {
        log.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(),
                                    new HttpHeaders(), HttpStatus.GATEWAY_TIMEOUT);
    }

}
