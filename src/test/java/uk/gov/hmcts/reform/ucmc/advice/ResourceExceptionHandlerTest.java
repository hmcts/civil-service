package uk.gov.hmcts.reform.ucmc.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ucmc.callback.CallbackException;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new ResourceExceptionHandler();
    }

    @Test
    public void shouldReturnNotFoundWhenCallbackException() {
        testTemplate(
            "expected exception for missing callback handler",
            CallbackException::new,
            handler::notFound,
            HttpStatus.NOT_FOUND
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
}
