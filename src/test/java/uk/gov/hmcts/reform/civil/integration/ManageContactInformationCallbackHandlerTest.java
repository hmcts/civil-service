package uk.gov.hmcts.reform.civil.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.ManageContactInformationCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.PrepareEventTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.ShowWarningTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.SubmitChangesTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.ValidateExpertsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.ValidateWitnessesTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.PartyDetailsChangedUtil;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.listFromDynamicList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;

@SpringBootTest(classes = {
    ManageContactInformationCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    PostcodeValidator.class,
    PartyValidator.class,
    PrepareEventTask.class,
    ShowWarningTask.class,
    SubmitChangesTask.class,
    ValidateExpertsTask.class,
    ValidateWitnessesTask.class
})
@SuppressWarnings("unchecked")
class ManageContactInformationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ManageContactInformationCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private PartyDetailsChangedUtil partyDetailsChangedUtil;

    @MockBean
    private CaseFlagsInitialiser caseFlagInitialiser;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

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
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1(Party.builder()
                                .type(COMPANY)
                                .companyName("Test Inc")
                                .build());
            caseData.setRespondent1(Party.builder()
                        .type(COMPANY)
                        .companyName("Test Inc")
                        .build());
            PartyFlagStructure claimantOrgIndividual = new PartyFlagStructure();
            claimantOrgIndividual.setFirstName("Claimant");
            claimantOrgIndividual.setLastName("OrgIndividual");
            claimantOrgIndividual.setEmail("claiamnt-orgindividual@example.com");
            claimantOrgIndividual.setPhone("07867654543");
            claimantOrgIndividual.setPartyID("party-id");
            caseData.setApplicant1OrgIndividuals(wrapElements(List.of(claimantOrgIndividual)));

            PartyFlagStructure defendantOrgIndividual = new PartyFlagStructure();
            defendantOrgIndividual.setFirstName("Defendant");
            defendantOrgIndividual.setLastName("OrgIndividual");
            defendantOrgIndividual.setEmail("defendant-orgindividual@example.com");
            defendantOrgIndividual.setPhone("07867654543");
            defendantOrgIndividual.setPartyID("party-id");
            caseData.setRespondent1OrgIndividuals(wrapElements(List.of(defendantOrgIndividual)));
            caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
            caseData.setCcdCaseReference(123L);
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertNull(response.getErrors());
        }

        @ParameterizedTest
        @EnumSource(value = CaseState.class, names = {"AWAITING_RESPONDENT_ACKNOWLEDGEMENT", "AWAITING_APPLICANT_INTENTION"})
        void shouldReturnErrors_WhenAboutToStartIsInvokedByNonAdminUserWhileCaseInAwaitingRespondentAcknowledgementState(CaseState states) {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdState(states);
            caseData.setCcdCaseReference(123L);
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected =
                List.of("You will be able to run the manage contact information event once the claimant has responded.");

            assertEquals(expected, response.getErrors());
        }

        @Test
        void shouldNotReturnErrors_WhenAboutToStartIsInvokedByNonAdminUserWhileCaseInANonAwaitingApplicantIntentionState() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);
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
                .applicant1Represented(YES)
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

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
                "DEFENDANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsAdmin_lipClaimant() {
            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("random role"));
            CaseData caseData = CaseDataBuilder.builder()
                    .addRespondent1LitigationFriend()
                    .addApplicant1LitigationFriend()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .addApplicant1ExpertsAndWitnesses()
                    .applicant1Represented(NO)
                    .respondent1(Party.builder()
                            .type(COMPANY)
                            .companyName("Test Inc")
                            .build())
                    .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            List<String> expected = List.of(
                    "CLAIMANT 1: Mr. John Rambo",
                    "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                    "CLAIMANT 1: Witnesses",
                    "CLAIMANT 1: Experts",
                    "DEFENDANT 1: Test Inc",
                    "DEFENDANT 1: Individuals attending for the organisation",
                    "DEFENDANT 1: Individuals attending for the legal representative",
                    "DEFENDANT 1: Witnesses",
                    "DEFENDANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addApplicant1LitigationFriend()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1ExpertsAndWitnesses()
                .applicant1Represented(YES)
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsRespondentSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[RESPONDENTSOLICITORONE]"));
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts"
            );

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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

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
                "DEFENDANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[APPLICANTSOLICITORONE]"));
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

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
                "CLAIMANTS: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsRespondentSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[RESPONDENTSOLICITORONE]"));
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts"
            );

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
                .applicant1Represented(YES)
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

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
                "DEFENDANTS: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .applicant1Represented(YES)
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsRespondentSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[RESPONDENTSOLICITORONE]"));
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

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
                "DEFENDANTS: Experts"
            );

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
                .applicant1Represented(YES)
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

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
                "DEFENDANT 2: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsApplicantSolicitor() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[APPLICANTSOLICITORONE]"));
            CaseData caseData = CaseDataBuilder.builder()
                .addRespondent1LitigationFriend()
                .addRespondent2LitigationFriend()
                .addApplicant1LitigationFriend()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1Represented(YES)
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "CLAIMANT 1: Mr. John Rambo",
                "CLAIMANT 1: Litigation Friend: Applicant Litigation Friend",
                "CLAIMANT 1: Individuals attending for the legal representative",
                "CLAIMANT 1: Witnesses",
                "CLAIMANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsRespondentSolicitorOne() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[RESPONDENTSOLICITORONE]"));
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 1: Test Inc",
                "DEFENDANT 1: Individuals attending for the organisation",
                "DEFENDANT 1: Individuals attending for the legal representative",
                "DEFENDANT 1: Witnesses",
                "DEFENDANT 1: Experts"
            );

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            List<String> actual = listFromDynamicList(updatedData.getUpdateDetailsForm().getPartyChosen());

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsRespondentSolicitorTwo() {
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(
                "[RESPONDENTSOLICITORTWO]"));
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
                .build();
            caseData.setCcdState(CaseState.AWAITING_CASE_DETAILS_NOTIFICATION);
            caseData.setCcdCaseReference(123L);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            List<String> expected = List.of(
                "DEFENDANT 2: Mr. John Rambo",
                "DEFENDANT 2: Litigation Friend: Litigation Friend",
                "DEFENDANT 2: Individuals attending for the legal representative",
                "DEFENDANT 2: Witnesses",
                "DEFENDANT 2: Experts"
            );

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
        PartyFlagStructure expectedExpertFlags;
        PartyFlagStructure expectedWitnessFlags;

        private static final String PARTY_ID = "party-id";
        private static final ContactDetailsUpdatedEvent EVENT;

        static {
            ContactDetailsUpdatedEvent event = new ContactDetailsUpdatedEvent();
            event.setSummary("Summary");
            event.setDescription("Description");
            EVENT = event;
        }

        private static MockedStatic partyIdMock;

        @BeforeAll
        static void setupSuite() {
            partyIdMock = mockStatic(PartyUtils.class, Mockito.CALLS_REAL_METHODS);
            partyIdMock.when(PartyUtils::createPartyId).thenReturn(PARTY_ID);
        }

        @AfterAll
        static void tearDown() {
            partyIdMock.reset();
            partyIdMock.close();
        }

        @BeforeEach
        void setup() {
            party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").build();
            dqExpert = Expert.builder().partyID("id").firstName("dq").lastName("dq").build();
            expectedExpert1 = dqExpert.builder().firstName("First").lastName("Name")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
                .partyID(PARTY_ID)
                .build();
            expectedExpertFlags = new PartyFlagStructure();
            expectedExpertFlags.setPartyID(PARTY_ID);
            expectedExpertFlags.setFirstName("First");
            expectedExpertFlags.setLastName("Name");
            dqWitness = Witness.builder().firstName("dq").lastName("dq").partyID("id").build();
            expectedWitness1 = Witness.builder().firstName("First").lastName("Name")
                .eventAdded("Manage Contact Information Event").dateAdded(LocalDate.now())
                .partyID(PARTY_ID).build();
            expectedWitnessFlags = new PartyFlagStructure();
            expectedWitnessFlags.setPartyID(PARTY_ID);
            expectedWitnessFlags.setFirstName("First");
            expectedWitnessFlags.setLastName("Name");

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .applicant1(Party.builder().type(INDIVIDUAL).build())
                .applicant2(Party.builder().type(INDIVIDUAL).build())
                .respondent1(Party.builder().type(INDIVIDUAL).build())
                .respondent2(Party.builder().type(INDIVIDUAL).build())
                .buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);
        }

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(MANAGE_CONTACT_INFORMATION);
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withPartyChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder()
                .respondent1(Party.builder()
                                 .individualFirstName("Dis")
                                 .individualLastName("Guy")
                                 .type(INDIVIDUAL).flags(respondent1Flags).build())
                .buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .code(DEFENDANT_ONE_ID)
                                                            .build())
                                                 .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_ID);

            CaseData updated = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .updateDetailsForm(updateDetailsForm)
                .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(partyDetailsChangedUtil.buildChangesEvent(any(CaseData.class), any(CaseData.class))).thenReturn(
                EVENT);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(updated.getApplicant1(), responseData.getApplicant1());
            assertEquals(MANAGE_CONTACT_INFORMATION.name(), responseData.getBusinessProcess().getCamundaEvent());
            assertEquals(READY, responseData.getBusinessProcess().getStatus());
            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent();
            expectedEvent.setSummary(EVENT.getSummary());
            expectedEvent.setDescription(EVENT.getDescription());
            expectedEvent.setSubmittedByCaseworker(YES);
            assertEquals(expectedEvent, responseData.getContactDetailsUpdatedEvent());
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withPartyChangesWithGaData() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1(Party.builder()
                                 .individualFirstName("Dis")
                                 .individualLastName("Guy")
                                 .type(INDIVIDUAL).flags(respondent1Flags).build())
                .buildClaimIssuedPaymentCaseData();

            CaseData caseDataBefore = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseData(caseData);

            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .code(DEFENDANT_ONE_ID)
                                                            .build())
                                                 .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_ID);

            CaseData updated = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .updateDetailsForm(updateDetailsForm)
                .build();

            CaseData updatedCaseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseData(updated);

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);
            when(partyDetailsChangedUtil.buildChangesEvent(any(CaseData.class), any(CaseData.class))).thenReturn(
                EVENT);
            when(coreCaseDataService.triggerGeneralApplicationEvent(anyLong(), any(CaseEvent.class), anyMap())).thenReturn(updatedCaseData);
            CallbackParams params = callbackParamsOf(updatedCaseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(updated.getApplicant1(), responseData.getApplicant1());
            assertEquals(MANAGE_CONTACT_INFORMATION.name(), responseData.getBusinessProcess().getCamundaEvent());
            assertEquals(READY, responseData.getBusinessProcess().getStatus());
            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent();
            expectedEvent.setSummary(EVENT.getSummary());
            expectedEvent.setDescription(EVENT.getDescription());
            expectedEvent.setSubmittedByCaseworker(YES);
            assertEquals(expectedEvent, responseData.getContactDetailsUpdatedEvent());
            verify(coreCaseDataService).triggerGeneralApplicationEvent(eq(1234L), eq(CaseEvent.UPDATE_GA_CASE_DATA), anyMap());
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withClaimantLRIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Claimant");
            expected.setLastName("LRIndividual");
            expected.setEmail("claiamnt-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateLRIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getApplicant1LRIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withDefendant1LRIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Defendant1");
            expected.setLastName("LRIndividual");
            expected.setEmail("defendant1-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateLRIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getRespondent1LRIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withDefendant2LRIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Defendant2");
            expected.setLastName("LRIndividual");
            expected.setEmail("defendant2-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateLRIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getRespondent2LRIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withClaimantOrgIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Claimant1");
            expected.setLastName("OrgIndividual");
            expected.setEmail("claiamnt-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateOrgIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(CLAIMANT_ONE_ORG_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_ORG_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getApplicant1OrgIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withClaimant2OrgIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Claimant2");
            expected.setLastName("OrgIndividual");
            expected.setEmail("claiamnt2-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateOrgIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(CLAIMANT_TWO_ORG_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(CLAIMANT_TWO_ORG_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getApplicant2OrgIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withDefendant1OrgIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Defendant1");
            expected.setLastName("OrgIndividual");
            expected.setEmail("defendant1-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateOrgIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(DEFENDANT_ONE_ORG_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_ORG_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getRespondent1OrgIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByAdmin_withDefendant2OrgIndividualChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder().respondent1(Party.builder()
                    .individualFirstName("Dis")
                    .individualLastName("Guy")
                    .type(INDIVIDUAL).flags(respondent1Flags).build()).buildClaimIssuedPaymentCaseData();
            given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

            PartyFlagStructure expected = new PartyFlagStructure();
            expected.setFirstName("Defendant2");
            expected.setLastName("OrgIndividual");
            expected.setEmail("defendant2-lrindividual@example.com");
            expected.setPhone("07867654543");
            expected.setPartyID("party-id");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setUpdateOrgIndividualsForm(wrapElements(UpdatePartyDetailsForm.builder()
                                    .firstName(expected.getFirstName())
                                    .lastName(expected.getLastName())
                                    .emailAddress(expected.getEmail())
                                    .phoneNumber(expected.getPhone())
                                    .build()
                            ));
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                            .code(DEFENDANT_TWO_ORG_INDIVIDUALS_ID)
                                            .build())
                                    .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_TWO_ORG_INDIVIDUALS_ID);

            CaseData updated = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

            when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(expected, unwrapElements(responseData.getRespondent2OrgIndividuals()).get(0));
        }

        @Test
        void shouldReturnExpectedResponseCaseData_whenTriggeredByNonAdmin_withPartyChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            CaseData caseDataBefore = CaseDataBuilder.builder()
                .respondent1(Party.builder()
                                 .individualFirstName("Dis")
                                 .individualLastName("Guy")
                                 .type(INDIVIDUAL).flags(respondent1Flags).build())
                .buildClaimIssuedPaymentCaseData();

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .code(DEFENDANT_ONE_ID)
                                                            .build())
                                                 .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_ID);

            CaseData updated = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoDefendantSolicitors()
                .updateDetailsForm(updateDetailsForm)
                .build();

            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseDataBefore);
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);
            when(partyDetailsChangedUtil.buildChangesEvent(any(CaseData.class), any(CaseData.class))).thenReturn(
                EVENT);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertEquals(updated.getApplicant1(), responseData.getApplicant1());
            assertEquals(MANAGE_CONTACT_INFORMATION.name(), responseData.getBusinessProcess().getCamundaEvent());
            assertEquals(READY, responseData.getBusinessProcess().getStatus());
            ContactDetailsUpdatedEvent expectedEvent = new ContactDetailsUpdatedEvent();
            expectedEvent.setSummary(EVENT.getSummary());
            expectedEvent.setDescription(EVENT.getDescription());
            expectedEvent.setSubmittedByCaseworker(NO);
            assertEquals(expectedEvent, responseData.getContactDetailsUpdatedEvent());
        }

        @Test
        void shouldNotSetEventDetailsOrChangeBusinessProcessCallbackResponse_withNoPartyChanges() {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Party respondent = Party.builder()
                .individualFirstName("Dis")
                .individualLastName("Guy")
                .type(INDIVIDUAL).flags(respondent1Flags).build();

            CaseData caseDataBefore = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            Party respondentWithFlags = Party.builder()
                .individualFirstName(respondent.getIndividualFirstName())
                .individualLastName(respondent.getIndividualLastName())
                .type(respondent.getType())
                .flags(respondent1Flags)
                .build();
            caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondentWithFlags);
            caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            caseDataBefore.setCaseNamePublic("John Rambo v Dis Guy");

            CaseData updated = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1(respondent).build();
            updated.setRespondent1DetailsForClaimDetailsTab(respondentWithFlags);
            updated.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
            updated.setCaseNamePublic("John Rambo v Dis Guy");

            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .code(DEFENDANT_ONE_ID)
                                                            .build())
                                                 .build());
            updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_ID);
            updated.setUpdateDetailsForm(updateDetailsForm);

            when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseDataBefore);
            when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

            CallbackParams params = callbackParamsOf(updated, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

            assertNull(responseData.getContactDetailsUpdatedEvent());
            assertEquals(caseDataBefore.getBusinessProcess(), responseData.getBusinessProcess());
        }

        @Test
        void shouldUpdateInternalCaseName() {
            UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
            updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                 .value(DynamicListElement.builder()
                                                            .code(CLAIMANT_ONE_ID)
                                                            .build())
                                                 .build());
            updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_ID);
            updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .addApplicant1LitigationFriend()
                .updateDetailsForm(updateDetailsForm)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getCaseNameHmctsInternal())
                .isEqualTo("John Rambo represented by Applicant Litigation Friend (litigation friend) " +
                               "v Sole Trader");
        }

        @Nested
        class RetainFlags {
            Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
            Flags respondent2Flags = Flags.builder().partyName("respondent2name").roleOnCase("respondent2").build();
            Flags applicant1Flags = Flags.builder().partyName("applicant1name").roleOnCase("applicant1").build();
            Flags applicant2Flags = Flags.builder().partyName("applicant2name").roleOnCase("applicant2").build();

            CaseData caseDataBefore = CaseData.builder()
                .applicant1(Party.builder().flags(applicant1Flags).build())
                .applicant2(Party.builder().flags(applicant2Flags).build())
                .respondent1(Party.builder().flags(respondent1Flags).build())
                .respondent2(Party.builder().flags(respondent2Flags).build())
                .build();

            @BeforeEach
            void setup() {
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseDataBefore);
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID})
            void shouldCopyFlagsForApplicants(String partyChosenId) {
                // Fix: Include all parties with their types to avoid NullPointerException
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(partyChosenId)
                                .build())
                            .build());
                updateDetailsForm.setPartyChosenId(partyChosenId);

                CaseData caseData = CaseData.builder()
                    .applicant1(Party.builder()
                        .type(INDIVIDUAL)  // Using static import
                        .individualFirstName("John")
                        .individualLastName("Doe")
                        .flags(applicant1Flags)
                        .build())
                    .applicant2(Party.builder()
                        .type(COMPANY)  // Using static import
                        .companyName("Test Company")
                        .flags(applicant2Flags)
                        .build())
                    .respondent1(Party.builder()
                        .type(INDIVIDUAL)  // Using static import
                        .individualFirstName("Jane")
                        .individualLastName("Smith")
                        .flags(respondent1Flags)
                        .build())
                    .respondent2(Party.builder()
                        .type(COMPANY)  // Using static import
                        .companyName("Defendant Corp")
                        .flags(respondent2Flags)
                        .build())
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(responseCaseData.getApplicant1().getFlags()).isEqualTo(applicant1Flags);
                assertThat(responseCaseData.getApplicant2().getFlags()).isEqualTo(applicant2Flags);
                // Also verify respondent flags are preserved
                assertThat(responseCaseData.getRespondent1().getFlags()).isEqualTo(respondent1Flags);
                assertThat(responseCaseData.getRespondent2().getFlags()).isEqualTo(respondent2Flags);
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_ID, DEFENDANT_TWO_ID})
            void shouldCopyFlagsForRespondents(String partyChosenId) {
                // Fix: Include all parties with their types to avoid NullPointerException
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(partyChosenId)
                                .build())
                            .build());
                updateDetailsForm.setPartyChosenId(partyChosenId);

                CaseData caseData = CaseData.builder()
                    .applicant1(Party.builder()
                        .type(INDIVIDUAL)  // Using static import
                        .individualFirstName("John")
                        .individualLastName("Doe")
                        .flags(applicant1Flags)
                        .build())
                    .applicant2(Party.builder()
                        .type(COMPANY)  // Using static import
                        .companyName("Test Company")
                        .flags(applicant2Flags)
                        .build())
                    .respondent1(Party.builder()
                        .type(INDIVIDUAL)  // Using static import
                        .individualFirstName("Jane")
                        .individualLastName("Smith")
                        .flags(respondent1Flags)
                        .build())
                    .respondent2(Party.builder()
                        .type(COMPANY)  // Using static import
                        .companyName("Defendant Corp")
                        .flags(respondent2Flags)
                        .build())
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);
                // Also verify applicant flags are preserved
                assertThat(responseCaseData.getApplicant1().getFlags()).isEqualTo(applicant1Flags);
                assertThat(responseCaseData.getApplicant2().getFlags()).isEqualTo(applicant2Flags);
                assertThat(responseCaseData.getRespondent1().getFlags()).isEqualTo(respondent1Flags);
                assertThat(responseCaseData.getRespondent2().getFlags()).isEqualTo(respondent2Flags);
            }

            @Test
            void shouldUpdateApplicantOneExperts() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                Experts applicant1Experts = new Experts();
                applicant1Experts.setDetails(wrapElements(dqExpert));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQExperts(applicant1Experts)
                                      .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getApplicantExperts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateDefendantOneExperts() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                Experts respondent1Experts = new Experts();
                respondent1Experts.setDetails(wrapElements(dqExpert));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQExperts(respondent1Experts)
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getRespondent1Experts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateDefendantOneExperts_WhenNoExpertsExisted() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQExperts(new Experts())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getRespondent1Experts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateDefendantTwoExperts() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_TWO_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_TWO_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                Experts respondent2Experts = new Experts();
                respondent2Experts.setDetails(wrapElements(dqExpert));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQExperts(respondent2Experts)
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getRespondent2Experts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateBothApplicantExperts() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                Experts applicant1ExpertsMulti = new Experts();
                applicant1ExpertsMulti.setDetails(wrapElements(dqExpert));
                Experts applicant2Experts = new Experts();
                applicant2Experts.setDetails(wrapElements(dqExpert));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(YES)
                    .applicant2ProceedWithClaimMultiParty2v1(YES)
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQExperts(applicant1ExpertsMulti)
                                      .build())
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQExperts(applicant2Experts)
                                      .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getApplicant2DQ().getApplicant2DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getApplicantExperts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateBothRespondentExperts() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                Experts respondent1ExpertsMulti = new Experts();
                respondent1ExpertsMulti.setDetails(wrapElements(dqExpert));
                Experts respondent2ExpertsMulti = new Experts();
                respondent2ExpertsMulti.setDetails(wrapElements(dqExpert));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimOneDefendantSolicitor()
                    .respondentResponseIsSame(YES)
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQExperts(respondent1ExpertsMulti)
                                       .build())
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQExperts(respondent2ExpertsMulti)
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getRespondent1Experts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateDefendantTwoExperts_WhenNoExpertsExisted() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_TWO_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_TWO_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQExperts(new Experts())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(unwrapElements(updatedData.getRespondent2Experts()).get(0)).isEqualTo(expectedExpertFlags);
            }

            @Test
            void shouldUpdateApplicantOneWitnesses() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(YES)
                    .applicant2ProceedWithClaimMultiParty2v1(YES)
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                      .build())
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                      .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getApplicantWitnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void shouldUpdateDefendantOneWitnesses() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                       .build())
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getRespondent1Witnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void shouldUpdateDefendantTwoWitnesses() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_TWO_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_TWO_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getRespondent2Witnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void shouldUpdateBothApplicantWitnesses() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimTwoApplicants()
                    .applicant1ProceedWithClaimMultiParty2v1(YES)
                    .applicant2ProceedWithClaimMultiParty2v1(YES)
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                      .build())
                    .applicant2DQ(Applicant2DQ.builder()
                                      .applicant2DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                      .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getApplicant2DQ().getApplicant2DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getApplicantWitnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void shouldUpdateBothRespondentWitnesses() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .multiPartyClaimOneDefendantSolicitor()
                    .respondentResponseIsSame(YES)
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                       .build())
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQWitnesses(Witnesses.builder().details(wrapElements(dqWitness)).build())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getRespondent1Witnesses()).get(0)).isEqualTo(expectedWitnessFlags);
                assertThat(unwrapElements(updatedData.getRespondent2Witnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void shouldUpdateDefendantOneWitnesses_WhenNoWitnessesExisted() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQWitnesses(Witnesses.builder().build())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getRespondent1Witnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void shouldUpdateDefendantTwoWitnesses_WhenNoWitnessesExisted() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_TWO_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_TWO_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQWitnesses(Witnesses.builder().build())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(unwrapElements(updatedData.getRespondent2Witnesses()).get(0)).isEqualTo(expectedWitnessFlags);
            }

            @Test
            void addingExpertWhenNoneExisted() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(party));

                Experts applicant1ExpertsNoExpert = new Experts();
                applicant1ExpertsNoExpert.setExpertRequired(NO);
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQExperts(applicant1ExpertsNoExpert)
                                      .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getApplicant1DQ().getApplicant1DQExperts().getDetails()).get(0)).isEqualTo(
                    expectedExpert1);
                assertThat(updatedData.getApplicant1DQ().getApplicant1DQExperts().getExpertRequired()).isEqualTo(YES);
            }

            @Test
            void removingAllExperts() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_EXPERTS_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(CLAIMANT_ONE_EXPERTS_ID);
                updateDetailsForm.setUpdateExpertsDetailsForm(null);

                Experts applicant1ExpertsRemove = new Experts();
                applicant1ExpertsRemove.setExpertRequired(YES);
                applicant1ExpertsRemove.setDetails(wrapElements(dqExpert));
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQExperts(applicant1ExpertsRemove)
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
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party));

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQWitnesses(Witnesses.builder().witnessesToAppear(NO).build())
                                       .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(unwrapElements(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails()).get(0)).isEqualTo(
                    expectedWitness1);
                assertThat(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getWitnessesToAppear()).isEqualTo(
                    YES);
            }

            @Test
            void removingAllWitnesses() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(DEFENDANT_ONE_WITNESSES_ID)
                                                                .build())
                                                     .build());
                updateDetailsForm.setPartyChosenId(DEFENDANT_ONE_WITNESSES_ID);
                updateDetailsForm.setUpdateWitnessesDetailsForm(null);

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateApplicantRespondToDefenceAndProceed()
                    .updateDetailsForm(updateDetailsForm)
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
                assertThat(updatedData.getRespondent1DQ().getRespondent1DQWitnesses().getWitnessesToAppear()).isEqualTo(
                    NO);
            }
        }

        @Nested
        class MidShowWarning {
            private static final String PAGE_ID = "show-warning";

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID, DEFENDANT_ONE_ID, DEFENDANT_TWO_ID})
            void shouldReturnWarning(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addApplicant2LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .addRespondent2LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .addApplicant1LitigationFriend()
                    .addApplicant2LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .addRespondent2LitigationFriend()
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                String errorTitle = "Check the litigation friend's details";
                String errorMessage = "After making these changes, please ensure that the "
                    + "litigation friend's contact information is also up to date.";
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).contains(errorTitle);
                assertThat(response.getWarnings()).contains(errorMessage);
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID, DEFENDANT_ONE_ID, DEFENDANT_TWO_ID,
                DEFENDANT_ONE_LITIGATION_FRIEND_ID})
            void shouldNotReturnWarning(String partyChosenId) {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEmpty();
            }

            @Test
            void shouldReturnPostcodeError() {
                given(postcodeValidator.validate(any())).willReturn(List.of("Please enter Postcode"));

                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(CLAIMANT_ONE_ID)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1(Party.builder()
                                    .type(INDIVIDUAL)
                                    .primaryAddress(Address.builder()
                                                        .postCode(null)
                                                        .build())
                                    .build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isNotNull();
                assertEquals(1, response.getErrors().size());
                assertEquals("Please enter Postcode", response.getErrors().get(0));
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_LITIGATION_FRIEND_ID, DEFENDANT_TWO_LITIGATION_FRIEND_ID})
            void shouldReturnLitigationFriendWarning_sameDefendantLegalRep_twoDefendantLitigationFriends(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .addRespondent2LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                caseDataBefore.setRespondent2SameLegalRepresentative(YES);
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .addRespondent2LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                caseData.setRespondent2SameLegalRepresentative(YES);

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEqualTo(List.of(
                    "There is another litigation friend on this case. If the parties are using the same litigation "
                        + "friend you must update the other litigation friend's details too."));
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_LITIGATION_FRIEND_ID, DEFENDANT_TWO_LITIGATION_FRIEND_ID})
            void shouldNotReturnLitigationFriendWarning_diffDefendantLegalRep_twoDefendantLitigationFriends(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .addRespondent2LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                caseDataBefore.setRespondent2SameLegalRepresentative(null);
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .addRespondent2LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                caseData.setRespondent2SameLegalRepresentative(null);

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_LITIGATION_FRIEND_ID, CLAIMANT_TWO_LITIGATION_FRIEND_ID})
            void shouldReturnLitigationFriendWarning_twoClaimantLitigationFriend(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addApplicant2LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addApplicant2LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEqualTo(List.of(
                    "There is another litigation friend on this case. If the parties are using the same litigation "
                        + "friend you must update the other litigation friend's details too."));
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_LITIGATION_FRIEND_ID, DEFENDANT_TWO_LITIGATION_FRIEND_ID})
            void shouldNotReturnLitigationFriendWarning_sameDefendantRep_oneDefendantLitigationFriend(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                caseDataBefore.setRespondent2SameLegalRepresentative(YES);
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                caseData.setRespondent2SameLegalRepresentative(YES);

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_LITIGATION_FRIEND_ID, DEFENDANT_TWO_LITIGATION_FRIEND_ID})
            void shouldNotReturnLitigationFriendWarning_diffDefendantRep_oneDefendantLitigationFriend(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_LITIGATION_FRIEND_ID, CLAIMANT_TWO_LITIGATION_FRIEND_ID})
            void shouldNotReturnLitigationFriendWarning_oneClaimantLitigationFriend(String partyChosenId) {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();

                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .addApplicant1LitigationFriend()
                    .addRespondent1LitigationFriend()
                    .buildClaimIssuedPaymentCaseData();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getWarnings()).isEmpty();
            }
        }

        @Nested
        class MidShowWarningJudgmentOnline {
            private static final String PAGE_ID = "show-warning";

            @BeforeEach
            void setup() {
                when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID})
            void shouldReturnErrorForClaimantManageInformation(String partyChosenId) {

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .updateDetailsForm(updateDetailsForm)
                    .applicant1(Party.builder().type(Party.Type.INDIVIDUAL)
                                    .primaryAddress(Address.builder()
                                                        .addressLine1("Line 1 test again for more than 35 characters")
                                                        .addressLine2("Line 1 test again for more than 35 characters")
                                                        .addressLine3("Line 1 test again for more than 35 characters")
                                                        .county("Line 1 test again for more than 35 characters")
                                                        .postCode("PostCode more than 8 characters")
                                                        .postTown("Line 1 test again for more than 35 characters").build())
                                    .build())
                    .applicant2(Party.builder().type(Party.Type.INDIVIDUAL)
                                    .primaryAddress(Address.builder()
                                                        .addressLine1("Line 1 test again for more than 35 characters")
                                                        .addressLine2("Line 1 test again for more than 35 characters")
                                                        .addressLine3("Line 1 test again for more than 35 characters")
                                                        .county("Line 1 test again for more than 35 characters")
                                                        .postCode("PostCode more than 8 characters")
                                                        .postTown("Line 1 test again for more than 35 characters").build())
                                    .build())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isNotEmpty();
                assertThat(response.getErrors()).hasSize(6);
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_ID, CLAIMANT_TWO_ID})
            void shouldNotErrorForClaimantManageInformation(String partyChosenId) {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldNotErrorForNoPartyChosenSelectedManageInformation() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code("partyChosenId")
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldNotErrorForDefaultPartyChosenSelectedManageInformation() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code("default")
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_ID, DEFENDANT_TWO_ID})
            void shouldReturnErrorForDefendantManageInformation(String partyChosenId) {

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .updateDetailsForm(updateDetailsForm)
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                                    .primaryAddress(Address.builder()
                                                        .addressLine1("Line 1 test again for more than 35 characters")
                                                        .addressLine2("Line 1 test again for more than 35 characters")
                                                        .addressLine3("Line 1 test again for more than 35 characters")
                                                        .county("Line 1 test again for more than 35 characters")
                                                        .postCode("PostCode more than 8 characters")
                                                        .postTown("Line 1 test again for more than 35 characters").build())
                                    .build())
                    .respondent2(Party.builder().type(Party.Type.INDIVIDUAL)
                                    .primaryAddress(Address.builder()
                                                        .addressLine1("Line 1 test again for more than 35 characters")
                                                        .addressLine2("Line 1 test again for more than 35 characters")
                                                        .addressLine3("Line 1 test again for more than 35 characters")
                                                        .county("Line 1 test again for more than 35 characters")
                                                        .postCode("PostCode more than 8 characters")
                                                        .postTown("Line 1 test again for more than 35 characters").build())
                                    .build())
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isNotEmpty();
                assertThat(response.getErrors()).hasSize(6);
            }

            @ParameterizedTest
            @ValueSource(strings = {DEFENDANT_ONE_ID, DEFENDANT_TWO_ID})
            void shouldNotErrorForDefendantManageInformation(String partyChosenId) {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                when(caseDetailsConverter.toCaseData(any(CaseDetails.class))).thenReturn(caseData);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isEmpty();
            }

        }

        @Nested
        class MidShowPartyField {
            private static final String PAGE_ID = "show-party-field";

            @BeforeEach
            void setup() {
                CaseData caseDataBefore = CaseDataBuilder.builder()
                    .applicant1(Party.builder().type(INDIVIDUAL).build())
                    .applicant2(Party.builder().type(INDIVIDUAL).build())
                    .respondent1(Party.builder().type(INDIVIDUAL).build())
                    .respondent2(Party.builder().type(INDIVIDUAL).build())
                    .buildClaimIssuedPaymentCaseData();
                given(caseDetailsConverter.toCaseData(any(CaseDetails.class))).willReturn(caseDataBefore);
            }

            @Test
            void shouldPopulatePartyChosenId() {
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code("CODE")
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
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

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
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

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .updateDetailsForm(updateDetailsForm)
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

                Experts applicant1ExpertsPopulate = new Experts();
                applicant1ExpertsPopulate.setDetails(wrapElements(expert));
                Experts respondent1ExpertsPopulate = new Experts();
                respondent1ExpertsPopulate.setDetails(wrapElements(expert));
                Experts respondent2ExpertsPopulate = new Experts();
                respondent2ExpertsPopulate.setDetails(wrapElements(expert));
                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQExperts(applicant1ExpertsPopulate)
                                      .build())
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQExperts(respondent1ExpertsPopulate)
                                       .build())
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQExperts(respondent2ExpertsPopulate)
                                       .build())
                    .build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
                    .partyId("id").build();
                List<Element<UpdatePartyDetailsForm>> form = wrapElements(party);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
                assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEqualTo(form);
                assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_EXPERTS_ID, DEFENDANT_ONE_EXPERTS_ID, DEFENDANT_TWO_EXPERTS_ID})
            void shouldNotPopulateExpertsIfEmpty(String partyChosenId) {
                when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1DQ(null)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQExperts(new Experts())
                                       .build())
                    .respondent2DQ(Respondent2DQ.builder()
                                       .respondent2DQExperts(new Experts())
                                       .build())
                    .build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
                assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
                assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();

            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_WITNESSES_ID, DEFENDANT_ONE_WITNESSES_ID, DEFENDANT_TWO_WITNESSES_ID})
            void shouldPopulateWitnesses(String partyChosenId) {
                Witness witness = Witness.builder().firstName("First").lastName("Name").partyID("id").build();
                UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
                    .partyId("id").build();

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
                    .build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());
                caseData.setUpdateDetailsForm(updateDetailsForm);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                List<Element<UpdatePartyDetailsForm>> form = wrapElements(party);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
                assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
                assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEqualTo(form);
            }

            @ParameterizedTest
            @ValueSource(strings = {CLAIMANT_ONE_WITNESSES_ID, DEFENDANT_ONE_WITNESSES_ID, DEFENDANT_TWO_WITNESSES_ID})
            void shouldNotPopulateWitnessesIfEmpty(String partyChosenId) {
                Witness witness = Witness.builder().firstName("First").lastName("Name").partyID("id").build();
                UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name")
                    .partyId("id").build();
                List<Element<UpdatePartyDetailsForm>> form = wrapElements(party);

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setPartyChosen(DynamicList.builder()
                                                     .value(DynamicListElement.builder()
                                                                .code(partyChosenId)
                                                                .build())
                                                     .build());

                CaseData caseData = CaseDataBuilder.builder()
                    .applicant1DQ(Applicant1DQ.builder()
                                      .applicant1DQWitnesses(Witnesses.builder().build())
                                      .build())
                    .respondent1DQ(null)
                    .respondent2DQ(null)
                    .updateDetailsForm(updateDetailsForm)
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenId()).isEqualTo(partyChosenId);
                assertThat(updatedData.getUpdateDetailsForm().getPartyChosenType()).isEqualTo(null);
                assertThat(updatedData.getUpdateDetailsForm().getUpdateExpertsDetailsForm()).isEmpty();
                assertThat(updatedData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm()).isEmpty();

            }
        }

        @Nested
        class MidValidateExperts {
            private static final String PAGE_ID = "validate-experts";

            @Test
            void shouldValidatePartyIdsandReturnError() {
                when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

                UpdatePartyDetailsForm expert = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").partyId(
                    "id").build();
                UpdatePartyDetailsForm expert2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("Name2").build();

                // Create CaseData - try using toBuilder() if available
                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(expert, expert2));

                CaseData caseData = CaseData.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                List<String> expected = List.of(
                    "Adding a new expert is not permitted in this screen. Please delete any new experts.");

                assertEquals(expected, response.getErrors());

            }

            @Test
            void shouldValidatePartyIds_NotReturnError() {
                when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

                UpdatePartyDetailsForm expert = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").partyId(
                    "id").build();
                UpdatePartyDetailsForm expert2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("Name2").partyId(
                    "id").build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(expert, expert2));

                CaseData caseData = CaseData.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldValidatePartyIds_NotReturnErrorAsAdmin() {
                when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

                UpdatePartyDetailsForm expert = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").partyId(
                    "id").build();
                UpdatePartyDetailsForm expert2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("Name2").build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setUpdateExpertsDetailsForm(wrapElements(expert, expert2));

                CaseData caseData = CaseData.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }
        }

        @Nested
        class MidValidateWitnesses {
            private static final String PAGE_ID = "validate-witnesses";

            @Test
            void shouldValidatePartyIds_ReturnError() {
                when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

                UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").partyId(
                    "id").build();
                UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("Name2").build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party, party2));

                CaseData caseData = CaseData.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                List<String> expected = List.of(
                    "Adding a new witness is not permitted in this screen. Please delete any new witnesses.");

                assertEquals(expected, response.getErrors());

            }

            @Test
            void shouldValidatePartyIds_NotReturnError() {
                when(userService.getUserInfo(anyString())).thenReturn(LEGAL_REP_USER);

                UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").partyId(
                    "id").build();
                UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("Name2").partyId(
                    "id").build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party, party2));

                CaseData caseData = CaseData.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldValidatePartyIds_NotReturnErrorAsAdmin() {
                when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

                UpdatePartyDetailsForm party = UpdatePartyDetailsForm.builder().firstName("First").lastName("Name").partyId(
                    "id").build();
                UpdatePartyDetailsForm party2 = UpdatePartyDetailsForm.builder().firstName("Second").lastName("Name2").build();

                UpdateDetailsForm updateDetailsForm = new UpdateDetailsForm();
                updateDetailsForm.setUpdateWitnessesDetailsForm(wrapElements(party, party2));

                CaseData caseData = CaseData.builder()
                    .updateDetailsForm(updateDetailsForm)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }
        }
    }

    @Nested
    class Submitted {
        @Test
        void shouldBuildConfirmation() {

            CallbackParams params = callbackParamsOf(CaseDataBuilder.builder().build(), SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);

            assertEquals("# Contact information changed", response.getConfirmationHeader());
            assertEquals("### What happens next\n" +
                             "Any changes made to contact details have been updated.", response.getConfirmationBody());
        }
    }
}
