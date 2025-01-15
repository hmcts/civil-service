package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.civil.interceptors.RequestInterceptor;

import static org.junit.jupiter.api.Assertions.assertNull;

class RequestInterceptorTest {

    private static final String CASE_ID = "caseId";

    RequestInterceptor requestInterceptor;

    @BeforeEach
    void setUp() {
        requestInterceptor = new RequestInterceptor();
    }

    @Test
    void shouldRemoveCaseIdFromMdc() {
        MDC.put(CASE_ID, "testValue");
        requestInterceptor.afterCompletion(null, null, null, null);
        assertNull(MDC.get(CASE_ID));
    }
}
