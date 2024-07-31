package uk.gov.hmcts.reform.civil.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.request.RequestData;

import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UncaughtExceptionHandlerTest {

    private static final String CASE_ID = "1234567891234560";
    private static final String USER_ID = UUID.randomUUID().toString();

    @Mock
    private RequestData requestData;

    @InjectMocks
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    @Test
    void shouldReturnNotFound_whenCallbackExceptionThrown() {
        when(requestData.caseId()).thenReturn(CASE_ID);
        when(requestData.userId()).thenReturn(USER_ID);
        testTemplate(
            NullPointerException::new,
            uncaughtExceptionHandler::runtimeException
        );
    }

    private <E extends Throwable> void testTemplate(
        Function<String, E> exceptionBuilder,
        Function<E, ResponseEntity<?>> method
    ) {
        E exception = exceptionBuilder.apply("Null pointer Exception!!");
        ResponseEntity<?> result = method.apply(exception);
        assertThat(result.getStatusCode()).isSameAs(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains("Null pointer Exception!!");
        assertThat(result.getBody()).extracting(Object::toString).asString().contains(USER_ID);
        assertThat(result.getBody()).extracting(Object::toString).asString().contains(CASE_ID);
    }
}
