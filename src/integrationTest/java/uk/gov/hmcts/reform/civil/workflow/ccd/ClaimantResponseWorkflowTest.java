package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specified1v2SameSolicitorFullAdmitClaimantResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specified1v2SameSolicitorPartAdmitClaimantResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specifiedFullAdmitClaimantAcceptsResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecified2v1Applicant2ProceedsResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecifiedClaimantDoesNotProceedResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecifiedClaimantProceedsResponse;

@SuppressWarnings("java:S5960")
class ClaimantResponseWorkflowTest extends WorkflowIntegrationTest {

    @Test
    void shouldSubmitUnspecifiedFullDefenceProceedAndMoveToProceedsInHeritageSystem() throws Exception {
        CaseData caseData = unspecifiedClaimantProceedsResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(CLAIMANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getApplicant1ProceedWithClaim()).isEqualTo(YesOrNo.YES);
                assertThat(updatedData.getApplicant1ResponseDate()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, CLAIMANT_RESPONSE.name());
                assertThat(updatedData.getApplicant1DefenceResponseDocument().getFile().getDocumentUrl()).isNull();
            });
    }

    @Test
    void shouldSubmitUnspecifiedFullDefenceNotProceedAndMoveToProceedsInHeritageSystem() throws Exception {
        CaseData caseData = unspecifiedClaimantDoesNotProceedResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(CLAIMANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getApplicant1ProceedWithClaim()).isEqualTo(YesOrNo.NO);
                assertThat(updatedData.getApplicant1ResponseDate()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isNull();
                assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(CLAIMANT_RESPONSE.name());
            });
    }

    @Test
    void shouldSubmitUnspecified2v1ClaimantResponseForApplicant2AndMoveToProceedsInHeritageSystem() throws Exception {
        CaseData caseData = unspecified2v1Applicant2ProceedsResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(CLAIMANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getApplicant1ProceedWithClaimMultiParty2v1()).isEqualTo(YesOrNo.NO);
                assertThat(updatedData.getApplicant2ProceedWithClaimMultiParty2v1()).isEqualTo(YesOrNo.YES);
                assertThat(updatedData.getApplicant1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant2ResponseDate()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, CLAIMANT_RESPONSE.name());
            });
    }

    @Test
    void shouldSubmitSpecifiedFullAdmitClaimantResponse() throws Exception {
        CaseData caseData = specifiedFullAdmitClaimantAcceptsResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(CLAIMANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getApplicant1AcceptFullAdmitPaymentPlanSpec()).isEqualTo(YesOrNo.YES);
                assertThat(updatedData.getApplicant1ResponseDate()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, CLAIMANT_RESPONSE_SPEC.name());
            });
    }

    @Test
    void shouldSubmitSpecified1v2SameSolicitorFullAdmitClaimantResponseWithoutChangingState() throws Exception {
        CaseData caseData = specified1v2SameSolicitorFullAdmitClaimantResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(CLAIMANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getCcdState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION);
                assertThat(updatedData.getApplicant1AcceptFullAdmitPaymentPlanSpec()).isEqualTo(YesOrNo.YES);
                assertThat(updatedData.getApplicant1ResponseDate()).isNotNull();
                assertThat(updatedData.getPreviousCCDState()).isEqualTo(caseData.getCcdState());
                assertThat(updatedData.getNextDeadline()).isNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, CLAIMANT_RESPONSE_SPEC.name());
            });
    }

    @Test
    void shouldSubmitSpecified1v2SameSolicitorPartAdmitClaimantResponseWithoutChangingState() throws Exception {
        CaseData caseData = specified1v2SameSolicitorPartAdmitClaimantResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(CLAIMANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getCcdState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION);
                assertThat(updatedData.getApplicant1AcceptPartAdmitPaymentPlanSpec()).isEqualTo(YesOrNo.YES);
                assertThat(updatedData.getApplicant1ResponseDate()).isNotNull();
                assertThat(updatedData.getPreviousCCDState()).isEqualTo(caseData.getCcdState());
                assertThat(updatedData.getNextDeadline()).isNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, CLAIMANT_RESPONSE_SPEC.name());
            });
    }
}
