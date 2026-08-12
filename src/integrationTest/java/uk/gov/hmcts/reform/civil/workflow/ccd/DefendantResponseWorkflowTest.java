package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
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

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @MockBean(name = "deadlinesCalculator")
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setUpDefendantResponseWorkflowTest() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("user-id").build());
        when(deadlinesCalculator.calculateApplicantResponseDeadline(any(LocalDateTime.class)))
            .thenReturn(LocalDateTime.now().plusDays(14));
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
            .thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORTWO)))
            .thenReturn(false);

        LocationRefData courtLocation = new LocationRefData()
            .setEpimmsId("99999")
            .setSiteName("Court 99999")
            .setCourtAddress("1 Court Street")
            .setPostcode("SW1A 1AA")
            .setRegionId("4")
            .setRegion("London");
        when(locationRefDataService.getCourtLocationsForDefaultJudgments(anyString(), anyString()))
            .thenReturn(List.of(courtLocation));
        when(courtLocationUtils.findPreferredLocationData(any(), any(DynamicList.class))).thenReturn(courtLocation);
    }

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
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE.name());
                assertThat(updatedData.getRespondent1ClaimResponseDocument().getFile().getDocumentUrl()).isNull();
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
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNull();
                assertThat(updatedData.getNextDeadline()).isNull();
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
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNull();
                assertThat(updatedData.getNextDeadline()).isNull();
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
                assertThat(result.response().getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

                CaseData updatedData = result.caseData();
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
                assertThat(result.response().getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());

                CaseData updatedData = result.caseData();
                assertThat(updatedData.getRespondent1ResponseDate()).isNotNull();
                assertThat(updatedData.getApplicant1ResponseDeadline()).isNotNull();
                assertThat(updatedData.getNextDeadline()).isEqualTo(updatedData.getApplicant1ResponseDeadline().toLocalDate());
                assertThat(updatedData.getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(READY, DEFENDANT_RESPONSE_SPEC.name());
            });
    }
}
