package uk.gov.hmcts.reform.civil.advice;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler handler;
    private FeignException feignException;
    private Request Request;
    private FeignException feignException;

    @BeforeEach
    void setUp() {
        handler = new ResourceExceptionHandler();
        feignException = new FeignException();

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
    void shouldReturnForbidden_whenFeignExceptionForbiddenExceptionThrown() {
        testTemplateA(
            "expected exception for missing callback handler",
            FeignException.Unauthorized::new,
            handler::forbiddenFeign,
            HttpStatus.FORBIDDEN
        );
    }

   /* @Test
    void shouldReturnUnauthorized_whenFeignExceptionUnauthorizedExceptionThrown() {
        testTemplate(
            "expected exception for missing callback handler",
            RuntimeException::new,
            handler::unauthorizedFeign,
            HttpStatus.UNAUTHORIZED
        );
    }*/



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

    private <E extends Exception> void testTemplateA(
        String message,
        FeignException.Unauthorized e,
        Function<E, ResponseEntity<?>> method,
        HttpStatus expectedStatus
    ) {
        //E exception = exceptionBuilder.apply(message);
        int i= 200;
       // FeignException exception = new FeignException.Unauthorized(message, request, body) ;
        ResponseEntity<?> result = method.apply((E) e);
        assertThat(result.getStatusCode()).isSameAs(expectedStatus);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains(message);
    }

}


