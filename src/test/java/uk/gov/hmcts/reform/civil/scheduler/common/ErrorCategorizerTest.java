package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCategorizerTest {

    private static final String CCD_ERROR = "CCD error";

    private final ErrorCategorizer errorCategorizer = new ErrorCategorizer();

    @ParameterizedTest
    @CsvSource({
        "Database lock exception, lock conflict",
        "Version conflict detected, lock conflict",
        "IDAM authentication failed, IDAM timeout",
        "Connection timeout, IDAM timeout",
        "CCD service unavailable, CCD error",
        "Some other error, Other"
    })
    void shouldCategorizeErrorByMessage(String message, String expectedCategory) {
        Exception e = new RuntimeException(message);
        String category = errorCategorizer.categorizeError(e);
        assertThat(category).isEqualTo(expectedCategory);
    }

    @Test
    void shouldCategorizeAsCCDErrorWhenFeignException() {
        Exception e = new CustomFeignException("Service error");
        String category = errorCategorizer.categorizeError(e);
        assertThat(category).isEqualTo(CCD_ERROR);
    }

    @Test
    void shouldHandleNullExceptionMessage() {
        Exception e = new RuntimeException((String) null);
        String category = errorCategorizer.categorizeError(e);
        assertThat(category).isEqualTo("Other");
    }

    @Test
    void shouldHandleCaseInsensitivity() {
        Exception e = new RuntimeException("LOCK obtained");
        String category = errorCategorizer.categorizeError(e);
        assertThat(category).isEqualTo("lock conflict");
    }

    static class CustomFeignException extends RuntimeException {
        public CustomFeignException(String message) {
            super(message);
        }
    }
}
