package uk.gov.hmcts.reform.civil.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UncaughtExceptionHandlerTest {

    public static final String NULL_POINTER_EXCEPTION = "Null pointer Exception!!";

    @Mock
    private ContentCachingRequestWrapper contentCachingRequestWrapper;

    @InjectMocks
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    @BeforeEach
    void setUp() {
        String jsonString = "{ \"case_details\" : {\"id\" : \"1234\"}}";
        when(contentCachingRequestWrapper.getHeader("user-id")).thenReturn("4321");
        when(contentCachingRequestWrapper.getContentAsByteArray()).thenReturn(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldReturnInternalServerError_whenUncaughtExceptionThrown() {
        Exception e = new NullPointerException(NULL_POINTER_EXCEPTION);
        ResponseEntity<?> result = uncaughtExceptionHandler.runtimeException(e, contentCachingRequestWrapper);

        assertThat(result.getStatusCode()).isSameAs(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains(NULL_POINTER_EXCEPTION);
    }

}
