package uk.gov.hmcts.reform.civil.advice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;

import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.FeignRetryUtils.RETRY_AFTER;

class ControllerExceptionHandlerTest {

    private final ControllerExceptionHandler controllerExceptionHandler = new ControllerExceptionHandler();

    @Test
    void pinNotMatched_returnsBadRequestWithoutDuplicateLogging() {
        Logger logger = (Logger) LoggerFactory.getLogger(ControllerExceptionHandler.class);
        Level originalLogLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.setLevel(Level.TRACE);
        logger.addAppender(appender);
        try {
            ResponseEntity<Object> response = controllerExceptionHandler.pinNotMatchedUnauthorised(
                new PinNotMatchException(), null
            );

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("BAD_REQUEST", response.getBody());
            assertThat(appender.list).isEmpty();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
            logger.setLevel(originalLogLevel);
        }
    }

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

    @Test
    void invalidToken_returnsUnauthorized() {
        ResponseEntity<Object> response = controllerExceptionHandler.invalidToken(
            new InvalidTokenException("Invalid S2S token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid S2S token", response.getBody());
    }
}
