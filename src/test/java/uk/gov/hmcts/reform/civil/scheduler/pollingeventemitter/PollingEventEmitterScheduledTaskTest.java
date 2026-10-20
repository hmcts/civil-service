package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingEventEmitterScheduledTaskTest {

    private static final long CASE_ID = 123L;
    private static final String CAMUNDA_EVENT = "TEST_EVENT";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private EventEmitterService eventEmitterService;
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @InjectMocks
    private PollingEventEmitterScheduledTask task;
    private CaseDetails caseDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseDetails = CaseDetails.builder().id(CASE_ID).data(Map.of()).build();
        caseData = mock(CaseData.class);
    }

    @Test
    void shouldEmitBusinessProcessCamundaEvent() {
        when(caseData.getBusinessProcess()).thenReturn(new BusinessProcess().setCamundaEvent(CAMUNDA_EVENT));
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        task.accept(caseDetails);

        verify(caseDetailsConverter).toCaseData(caseDetails);
        verify(caseDetailsConverter, never()).toCaseData(caseDetails.getData());
        verify(eventEmitterService).emitBusinessProcessCamundaEvent(caseData, true);
    }

    @Test
    void shouldLimitMaxCasesToFitScheduledWindow() {
        ReflectionTestUtils.setField(task, "multiCasesExecutionDelayInSeconds", 30L);

        assertThat(task.maxCasesPerRun()).isEqualTo(100);
    }

    @Test
    void shouldUseMinimumOneSecondDelayWhenConfiguredDelayIsInvalid() {
        ReflectionTestUtils.setField(task, "multiCasesExecutionDelayInSeconds", 0L);

        assertThat(task.maxCasesPerRun()).isEqualTo(PollingEventEmitterScheduler.FIFTY_MINUTES_IN_SECONDS);
    }
}
