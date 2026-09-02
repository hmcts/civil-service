package uk.gov.hmcts.reform.civil.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.RETRY_AFTER;

class ControllerExceptionHandlerTest {

    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();

    @Test
    void caseNotFoundBadRequest_returnsBadRequestWhenRequestWrapperIsNull() {
        ResponseEntity<Object> response = controllerExceptionHandler.caseNotFoundBadRequest(
            new CaseNotFoundException(),
            null
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Case was not found", response.getBody());
    }

    @Test
    void upstreamUnavailable_returnsServiceUnavailableWithRetryAfterHeader() {
        ResponseEntity<Object> response = controllerExceptionHandler.upstreamUnavailable(
            new UpstreamUnavailableException(
                "CCD case-users",
                "123",
                "uid",
                new RuntimeException("CCD failed")
            )
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("CCD case-users is currently unavailable", response.getBody());
        assertEquals("10", response.getHeaders().getFirst(RETRY_AFTER));
    }
}
