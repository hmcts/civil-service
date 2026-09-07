package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentStrategy;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentStrategyFactory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.UserType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_LANGUAGE_PREFERENCE;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.ENGLISH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.ENGLISH_AND_WELSH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.WELSH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ChangeLanguagePreferenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ChangeLanguagePreferenceCallbackHandler handler;
    private ObjectMapper mapper;

    @Mock
    private UploadTranslatedDocumentStrategyFactory uploadTranslatedDocumentStrategyFactory;
    @Mock
    private UploadTranslatedDocumentStrategy uploadTranslatedDocumentStrategy;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new ChangeLanguagePreferenceCallbackHandler(
            uploadTranslatedDocumentStrategyFactory,
            mapper
        );
    }

    private void stubUploadDocumentResponse() {
        when(uploadTranslatedDocumentStrategyFactory.getUploadTranslatedDocumentStrategy(nullable(CallbackVersion.class)))
            .thenReturn(uploadTranslatedDocumentStrategy);
        when(uploadTranslatedDocumentStrategy.uploadDocument(any(CallbackParams.class)))
            .thenAnswer(invocation -> {
                CallbackParams callbackParams = invocation.getArgument(0);
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(callbackParams.getCaseData().toMap(mapper))
                    .build();
            });
    }

    private ChangeLanguagePreference changeLanguagePreference(UserType userType) {
        ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
        changeLanguagePreference.setUserType(userType);
        changeLanguagePreference.setPreferredLanguage(WELSH);
        return changeLanguagePreference;
    }

    private CaseData respondentCaseData() {
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("WELSH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(NO);
        caseData.setRespondent1Represented(NO);
        caseData.setCaseDataLiP(caseDataLiP);
        return caseData;
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CHANGE_LANGUAGE_PREFERENCE);
    }

    @Test
    void shouldClearData_WhenAboutToStart() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
        changeLanguagePreference.setUserType(CLAIMANT);
        changeLanguagePreference.setPreferredLanguage(ENGLISH);
        caseData.setChangeLanguagePreference(changeLanguagePreference);
        caseData.setApplicant1Represented(NO);
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedCaseData.getChangeLanguagePreference()).isNull();
    }

    @Test
    void shouldReturnEmptyResponse_WhenSubmitted() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
    }

    @Nested
    class MidCallback {

        @Test
        void shouldThrowException_WhenNoLanguageChangeData() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("User type not found");
        }

        @Test
        void shouldThrowException_WhenNoUserType() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("User type not found");
        }

        @Test
        void shouldReturnError_WhenClaimantIsNotLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(CLAIMANT);
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("The selected party must be unrepresented.");
        }

        @Test
        void shouldReturnError_WhenDefendantIsNotLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(DEFENDANT);
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("The selected party must be unrepresented.");
        }

        @Test
        void shouldReturnError_WhenDefendantHasNotResponded() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(DEFENDANT);
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            caseData.setRespondent1Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).containsExactly("The defendant must have already responded in order to change their language preference.");
        }

        @Test
        void shouldReturnError_WhenDefendantLipResponseIsMissing() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT));
            caseData.setRespondent1Represented(NO);
            caseData.setCaseDataLiP(new CaseDataLiP());
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("The defendant must have already responded in order to change their language preference.");
        }

        @Test
        void shouldReturnNoErrors_WhenClaimantIsLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(CLAIMANT));
            caseData.setApplicant1Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrors_WhenDefendantIsLipAndHasResponded() {
            CaseData caseData = respondentCaseData();
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT));
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldThrowException_WhenNoLanguageChangeData() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("Preferred language not found");
        }

        @Test
        void shouldThrowException_WhenNoLanguagePreference() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(CLAIMANT);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("Preferred language not found");
        }

        @Test
        void shouldThrowException_WhenNoUserType() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(params))
                .withMessage("User type not found");
        }

        @Test
        void shouldChangeClaimantLanguagePreferenceToEnglish() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().ccdCaseReference(123L).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(CLAIMANT);
            changeLanguagePreference.setPreferredLanguage(ENGLISH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            caseData.setApplicant1Represented(NO);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            stubUploadDocumentResponse();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("ENGLISH");
            assertThat(updatedCaseData.getClaimantLanguagePreferenceDisplay()).isEqualTo(ENGLISH);
        }

        @Test
        void shouldChangeClaimantLanguagePreferenceToWelsh() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(CLAIMANT);
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            caseData.setApplicant1Represented(NO);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("WELSH");
            assertThat(updatedCaseData.getClaimantLanguagePreferenceDisplay()).isEqualTo(WELSH);
            verifyNoInteractions(uploadTranslatedDocumentStrategyFactory);
        }

        @Test
        void shouldSetBusinessProcess_WhenLanguagePreferenceChanged() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(CLAIMANT));
            caseData.setApplicant1Represented(NO);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData().get("businessProcess"))
                .extracting("camundaEvent", "status")
                .contains(CHANGE_LANGUAGE_PREFERENCE.name(), READY.name());
        }

        @Test
        void shouldChangeDefendantLanguagePreference() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentRespondToClaim(FULL_DEFENCE).build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(DEFENDANT);
            changeLanguagePreference.setPreferredLanguage(ENGLISH_AND_WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            caseData.setRespondent1Represented(NO);
            RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
            respondentLiPResponse.setRespondent1ResponseLanguage("ENGLISH");
            CaseDataLiP caseDataLiP = new CaseDataLiP();
            caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
            caseData.setCaseDataLiP(caseDataLiP);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1ResponseLanguage()).isEqualTo("BOTH");
            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isEqualTo(ENGLISH_AND_WELSH);
        }
    }
}
