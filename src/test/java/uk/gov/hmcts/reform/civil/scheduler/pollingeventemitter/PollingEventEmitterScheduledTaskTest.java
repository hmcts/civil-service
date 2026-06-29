package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.SchedulerThrottleUtils;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingEventEmitterScheduledTaskTest {

    private static final long CASE_ID = 123L;
    private static final long TOTAL_CASES = 10L;
    private static final long DELAY_MS = 30000L;
    private static final long DEFAULT_LOCK_DURATION = 1980000L;
    private static final long POLLING_WINDOW_MS = TimeUnit.SECONDS.toMillis(
        PollingEventEmitterScheduler.FIFTY_MINUTES_IN_SECONDS
    );
    private static final String CAMUNDA_EVENT = "TEST_EVENT";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private EventEmitterService eventEmitterService;

    private PollingEventEmitterScheduledTask task;
    private CaseDetails caseDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        EventProperties eventProperties = new EventProperties();
        eventProperties.setDispatchDelay((int) DELAY_MS);
        eventProperties.setLockDuration(DEFAULT_LOCK_DURATION);
        task = new PollingEventEmitterScheduledTask(caseDetailsConverter, eventEmitterService, eventProperties);

        caseDetails = CaseDetails.builder().id(CASE_ID).data(Map.of()).build();
        caseData = mock(CaseData.class);
        when(caseData.getBusinessProcess()).thenReturn(new BusinessProcess().setCamundaEvent(CAMUNDA_EVENT));
    }

    @Test
    void shouldEmitBusinessProcessCamundaEventAndThrottle() {
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        try (MockedStatic<SchedulerThrottleUtils> throttleUtils = mockStatic(SchedulerThrottleUtils.class)) {
            task.accept(caseDetails, TOTAL_CASES, DELAY_MS);

            verify(caseDetailsConverter).toCaseData(caseDetails);
            verify(caseDetailsConverter, never()).toCaseData(caseDetails.getData());
            verify(eventEmitterService).emitBusinessProcessCamundaEvent(caseData, true);
            throttleUtils.verify(() -> SchedulerThrottleUtils.throttle(TOTAL_CASES, DELAY_MS, POLLING_WINDOW_MS));
        }
    }

    @Test
    void shouldUseConfiguredDispatchDelayWhenCalledAsScheduledTask() {
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        try (MockedStatic<SchedulerThrottleUtils> throttleUtils = mockStatic(SchedulerThrottleUtils.class)) {
            task.accept(caseDetails);

            verify(caseDetailsConverter).toCaseData(caseDetails);
            verify(caseDetailsConverter, never()).toCaseData(caseDetails.getData());
            verify(eventEmitterService).emitBusinessProcessCamundaEvent(caseData, true);
            throttleUtils.verify(() -> SchedulerThrottleUtils.throttle(1, DELAY_MS, POLLING_WINDOW_MS));
        }
    }
}
