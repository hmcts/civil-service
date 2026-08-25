package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.cas.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ApplyNoticeOfChangeDecisionFixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;

@SuppressWarnings("java:S5960")
class ApplyNoticeOfChangeDecisionWorkflowTest extends WorkflowIntegrationTest {

    private static final String S2S_TOKEN = "s2s-token";

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    private void stubApplyDecisionReturningInputData(CaseData inputCaseData) {
        when(caseAssignmentApi.applyDecision(anyString(), anyString(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                            .data(inputCaseData.toMap(objectMapper))
                            .build());
    }

    @Test
    void shouldApplyNocDecisionForRespondent1SolicitorChange() throws Exception {
        CaseData fixture = ApplyNoticeOfChangeDecisionFixtures.respondent1SolicitorChange();
        stubApplyDecisionReturningInputData(fixture);

        startWorkflow(fixture)
            .eventId(CaseEvent.APPLY_NOC_DECISION)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getChangeOfRepresentation()).isNotNull();
                assertThat(caseData.getChangeOfRepresentation().getCaseRole())
                    .isEqualTo(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
                assertThat(caseData.getChangeOfRepresentation().getOrganisationToAddID())
                    .isEqualTo("NEW-ORG-001");
                assertThat(caseData.getChangeOfRepresentation().getOrganisationToRemoveID())
                    .isEqualTo("QWERTY-R1");

                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.APPLY_NOC_DECISION.name());

                assertThat(result.response().getData()).doesNotContainKey("addLegalRepDeadlineRes1");

                verify(caseAssignmentApi).applyDecision(anyString(), anyString(), any());
            });
    }

    @Test
    void shouldApplyNocDecisionForApplicantSolicitorChange() throws Exception {
        CaseData fixture = ApplyNoticeOfChangeDecisionFixtures.applicantSolicitorChange();
        stubApplyDecisionReturningInputData(fixture);

        startWorkflow(fixture)
            .eventId(CaseEvent.APPLY_NOC_DECISION)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getChangeOfRepresentation()).isNotNull();
                assertThat(caseData.getChangeOfRepresentation().getCaseRole())
                    .isEqualTo(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
                assertThat(caseData.getChangeOfRepresentation().getOrganisationToAddID())
                    .isEqualTo("NEW-ORG-001");

                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.APPLY_NOC_DECISION.name());
            });
    }

    @Test
    void shouldApplyNocDecisionForLipDefendant() throws Exception {
        CaseData fixture = ApplyNoticeOfChangeDecisionFixtures.lipToSolicitorChange();
        stubApplyDecisionReturningInputData(fixture);

        startWorkflow(fixture)
            .eventId(CaseEvent.APPLY_NOC_DECISION)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY,
                                     CaseEvent.APPLY_NOC_DECISION_DEFENDANT_LIP.name());
            });
    }

    @Test
    void shouldRejectWhenChangeOrganisationRequestIsMissing() throws Exception {
        CaseData fixture = ApplyNoticeOfChangeDecisionFixtures.missingChangeOrganisationRequest();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.APPLY_NOC_DECISION.name())
            .caseDetails(CaseDetails.builder()
                             .id(fixture.getCcdCaseReference())
                             .caseTypeId(CASE_TYPE)
                             .data(fixture.toMap(objectMapper))
                             .build())
            .build();

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, "about-to-submit")
            .andExpect(status().is4xxClientError());
    }
}
