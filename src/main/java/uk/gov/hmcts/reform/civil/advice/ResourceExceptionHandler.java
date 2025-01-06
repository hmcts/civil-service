package uk.gov.hmcts.reform.civil.advice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.service.notify.NotificationClientException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.springframework.http.HttpStatus.FAILED_DEPENDENCY;
import static uk.gov.hmcts.reform.civil.utils.CaseDataRequestUtil.getCaseId;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ResourceExceptionHandler {

    @ExceptionHandler(value = CallbackException.class)
    public ResponseEntity<Object> notFound(Exception exception,
                                           ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Not found error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ClientAbortException.class})
    public ResponseEntity<String> handleClientAbortException(Exception exception,
                                                             ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Request aborted error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(),
                                    new HttpHeaders(), HttpStatus.REQUEST_TIMEOUT
        );
    }

    @ExceptionHandler({
        StateFlowException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<Object> incorrectStateFlowOrIllegalArgument(Exception exception,
                                                                      ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Illegal flow state / illegal arg error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(value = HttpClientErrorException.BadRequest.class)
    public ResponseEntity<Object> badRequest(Exception exception,
                                             ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Bad request error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
        UnknownHostException.class,
        InvalidPaymentRequestException.class
    })
    public ResponseEntity<Object> unknownHostAndInvalidPayment(Exception exception,
                                                               ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Unknown host / invalid payment error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value =  FeignException.Unauthorized.class)
    public ResponseEntity<Object> unauthorizedFeign(Exception exception,
                                                    ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Unauthorized feign error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value =  FeignException.Forbidden.class)
    public ResponseEntity<Object> forbiddenFeign(Exception exception,
                                                 ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Forbidden feign error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value =  FeignException.NotFound.class)
    public ResponseEntity<Object> feignExceptionNotFound(FeignException exception,
                                                         ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Not found feign error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = NoSuchMethodError.class)
    public ResponseEntity<Object> noSuchMethodError(Throwable error,
                                                    ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(error.getMessage(), error);
        String errorMessage = "No such method error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(error.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(error.getMessage(), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler({FeignException.GatewayTimeout.class, SocketTimeoutException.class})
    public ResponseEntity<String> handleFeignExceptionGatewayTimeout(Exception exception,
                                                                     ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Feign gateway timeout error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(),
                                    new HttpHeaders(), HttpStatus.GATEWAY_TIMEOUT
        );
    }

    @ExceptionHandler(NotificationClientException.class)
    public ResponseEntity<Object> handleNotificationClientException(NotificationClientException exception,
                                                                    ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "Notification client error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return ResponseEntity
            .status(FAILED_DEPENDENCY)
            .body(exception.getMessage());
    }

    @ExceptionHandler({
        JsonSchemaValidationException.class
    })
    public ResponseEntity<Object> handleJsonSchemaValidationException(JsonSchemaValidationException exception,
                                                                      ContentCachingRequestWrapper contentCachingRequestWrapper) {
        log.debug(exception.getMessage(), exception);
        String errorMessage = "JSON validation error with message: %s for case %s run by user %s";
        log.error(errorMessage.formatted(exception.getMessage(), getCaseId(contentCachingRequestWrapper),
                                         contentCachingRequestWrapper.getHeader("user-id")));
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.EXPECTATION_FAILED);
    }
}
