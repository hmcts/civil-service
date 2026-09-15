package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.MediationWorkflowFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SuppressWarnings({"java:S5960", "java:S6813"})
class MediationUnsuccessfulWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldTransitionToJudicialReferralOnUnsuccessfulMediation() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData fixture = MediationWorkflowFixtures.withMediationUnsuccessfulData(
            MediationWorkflowFixtures.inMediation1v1(),
            MediationUnsuccessfulReason.PARTY_WITHDRAWS
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_UNSUCCESSFUL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo("JUDICIAL_REFERRAL");
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, "MEDIATION_UNSUCCESSFUL");
            });
    }

    @Test
    void shouldHandlePartyWithdrawsReason() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData fixture = MediationWorkflowFixtures.withMediationUnsuccessfulData(
            MediationWorkflowFixtures.inMediation1v1(),
            MediationUnsuccessfulReason.PARTY_WITHDRAWS
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_UNSUCCESSFUL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getState()).isEqualTo("JUDICIAL_REFERRAL");
                assertThat(result.caseData().getMediation().getUnsuccessfulMediationReason())
                    .isEqualTo(MediationUnsuccessfulReason.PARTY_WITHDRAWS.getValue());
            });
    }

    @Test
    void shouldHandleNotContactableClaimantReason() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData fixture = MediationWorkflowFixtures.withMediationUnsuccessfulData(
            MediationWorkflowFixtures.inMediation1v1(),
            MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_UNSUCCESSFUL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getState()).isEqualTo("JUDICIAL_REFERRAL");
                assertThat(result.caseData().getMediation().getMediationUnsuccessfulReasonsMultiSelect())
                    .contains(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE);
            });
    }

    @Test
    void shouldHandleAppointmentNoAgreementReason() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        CaseData fixture = MediationWorkflowFixtures.withMediationUnsuccessfulData(
            MediationWorkflowFixtures.inMediation1v1(),
            MediationUnsuccessfulReason.APPOINTMENT_NO_AGREEMENT
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_UNSUCCESSFUL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo("JUDICIAL_REFERRAL");
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, "MEDIATION_UNSUCCESSFUL");
            });
    }

    @Test
    void shouldReturnEmptySubmittedResponse() throws Exception {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);

        CaseData fixture = MediationWorkflowFixtures.inMediation1v1();

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_UNSUCCESSFUL)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }
}
