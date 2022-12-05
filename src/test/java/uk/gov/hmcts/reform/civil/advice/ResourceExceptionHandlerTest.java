package uk.gov.hmcts.reform.civil.advice;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.function.Function;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
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
            handler::incorrectStateFlow,
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
        FeignException notFoundFeignException = new FeignException.GatewayTimeout(
            "gateway time out message",
            Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
            "gateway time out response body".getBytes(UTF_8)
        );
        testTemplate(
            "gateway time out message",
            notFoundFeignException,
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

    private <E extends Exception> void testTemplate(
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

    private void testTemplate(
        String message,
        FeignException exception,
        Function<FeignException, ResponseEntity<?>> method,
        HttpStatus expectedStatus
    ) {
        ResponseEntity<?> result = method.apply(exception);
        assertThat(result.getStatusCode()).isSameAs(expectedStatus);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains(message);
    }
}
