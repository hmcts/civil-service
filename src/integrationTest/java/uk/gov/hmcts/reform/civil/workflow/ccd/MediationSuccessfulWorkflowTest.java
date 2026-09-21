package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.MediationWorkflowFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@SuppressWarnings({"java:S5960", "java:S6813"})
class MediationSuccessfulWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldTransitionToCaseStayedOnSuccessfulMediation() throws Exception {
        CaseData fixture = MediationWorkflowFixtures.withMediationSuccessfulData(
            MediationWorkflowFixtures.inMediation1v1()
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_SUCCESSFUL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo("CASE_STAYED");
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, "MEDIATION_SUCCESSFUL");
            });
    }

    @Test
    void shouldAddMediationAgreementDocumentToManageDocuments() throws Exception {
        CaseData fixture = MediationWorkflowFixtures.withMediationSuccessfulData(
            MediationWorkflowFixtures.inMediation1v1()
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_SUCCESSFUL)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.caseData().getManageDocuments()).isNotEmpty();
                assertThat(result.caseData().getManageDocuments())
                    .extracting(element -> element.getValue().getDocumentName())
                    .contains("mediation-agreement.pdf");
            });
    }

    @Test
    void shouldReturnEmptySubmittedResponse() throws Exception {
        CaseData fixture = MediationWorkflowFixtures.withMediationSuccessfulData(
            MediationWorkflowFixtures.inMediation1v1()
        );

        startWorkflow(fixture)
            .eventId(CaseEvent.MEDIATION_SUCCESSFUL)
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }
}
