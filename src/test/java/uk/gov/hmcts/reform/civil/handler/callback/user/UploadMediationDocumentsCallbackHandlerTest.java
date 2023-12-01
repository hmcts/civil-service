package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.listFromDynamicList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    UploadMediationDocumentsCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class UploadMediationDocumentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UploadMediationDocumentsCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    private static final String PARTY_OPTIONS_PAGE = "populate-party-options";
    private static final String APPLICANT_SOLICITOR_ROLE = "[APPLICANTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_ONE_ROLE = "[RESPONDENTSOLICITORONE]";
    private static final String RESPONDENT_SOLICITOR_TWO_ROLE = "[RESPONDENTSOLICITORTWO]";

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventCallback {
        @BeforeEach
        void setUp() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Nested
        class ApplicantSolicitorInvokesEvent {

            @BeforeEach
            void setUp() {
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(APPLICANT_SOLICITOR_ROLE));
            }

            @Test
            void shouldReturnExpectedList_WhenInvokedFor1v1AsClaimantLR() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

                CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsPartyChosen());

                List<String> expected = List.of("Claimant 1: Mr. John Rambo");

                assertThat(actual).isEqualTo(expected);

            }

            @Test
            void shouldReturnExpectedList_WhenInvokedFor2v1AsClaimantLR() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoApplicants()
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsPartyChosen());

                List<String> expected = List.of("Claimant 1: Mr. John Rambo", "Claimant 2: Mr. Jason Rambo",
                                                "Claimants 1 and 2");

                assertThat(actual).isEqualTo(expected);

            }
        }

        @Nested
        class RespondentSolicitorInvokesEvent {

            @Test
            void shouldReturnExpectedList_WhenInvokedFor1v1AsRespondent1LR() {
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

                CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsPartyChosen());

                List<String> expected = List.of("Defendant 1: Mr. Sole Trader");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldReturnExpectedList_WhenInvokedFor1v2SameSolAsRespondent1LR() {
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE, RESPONDENT_SOLICITOR_TWO_ROLE));
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsPartyChosen());

                List<String> expected = List.of("Defendant 1: Mr. Sole Trader", "Defendant 2: Mr. John Rambo",
                                                "Defendants 1 and 2");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolAsRespondent1LR() {
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_ONE_ROLE));
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsPartyChosen());

                List<String> expected = List.of("Defendant 1: Mr. Sole Trader");

                assertThat(actual).isEqualTo(expected);
            }

            @Test
            void shouldReturnExpectedList_WhenInvokedFor1v2DifferentSolAsRespondent2LR() {
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of(RESPONDENT_SOLICITOR_TWO_ROLE));
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                CallbackParams params = callbackParamsOf(caseData, MID, PARTY_OPTIONS_PAGE);
                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);
                CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
                List<String> actual = listFromDynamicList(updatedData.getUploadMediationDocumentsPartyChosen());

                List<String> expected = List.of("Defendant 2: Mr. John Rambo");

                assertThat(actual).isEqualTo(expected);
            }
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldClearChosenParty_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
                .uploadMediationDocumentsPartyChosen(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .label("aa")
                                                                    .code("aa")
                                                                    .build())
                                                         .listItems(List.of(DynamicListElement.builder()
                                                                                .label("aa")
                                                                                .code("aa")
                                                                                .build()))
                                                         .build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getUploadMediationDocumentsPartyChosen()).isNull();
        }

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }
}
