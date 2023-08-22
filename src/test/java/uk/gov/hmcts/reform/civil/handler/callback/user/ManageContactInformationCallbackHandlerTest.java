package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;

@SpringBootTest(classes = {
    ManageContactInformationCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class ManageContactInformationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ManageContactInformationCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

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
            CaseData caseData = CaseData.builder().ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
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
                .addRespondent1ExpertsAndWitnesses()
                .respondent1(Party.builder()
                                 .type(COMPANY)
                                 .companyName("Test Inc")
                                 .build())
                .build()
                .toBuilder().ccdCaseReference(123L).build();

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            DynamicList expected = DynamicList.fromList(List.of("CLAIMANT 1: ASDASD",
                                                                   "CLAIMANT 1: Indi org",
                                                                   "CLAIMANT 1: INdi lR",
                                                                   "CLAIMANT 1: Witnesses",
                                                                   "CLAIMANT 1: Experts",
                                                                   "DEFENDANT 1: Mr Sole Trader",
                                                                   "DEFENDANT 1: Litigation Friend: Litigation Frined",
                                                                   "DEFENDANT 1: Ind LR",
                                                                   "DEFENDANT 1: Witnesses",
                                                                   "DEFENDANT 1: Experts"
            ));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getUpdateDetailsForm().getPartyChosen().getListItems()).isEqualTo(expected.getListItems());
        }

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsApplicantSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v1AsRespondentSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsAdmin() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsApplicantSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor2v1AsRespondentSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsAdmin() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsApplicantSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2SameSolicitorAsRespondentSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsAdmin() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsApplicantSolicitor() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsRespondentSolicitorOne() {}

        @Test
        void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolicitorAsRespondentSolicitorTwo() {}
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnExpectedResponse_WhenAboutToSubmitIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertEquals(AboutToStartOrSubmitCallbackResponse.builder().build(), response);
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnExpectedResponse_WhenSubmittedIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertEquals(AboutToStartOrSubmitCallbackResponse.builder().build(), response);
        }
    }
}
