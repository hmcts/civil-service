package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("java:S5960")
class ApplicationOfflineWorkflowTest extends GAWorkflowIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 7, 10, 15);

    @MockBean
    private Time time;

    @MockBean
    private DocUploadDashboardNotificationService dashboardNotificationService;

    @BeforeEach
    void setUpTime() {
        when(time.now()).thenReturn(NOW);
    }

    @Test
    void shouldMoveALiveApplicationOfflineAndNotifyBothLipParties() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.paidWithResponse().copy()
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE);
                assertThat(result.caseData().getApplicationTakenOfflineDate()).isEqualTo(NOW);
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.FINISHED, CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE.name());
            });

        verify(dashboardNotificationService)
            .createOfflineResponseDashboardNotification(
                argThat(actual -> actual.getCcdCaseReference().equals(caseData.getCcdCaseReference())
                    && actual.getCcdState() == CaseState.AWAITING_RESPONDENT_RESPONSE
                    && actual.getIsGaApplicantLip() == YesOrNo.YES
                    && actual.getIsGaRespondentOneLip() == YesOrNo.YES),
                eq("APPLICANT"),
                eq(BEARER_TOKEN)
            );
        verify(dashboardNotificationService)
            .createOfflineResponseDashboardNotification(
                argThat(actual -> actual.getCcdCaseReference().equals(caseData.getCcdCaseReference())
                    && actual.getCcdState() == CaseState.AWAITING_RESPONDENT_RESPONSE
                    && actual.getIsGaApplicantLip() == YesOrNo.YES
                    && actual.getIsGaRespondentOneLip() == YesOrNo.YES),
                eq("RESPONDENT"),
                eq(BEARER_TOKEN)
            );
    }

    @Test
    void shouldLeaveAnOrderMadeApplicationUnchangedWhenTheParentGoesOffline() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.paidWithResponse().copy()
            .ccdState(CaseState.ORDER_MADE)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.ORDER_MADE);
                assertThat(result.caseData().getApplicationTakenOfflineDate()).isNull();
            });

        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldCloseALiveApplicationWhenTheMainCaseCloses() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.paidWithResponse().copy()
            .businessProcess(null)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.MAIN_CASE_CLOSED)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.APPLICATION_CLOSED);
                assertThat(result.caseData().getApplicationClosedDate()).isEqualTo(NOW);
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.FINISHED, CaseEvent.MAIN_CASE_CLOSED.name());
            });
    }

    @Test
    void shouldLeaveADismissedApplicationUnchangedWhenTheMainCaseCloses() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.paidWithResponse().copy()
            .ccdState(CaseState.APPLICATION_DISMISSED)
            .businessProcess(null)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.MAIN_CASE_CLOSED)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.caseData().getCcdState()).isEqualTo(CaseState.APPLICATION_DISMISSED);
                assertThat(result.caseData().getApplicationClosedDate()).isNull();
                assertThat(result.caseData().getBusinessProcess()).isNull();
            });
    }
}
