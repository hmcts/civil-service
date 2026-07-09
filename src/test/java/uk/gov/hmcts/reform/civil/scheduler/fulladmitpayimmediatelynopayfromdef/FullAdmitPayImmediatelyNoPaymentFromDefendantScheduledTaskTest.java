package uk.gov.hmcts.reform.civil.scheduler.fulladmitpayimmediatelynopayfromdef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTaskTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @Spy
    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask scheduledTask;

    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseDetails = CaseDetails.builder()
            .id(123L)
            .build();
    }

    @Test
    void shouldReturnCaseId() {
        assertThat(scheduledTask.getItemId(caseDetails)).isEqualTo(123L);
    }

    @Test
    void shouldAcceptAndPublishEvent() {
        scheduledTask.accept(caseDetails);

        verify(scheduledTask).setFullAdmitNoPaymentSchedulerProcessed(123L);

        ArgumentCaptor<FullAdmitPayImmediatelyNoPaymentFromDefendantEvent> captor =
            ArgumentCaptor.forClass(FullAdmitPayImmediatelyNoPaymentFromDefendantEvent.class);

        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().caseId()).isEqualTo(123L);
    }

    @Test
    void shouldTriggerEventToUpdateCase() {
        scheduledTask.setFullAdmitNoPaymentSchedulerProcessed(123L);

        verify(coreCaseDataService).triggerEvent(
            eq(123L),
            eq(UPDATE_CASE_DATA),
            argThat(map ->
                        YesOrNo.YES.equals(map.get("fullAdmitNoPaymentSchedulerProcessed"))
            ),
            eq("Updating case - Full Admit No Payment Dashboard notification created successfully"),
            eq("Updating case - Full Admit No Payment Dashboard notification created successfully")
        );
    }

    @Test
    void shouldReturnBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure())
            .thenReturn(backPressureConfiguration);

        assertThat(scheduledTask.backPressureConfiguration())
            .isEqualTo(backPressureConfiguration);
    }
}
