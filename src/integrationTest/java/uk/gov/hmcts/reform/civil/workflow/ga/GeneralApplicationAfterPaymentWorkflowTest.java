package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.service.GeneralAppsDeadlinesCalculator;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5960")
class GeneralApplicationAfterPaymentWorkflowTest extends GAWorkflowIntegrationTest {

    private static final LocalDateTime RESET_RESPONSE_DEADLINE = LocalDateTime.of(2026, 8, 14, 16, 0);

    @MockBean
    private GeneralAppsDeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setUpDeadline() {
        when(deadlinesCalculator.calculateApplicantResponseDeadlineWithWeekendCheck(
            any(LocalDateTime.class),
            anyInt()
        )).thenReturn(RESET_RESPONSE_DEADLINE);
    }

    @Test
    void shouldRestartTheApplicationWorkflowAndResetTheResponseDeadlineAfterPayment() throws Exception {
        startWorkflow(GaLifecycleFixtures.successfulPayment())
            .eventId(CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(
                        BusinessProcessStatus.READY,
                        CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT.name()
                    );
                assertThat(result.caseData().getGeneralAppNotificationDeadlineDate())
                    .isEqualTo(RESET_RESPONSE_DEADLINE);
                assertThat(result.caseData().getRespondentResponseDeadlineChecked()).isEqualTo(YesOrNo.NO);
            })
            .submitted();

        verify(gaEventEmitterService).emitBusinessProcessCamundaGAEvent(
            argThat(actual -> actual.getCcdCaseReference().equals(GaLifecycleFixtures.CASE_ID)
                && actual.getBusinessProcess().getStatus() == BusinessProcessStatus.READY
                && actual.getBusinessProcess().getCamundaEvent()
                .equals(CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT.name())),
            eq(false)
        );
    }
}
