package uk.gov.hmcts.reform.civil.scheduler.common;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ErrorCategorizerTest {

    private final ErrorCategorizer errorCategorizer = new ErrorCategorizer();

    @ParameterizedTest
    @CsvSource({
        "Database lock exception, Lock conflict",
        "Version conflict detected, Lock conflict",
        "Deadlock found when trying to get lock, Lock conflict",
        "IDAM authentication failed, IDAM error",
        "Connection timeout, Timeout error",
        "CCD service unavailable, CCD error",
        "Some other error, Other"
    })
    void shouldCategorizeErrorByMessage(String message, String expectedCategory) {
        Exception e = new RuntimeException(message);
        String category = errorCategorizer.categorizeError(e);
        assertThat(category).isEqualTo(expectedCategory);
    }

    @Test
    void shouldCategorizeAsIDAMErrorWhenFeignExceptionFromIdam() {
        FeignException e = mock(FeignException.class);
        org.mockito.Mockito.when(e.getMessage()).thenReturn("IDAM authentication failed");
        String category = errorCategorizer.categorizeError(e);
        assertThat(category).isEqualTo("IDAM error");
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
        assertThat(category).isEqualTo("Lock conflict");
    }
}
