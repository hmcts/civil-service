package uk.gov.hmcts.reform.civil.advice;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.service.notify.NotificationClientException;

import java.net.UnknownHostException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResourceExceptionHandler();
    }

    @Test
    void shouldReturnNotFound_whenCallbackExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            CallbackException::new,
            handler::notFound,
            HttpStatus.NOT_FOUND
        );
    }

    @Test
    void shouldReturnPreconditionFailed_whenStateFlowExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            StateFlowException::new,
            handler::incorrectStateFlowOrIllegalArgument,
            HttpStatus.PRECONDITION_FAILED
        );
    }

    @Test
    void shouldReturnUnauthorized_whenFeignExceptionUnauthorizedExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            str -> new FeignException.Unauthorized(
                "expected exception for missing callback handler",
                Mockito.mock(feign.Request.class),
                new byte[]{}
            ),
            handler::unauthorizedFeign,
            HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    void shouldReturnMethodNotAllowed_whenUnknownHostException() {
        testTemplate(
            "expected exception for missing callback handler",
            UnknownHostException::new,
            handler::unknownHost,
            HttpStatus.NOT_ACCEPTABLE
        );
    }

    @Test
    void shouldReturnForbidden_whenFeignExceptionForbiddenExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            str -> new FeignException.Unauthorized(
                "expected exception for missing callback handler",
                Mockito.mock(feign.Request.class),
                new byte[]{}
            ),
            handler::forbiddenFeign,
            HttpStatus.FORBIDDEN
        );
    }

    @Test
    void shouldReturnMethodNotAllowed_whenNoSuchMethodErrorThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            NoSuchMethodError::new,
            handler::noSuchMethodError,
            HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @Test
    void shouldReturnPreconditionFailed_whenIllegalArgumentExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            IllegalArgumentException::new,
            handler::incorrectStateFlowOrIllegalArgument,
            HttpStatus.PRECONDITION_FAILED
        );
    }

    @Test
    void shouldReturnBadRequest_whenHttpClientErrorExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            exp -> new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "expected exception for missing callback handler"
            ),
            handler::badRequest,
            HttpStatus.BAD_REQUEST
        );
    }

    public void testFeignExceptionGatewayTimeoutException() {
        testTemplate(
            "gateway time out message",
            str -> new FeignException.GatewayTimeout(
                "gateway time out message",
                Mockito.mock(feign.Request.class),
                new byte[]{}
            ),
            handler::handleFeignExceptionGatewayTimeout,
            HttpStatus.GATEWAY_TIMEOUT
        );
    }

    @Test
    public void testHandleNotificationClientException() {
        testTemplate(
            "expected exception from notification api",
            NotificationClientException::new,
            handler::handleNotificationClientException,
            HttpStatus.FAILED_DEPENDENCY
        );
    }

    private <E extends Throwable> void testTemplate(
        String message,
        Function<String, E> exceptionBuilder,
        Function<E, ResponseEntity<?>> method,
        HttpStatus expectedStatus
    ) {
        E exception = exceptionBuilder.apply(message);
        ResponseEntity<?> result = method.apply(exception);
        assertThat(result.getStatusCode()).isSameAs(expectedStatus);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains(message);
    }

}
