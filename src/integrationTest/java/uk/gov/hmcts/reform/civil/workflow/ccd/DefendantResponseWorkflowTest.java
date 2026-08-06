package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specifiedFullAdmitResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specified1v2SameSolicitorFullAdmitResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specified1v2SameSolicitorPartAdmitResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.specifiedPartAdmitResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecified1v2DifferentSolicitorFirstFullDefenceResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecified1v2SameSolicitorFullDefenceResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecifiedFullAdmitResponse;
import static uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ResponseWorkflowFixtures.unspecifiedFullDefenceResponse;

@SuppressWarnings("java:S5960")
class DefendantResponseWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Test
    void shouldSubmitUnspecifiedFullDefenceAndSetClaimantResponseDeadline() throws Exception {
        CaseData caseData = unspecifiedFullDefenceResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseType()).isEqualTo(RespondentResponseType.FULL_DEFENCE);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isEqualTo(updatedData.getApplicant1ResponseDeadline().toLocalDate());
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE.name());
                assertThat(updatedData.getRespondent1ClaimResponseDocument()).isNull();
            });
    }

    @Test
    void shouldSubmitUnspecified1v2DifferentSolicitorFirstResponseAndWaitForSecondDefendant() throws Exception {
        CaseData caseData = unspecified1v2DifferentSolicitorFirstFullDefenceResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isNull();

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseType()).isEqualTo(RespondentResponseType.FULL_DEFENCE);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isEqualTo(caseData.getRespondent2ResponseDeadline().toLocalDate());
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE.name());
            });
    }

    @Test
    void shouldSubmitUnspecified1v2SameSolicitorFullDefenceForBothDefendants() throws Exception {
        CaseData caseData = unspecified1v2SameSolicitorFullDefenceResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseType()).isEqualTo(RespondentResponseType.FULL_DEFENCE);
                assertThat(updatedData.getRespondent2ClaimResponseType()).isEqualTo(RespondentResponseType.FULL_DEFENCE);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isEqualTo(updatedData.getApplicant1ResponseDeadline().toLocalDate());
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE.name());
            });
    }

    @Test
    void shouldSubmitUnspecifiedFullAdmitAndSetClaimantResponseDeadline() throws Exception {
        CaseData caseData = unspecifiedFullAdmitResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseType()).isEqualTo(RespondentResponseType.FULL_ADMISSION);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getClaimDismissedDeadline()).isNotNull();
                assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(DEFENDANT_RESPONSE.name());
            });
    }

    @Test
    void shouldSubmitSpecifiedPartAdmitAndMoveToAwaitingApplicantIntention() throws Exception {
        CaseData caseData = specifiedPartAdmitResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseTypeForSpec())
                    .isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
                assertThat(updatedData.getMultiPartyResponseTypeFlags())
                    .isEqualTo(MultiPartyResponseTypeFlags.PART_ADMISSION);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE_SPEC.name());
            });
    }

    @Test
    void shouldSubmitSpecifiedFullAdmitAndMoveToAwaitingApplicantIntention() throws Exception {
        CaseData caseData = specifiedFullAdmitResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseTypeForSpec())
                    .isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
                assertThat(updatedData.getMultiPartyResponseTypeFlags())
                    .isEqualTo(MultiPartyResponseTypeFlags.FULL_ADMISSION);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE_SPEC.name());
            });
    }

    @Test
    void shouldSubmitSpecified1v2SameSolicitorFullAdmitForBothDefendants() throws Exception {
        CaseData caseData = specified1v2SameSolicitorFullAdmitResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseTypeForSpec())
                    .isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
                assertThat(updatedData.getRespondent2ClaimResponseTypeForSpec())
                    .isEqualTo(RespondentResponseTypeSpec.FULL_ADMISSION);
                assertThat(updatedData.getMultiPartyResponseTypeFlags())
                    .isEqualTo(MultiPartyResponseTypeFlags.FULL_ADMISSION);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isEqualTo(updatedData.getApplicant1ResponseDeadline().toLocalDate());
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE_SPEC.name());
            });
    }

    @Test
    void shouldSubmitSpecified1v2SameSolicitorPartAdmitForBothDefendants() throws Exception {
        CaseData caseData = specified1v2SameSolicitorPartAdmitResponse();

        startWorkflow(caseData)
            .caseDataBefore(caseData)
            .eventId(DEFENDANT_RESPONSE_SPEC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ClaimResponseTypeForSpec())
                    .isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
                assertThat(updatedData.getRespondent2ClaimResponseTypeForSpec())
                    .isEqualTo(RespondentResponseTypeSpec.PART_ADMISSION);
                assertThat(updatedData.getMultiPartyResponseTypeFlags())
                    .isEqualTo(MultiPartyResponseTypeFlags.PART_ADMISSION);
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isEqualTo(updatedData.getApplicant1ResponseDeadline().toLocalDate());
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE_SPEC.name());
            });
    }
}
