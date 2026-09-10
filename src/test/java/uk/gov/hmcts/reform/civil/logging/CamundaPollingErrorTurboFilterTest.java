package uk.gov.hmcts.reform.civil.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.core.JsonParseException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.camunda.bpm.client.exception.RestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class CamundaPollingErrorTurboFilterTest {

    private static final String FETCH_MSG = "TASK/CLIENT-03001 Exception while fetching and locking task.";

    private final CamundaPollingErrorTurboFilter filter = new CamundaPollingErrorTurboFilter();
    private final LoggerContext loggerContext = new LoggerContext();
    private final Logger camundaLogger = loggerContext.getLogger("org.camunda.bpm.client");
    private final Logger otherLogger = loggerContext.getLogger("uk.gov.hmcts.reform.civil.SomethingElse");

    private Logger summaryLogger;
    private ListAppender<ILoggingEvent> summaryAppender;

    @BeforeEach
    void setUp() {
        filter.setWindowSeconds(3600);
        summaryLogger = (Logger) LoggerFactory.getLogger(CamundaPollingErrorTurboFilter.SUMMARY_LOGGER);
        summaryAppender = new ListAppender<>();
        summaryAppender.start();
        summaryLogger.addAppender(summaryAppender);
    }

    @AfterEach
    void tearDown() {
        summaryLogger.detachAppender(summaryAppender);
        summaryAppender.stop();
    }

    @Test
    void demotesFetchAndLockErrorCausedByGateway5xx() {
        FilterReply reply = filter.decide(null, camundaLogger, Level.ERROR, FETCH_MSG, null, wrapped(restException(502)));

        assertThat(reply).isEqualTo(FilterReply.DENY);
        assertThat(summaryAppender.list).hasSize(1);
        assertThat(summaryAppender.list.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(summaryAppender.list.get(0).getFormattedMessage())
            .contains("Suppressed 1 transient Camunda external-task fetchAndLock error");
    }

    @Test
    void demotesParseFailureCausedByNonJsonBody() {
        String parseMsg = "TASK/CLIENT-02004 Exception while parsing json object to response dto class 'x'";

        FilterReply reply = filter.decide(null, camundaLogger, Level.ERROR, parseMsg, null,
            wrapped(new JsonParseException(null, "Unexpected character ('<' (code 60))")));

        assertThat(reply).isEqualTo(FilterReply.DENY);
    }

    @Test
    void demotesFetchAndLockErrorCausedByDroppedConnection() {
        FilterReply reply = filter.decide(null, camundaLogger, Level.ERROR, FETCH_MSG, null,
            wrapped(new NoHttpResponseException("host failed to respond")));

        assertThat(reply).isEqualTo(FilterReply.DENY);
    }

    @Test
    void keepsFetchAndLockErrorCausedByA4xx() {
        FilterReply reply = filter.decide(null, camundaLogger, Level.ERROR, FETCH_MSG, null, wrapped(restException(400)));

        assertThat(reply).isEqualTo(FilterReply.NEUTRAL);
        assertThat(summaryAppender.list).isEmpty();
    }

    @Test
    void keepsFetchAndLockErrorWithANonUpstreamCause() {
        FilterReply reply = filter.decide(null, camundaLogger, Level.ERROR, FETCH_MSG, null,
            wrapped(new IllegalStateException("bug in a handler")));

        assertThat(reply).isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void ignoresNonErrorLevels() {
        assertThat(filter.decide(null, camundaLogger, Level.WARN, FETCH_MSG, null, wrapped(restException(503))))
            .isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void ignoresOtherLoggers() {
        assertThat(filter.decide(null, otherLogger, Level.ERROR, FETCH_MSG, null, wrapped(restException(503))))
            .isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void ignoresUnrelatedCamundaMessages() {
        assertThat(filter.decide(null, camundaLogger, Level.ERROR,
            "TASK/CLIENT-03002 Exception while executing external task handler 'x'.", null,
            wrapped(restException(503)))).isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void emitsOnlyOneSummaryPerWindowForABurst() {
        for (int i = 0; i < 25; i++) {
            filter.decide(null, camundaLogger, Level.ERROR, FETCH_MSG, null, wrapped(restException(504)));
        }

        assertThat(summaryAppender.list).hasSize(1);
    }

    private static Throwable wrapped(Throwable cause) {
        return new RuntimeException("TASK/CLIENT-03001 Exception while fetching and locking task.", cause);
    }

    private static RestException restException(int status) {
        RestException e = new RestException("upstream said " + status, "SomeType", null);
        e.setHttpStatusCode(status);
        return e;
    }
}
