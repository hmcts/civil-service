package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.listFromDynamicList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;

@SpringBootTest(classes = {
    ManageContactInformationCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    PostcodeValidator.class
})
class ManageContactInformationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ManageContactInformationCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private PostcodeValidator postcodeValidator;

    private static final UserInfo ADMIN_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-admin"))
        .uid("uid")
        .build();
    private static final UserInfo LEGAL_REP_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-solicitor"))
        .uid("uid")
        .build();

    @BeforeEach
    void setUp() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Nested
    class AboutToStart {

        @Test
        void shouldNotReturnReturnErrors_WhenAboutToStartIsInvokedByAdminUserWhileCaseInAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            CaseData caseData = CaseData.builder()
                .applicant1(Party.builder()
                                .type(COMPANY)
                                .companyName("Test Inc")
                                .build())
                .respondent1(Party.builder()
                                .type(COMPANY)
                                .companyName("Test Inc")
                                .build())
                .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
                .ccdCaseReference(123L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertNull(response.getErrors());
        }

        @Test
        void shouldReturnErrors_WhenAboutToStartIsInvokedByNonAdminUserWhileCaseInAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseData = CaseData.builder().ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
                .ccdCaseReference(123L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected =
                List.of("You will be able run the manage contact information event once the claimant has responded.");

            assertEquals(expected, response.getErrors());
        }

        @Test
        void shouldNotReturnErrors_WhenAboutToStartIsInvokedByNonAdminUserWhileCaseInANonAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseData = CaseData.builder().ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertNull(response.getErrors());
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsAdmin() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("random role"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addApplicant1LitigationFriend()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of("CLAIMANT 1: Mr. John Rambo",
                                                                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                                                                "CLAIMANT 1: Individuals attending for the legal representative",
                                                                "CLAIMANT 1: Witnesses",
                                                                "CLAIMANT 1: Experts",
                                                                "DEFENDANT 1: Test Inc",
                                                                "DEFENDANT 1: Individuals attending for the organisation",
                                                                "DEFENDANT 1: Individuals attending for the legal representative",
                                                                "DEFENDANT 1: Witnesses",
                                                                "DEFENDANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addApplicant1LitigationFriend()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsRespondentSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[RESPONDENTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addApplicant1LitigationFriend()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsAdmin() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("random role"));
            CaseData caseData = CaseDataBuilder.builder()
                .addApplicant1LitigationFriend()
                .addApplicant2LitigationFriend()
                .multiPartyClaimTwoApplicants()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 2: Mr. Jason Rambo",
                "CLAIMANT 2: Litigation Friend: Applicant Two Litigation Friend",
                "CLAIMANTS: Individuals attending for the legal representative",
                "CLAIMANTS: Witnesses",
                "CLAIMANTS: Experts",
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addApplicant1LitigationFriend()
                .addApplicant2LitigationFriend()
                .multiPartyClaimTwoApplicants()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 2: Mr. Jason Rambo",
                "CLAIMANT 2: Litigation Friend: Applicant Two Litigation Friend",
                "CLAIMANTS: Individuals attending for the legal representative",
                "CLAIMANTS: Witnesses",
                "CLAIMANTS: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsRespondentSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[RESPONDENTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addApplicant1LitigationFriend()
                .addApplicant2LitigationFriend()
                .multiPartyClaimTwoApplicants()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsAdmin() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("random role"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts",
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 2: Mr. John Rambo",
                "DEFENDANTS: Individuals attending for the legal representative",
                "DEFENDANTS: Witnesses",
                "DEFENDANTS: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsRespondentSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[RESPONDENTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 2: Mr. John Rambo",
                "DEFENDANT 2: Litigation Friend: Litigation Friend",
                "DEFENDANTS: Individuals attending for the legal representative",
                "DEFENDANTS: Witnesses",
                "DEFENDANTS: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsAdmin() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("random role"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
                .respondent2DQ()
                .respondent2Represented(YES)
                .addApplicant1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts",
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts",
                "DEFENDANT 2: Mr. John Rambo",
                "DEFENDANT 2: Litigation Friend: Litigation Friend",
                "DEFENDANT 2: Individuals attending for the legal representative",
                "DEFENDANT 2: Witnesses",
                "DEFENDANT 2: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
                .respondent2DQ()
                .respondent2Represented(YES)
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent2ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsRespondentSolicitorOne() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[RESPONDENTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
                .respondent2DQ()
                .respondent2Represented(YES)
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent2ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsRespondentSolicitorTwo() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("[RESPONDENTSOLICITORTWO]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2Responds(RespondentResponseType.FULL_DEFENCE)
                .respondent2DQ()
                .respondent2Represented(YES)
                .addApplicant1ExpertsAndWitnesses()
                .addRespondent1ExpertsAndWitnesses()
                .addRespondent2ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder()
                .ccdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION)
                .ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 2: Mr. John Rambo",
                "DEFENDANT 2: Litigation Friend: Litigation Friend",
                "DEFENDANT 2: Individuals attending for the legal representative",
                "DEFENDANT 2: Witnesses",
                "DEFENDANT 2: Experts");

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class AboutToSubmit {
        UpdatePartyDetailsForm party;
        Expert dqExpert;
        Expert expectedExpert1;
        Witness dqWitness;
        Witness expectedWitness1;

        @BeforeEach
        void setup() {
            party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").build();
            dqExpert = Expert.builder().partyID("id").firstName("dq").lastName("dq").build();
            expectedExpert1 = dqExpert.builder().firstName("First").lastName("Name")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
                .partyID(null) //change this for CIV-10382
                .build();
            dqWitness = Witness.builder().firstName("dq").lastName("dq").partyID("id").build();
            expectedWitness1 = Witness.builder().firstName("First").lastName("Name")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
                .partyID(null).build(); // CIV-10382
        }

        @Test
        void shouldUpdateApplicantOneExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(CLAIMANT_ONE_EXPERTS_ID)
                                       .updateExpertsDetailsForm(wrapElements(party))
                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder().details(wrapElements(dqExpert)).build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0)).isEqualTo(expectedExpert1);
        }

        @Test
        void shouldUpdateDefendantOneExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(DEFENDANT_ONE_EXPERTS_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(DEFENDANT_ONE_EXPERTS_ID)
                                       .updateExpertsDetailsForm(wrapElements(party))
                                       .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder().details(wrapElements(dqExpert)).build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0)).isEqualTo(expectedExpert1);
        }

        @Test
        void shouldUpdateDefendantTwoExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(DEFENDANT_TWO_EXPERTS_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(DEFENDANT_TWO_EXPERTS_ID)
                                       .updateExpertsDetailsForm(wrapElements(party))
                                       .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQExperts(Experts.builder().details(wrapElements(dqExpert)).build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0)).isEqualTo(expectedExpert1);
        }

        @Test
        void shouldUpdateApplicantOneWitnesses() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(CLAIMANT_ONE_WITNESSES_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(CLAIMANT_ONE_WITNESSES_ID)
                                       .updateWitnessesDetailsForm(wrapElements(party))
                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()).get(0)).isEqualTo(expectedWitness1);
        }

        @Test
        void shouldUpdateDefendantOneWitnesses() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(DEFENDANT_ONE_WITNESSES_ID)
                                       .updateWitnessesDetailsForm(wrapElements(party))
                                       .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0)).isEqualTo(expectedWitness1);
        }

        @Test
        void shouldUpdateDefendantTwoWitnesses() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(DEFENDANT_TWO_WITNESSES_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(DEFENDANT_TWO_WITNESSES_ID)
                                       .updateWitnessesDetailsForm(wrapElements(party))
                                       .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0)).isEqualTo(expectedWitness1);
        }

        @Test
        void addingExpertWhenNoneExisted() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(CLAIMANT_ONE_EXPERTS_ID)
                                       .updateExpertsDetailsForm(wrapElements(party))
                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(NO)
                                                           .build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0)).isEqualTo(expectedExpert1);
            assertThat(updatedData.getApplicant1DQ().getApplicant1DQExperts().getExpertRequired()).isEqualTo(YES);
        }

        @Test
        void removingAllExperts() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(CLAIMANT_ONE_EXPERTS_ID)
                                       .updateExpertsDetailsForm(null)
                                       .build())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder()
                                                           .expertRequired(YES)
                                                           .details(wrapElements(dqExpert)).build())
                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQExperts().getDetails())).isEmpty();
            assertThat(updatedData.getApplicant1DQ().getApplicant1DQExperts().getExpertRequired()).isEqualTo(NO);
        }

        @Test
        void addingWitnessWhenNoneExisted() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(DEFENDANT_ONE_WITNESSES_ID)
                                       .updateWitnessesDetailsForm(wrapElements(party))
                                       .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0)).isEqualTo(expectedWitness1);
            assertThat(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getWitnessesToAppear()).isEqualTo(YES);
        }

        @Test
        void removingAllWitnesses() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                   .build())
                                                        .build())
                                       .partyChosenId(DEFENDANT_ONE_WITNESSES_ID)
                                       .updateWitnessesDetailsForm(null)
                                       .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(Witnesses.builder()
                                                               .details(wrapElements(dqWitness))
                                                               .witnessesToAppear(YES).build())
                                   .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails())).isEmpty();
            assertThat(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getWitnessesToAppear()).isEqualTo(NO);
        }
    }

    @Nested
    class MidShowWarning {
        private static final String PAGE_ID = "show-warning";

        @ParameterizedTest
        @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID, DEFENDANT_ONE_ID, DEFENDANT_TWO_ID})
        void shouldReturnWarning(String partyChosenId) {
            String errorTitle = "Check the litigation friend's details";
            String errorMessage = "After making these changes, please ensure that the "
                + "litigation friend's contact information is also up to date.";

            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(partyChosenId)
                                                                   .build())
                                                        .build())
                                       .build())
                .addApplicant1LitigationFriend()
                .addApplicant2LitigationFriend()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).contains(errorTitle);
            assertThat(response.getWarnings()).contains(errorMessage);
        }

        @ParameterizedTest
        @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID, DEFENDANT_ONE_ID, DEFENDANT_TWO_ID, DEFENDANT_ONE_LITIGATION_FRIEND_ID})
        void shouldNotReturnWarning(String partyChosenId) {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(partyChosenId)
                                                                   .build())
                                                        .build())
                                       .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getWarnings()).isEmpty();
        }

        @Test
        void shouldReturnPostcodeError() {
            given(postcodeValidator.validate(any())).willReturn(List.of("Please enter Postcode"));

            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(CLAIMANT_ONE_ID)
                                                                   .build())
                                                        .build())
                                       .build())
                .applicant1(Party.builder()
                                .type(INDIVIDUAL)
                                .primaryAddress(Address.builder()
                                                    .postCode(null)
                                                    .build())
                                .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertEquals(1, response.getErrors().size());
            assertEquals("Please enter Postcode", response.getErrors().get(0));
        }
    }

    @Nested
    class MidShowPartyField {
        private static final String PAGE_ID = "show-party-field";

        @Test
        void shouldPopulatePartyChosenId() {
            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code("CODE")
                                                                   .build())
                                                        .build())
                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo("CODE");
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
            assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
            assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID, DEFENDANT_ONE_ID, DEFENDANT_TWO_ID})
        void shouldPopulatePartyType(String partyChosenId) {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            CaseData caseDataBefore = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(INDIVIDUAL).build())
                .applicant2(Party.builder().type(INDIVIDUAL).build())
                .respondent1(Party.builder().type(INDIVIDUAL).build())
                .respondent2(Party.builder().type(INDIVIDUAL).build())
                .buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(partyChosenId)
                                                                   .build())
                                                        .build())
                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(partyChosenId + "_ADMIN_INDIVIDUAL");
            assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
            assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {CLAIMANT_ONE_LITIGATION_FRIEND_ID, CLAIMANT_TWO_LITIGATION_FRIEND_ID, DEFENDANT_ONE_LITIGATION_FRIEND_ID, DEFENDANT_TWO_LITIGATION_FRIEND_ID})
        void shouldPopulatePartyTypeForLitigationFriend(String partyChosenId) {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseDataBefore = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(INDIVIDUAL).build())
                .applicant2(Party.builder().type(INDIVIDUAL).build())
                .respondent1(Party.builder().type(INDIVIDUAL).build())
                .respondent2(Party.builder().type(INDIVIDUAL).build())
                .buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            CaseData caseData = CaseDataBuilder.builder()
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(partyChosenId)
                                                                   .build())
                                                        .build())
                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(partyChosenId + "_LR");
            assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
            assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {CLAIMANT_ONE_EXPERTS_ID, DEFENDANT_ONE_EXPERTS_ID, DEFENDANT_TWO_EXPERTS_ID})
        void shouldPopulateExperts(String partyChosenId) {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            Expert expert = Expert.builder().firstName("First").lastName("Name").partyID("id").build();
            UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
                .partyId("id").build();
            List<Element<UpdatePartyDetailsForm>> form = wrapElements(party);

            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQExperts(Experts.builder().details(wrapElements(expert)).build())
                                  .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQExperts(Experts.builder().details(wrapElements(expert)).build())
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQExperts(Experts.builder().details(wrapElements(expert)).build())
                                   .build())
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(partyChosenId)
                                                                   .build())
                                                        .build())
                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
            assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEqualTo(form);
            assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();

        }

        @ParameterizedTest
        @ValueSource(strings = {CLAIMANT_ONE_WITNESSES_ID, DEFENDANT_ONE_WITNESSES_ID, DEFENDANT_TWO_WITNESSES_ID})
        void shouldPopulateWitnesses(String partyChosenId) {
            Witness witness = Witness.builder().firstName("First").lastName("Name").partyID("id").build();
            UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
                .partyId("id").build();
            List<Element<UpdatePartyDetailsForm>> form = wrapElements(party);

            CaseData caseData = CaseDataBuilder.builder()
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQWitnesses(Witnesses.builder().details(wrapElements(witness)).build())
                                  .build())
                .respondent1DQ(Respondent1DQ.builder()
                                   .respondent1DQWitnesses(Witnesses.builder().details(wrapElements(witness)).build())
                                   .build())
                .respondent2DQ(Respondent2DQ.builder()
                                   .respondent2DQWitnesses(Witnesses.builder().details(wrapElements(witness)).build())
                                   .build())
                .updateDetailsForm(UpdateDetailsForm.builder()
                                       .partyChosen(DynamicList.builder()
                                                        .value(DynamicListElement.builder()
                                                                   .code(partyChosenId)
                                                                   .build())
                                                        .build())
                                       .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
            assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
            assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEqualTo(form);

        }
    }
}
