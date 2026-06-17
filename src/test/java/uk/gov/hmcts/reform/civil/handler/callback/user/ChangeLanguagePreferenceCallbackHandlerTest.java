package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentStrategy;
import uk.gov.hmcts.reform.civil.handler.callback.user.strategy.translateddocuments.UploadTranslatedDocumentStrategyFactory;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.model.welshenhancements.UserType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CANCEL_DOC_TRANSLATION_TASK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_LANGUAGE_PREFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_GA_LANGUAGE_UPDATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.ENGLISH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.ENGLISH_AND_WELSH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage.WELSH;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ChangeLanguagePreferenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ChangeLanguagePreferenceCallbackHandler handler;
    private ObjectMapper mapper;

    @Mock
    private UploadTranslatedDocumentStrategyFactory uploadTranslatedDocumentStrategyFactory;
    @Mock
    private UploadTranslatedDocumentStrategy uploadTranslatedDocumentStrategy;
    @Mock
    private GenAppStateHelperService helperService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new ChangeLanguagePreferenceCallbackHandler(
            uploadTranslatedDocumentStrategyFactory,
            mapper,
            helperService,
            coreCaseDataService,
            caseDetailsConverter
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

    private void stubStartUpdateCaseData(CaseData startEventCaseData) {
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(coreCaseDataService.startUpdate("123", CANCEL_DOC_TRANSLATION_TASK))
            .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(startEventCaseData);
    }

    private ChangeLanguagePreference changeLanguagePreference(UserType userType, PreferredLanguage preferredLanguage) {
        ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
        changeLanguagePreference.setUserType(userType);
        changeLanguagePreference.setPreferredLanguage(preferredLanguage);
        return changeLanguagePreference;
    }

    private CaseData claimantCaseData(String claimantLanguage, boolean lipVLip) {
        CaseData caseData = CaseDataBuilder.builder()
            .claimantBilingualLanguagePreference(claimantLanguage)
            .build();
        caseData.setApplicant1Represented(lipVLip ? NO : null);
        caseData.setRespondent1Represented(NO);
        return caseData;
    }

    private CaseData respondentCaseData(String respondentLanguage) {
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage(respondentLanguage);
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
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT, WELSH));
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
            caseData.setChangeLanguagePreference(changeLanguagePreference(CLAIMANT, WELSH));
            caseData.setApplicant1Represented(NO);
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-lang-pref");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrors_WhenDefendantIsLipAndHasResponded() {
            CaseData caseData = respondentCaseData("WELSH");
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT, WELSH));
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
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

        @Test
        void shouldTriggerGaLanguageUpdateEvent_WhenCaseHasGeneralApplications() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            ChangeLanguagePreference changeLanguagePreference = new ChangeLanguagePreference();
            changeLanguagePreference.setUserType(CLAIMANT);
            changeLanguagePreference.setPreferredLanguage(WELSH);
            caseData.setChangeLanguagePreference(changeLanguagePreference);
            caseData.setApplicant1Represented(NO);
            caseData.setGeneralApplications(wrapElements(new GeneralApplication()));
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("WELSH");
            assertThat(updatedCaseData.getClaimantLanguagePreferenceDisplay()).isEqualTo(WELSH);

            verify(helperService).triggerEvent(caseData, TRIGGER_GA_LANGUAGE_UPDATE);
            verifyNoInteractions(uploadTranslatedDocumentStrategyFactory);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldTriggerCancelDocTranslationTask_WhenClaimantLanguageIsEnglish() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(CLAIMANT, ENGLISH));
            stubStartUpdateCaseData(claimantCaseData("ENGLISH", true));
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService).startUpdate("123", CANCEL_DOC_TRANSLATION_TASK);
            verify(caseDetailsConverter).toCaseData(any(CaseDetails.class));
            verify(coreCaseDataService).triggerEvent(123L, CANCEL_DOC_TRANSLATION_TASK);
        }

        @Test
        void shouldNotTriggerCancelDocTranslationTask_WhenClaimantLanguageIsEnglishButCaseIsNotLipVLip() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(CLAIMANT, ENGLISH));
            stubStartUpdateCaseData(claimantCaseData("ENGLISH", false));
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService, never()).triggerEvent(anyLong(), eq(CANCEL_DOC_TRANSLATION_TASK));
        }

        @Test
        void shouldNotTriggerCancelDocTranslationTask_WhenClaimantLanguageIsWelsh() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(CLAIMANT, WELSH));
            stubStartUpdateCaseData(claimantCaseData("WELSH", true));
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService).startUpdate("123", CANCEL_DOC_TRANSLATION_TASK);
            verify(caseDetailsConverter).toCaseData(any(CaseDetails.class));
            verify(coreCaseDataService, never()).triggerEvent(anyLong(), eq(CANCEL_DOC_TRANSLATION_TASK));
        }

        @Test
        void shouldTriggerCancelDocTranslationTask_WhenRespondentLanguageIsEnglish() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT, ENGLISH));
            stubStartUpdateCaseData(respondentCaseData("english"));
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService).startUpdate("123", CANCEL_DOC_TRANSLATION_TASK);
            verify(caseDetailsConverter).toCaseData(any(CaseDetails.class));
            verify(coreCaseDataService).triggerEvent(123L, CANCEL_DOC_TRANSLATION_TASK);
        }

        @Test
        void shouldTriggerCancelDocTranslationTask_WhenRespondentResponseIsMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT, ENGLISH));
            stubStartUpdateCaseData(CaseDataBuilder.builder().build());
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService).triggerEvent(123L, CANCEL_DOC_TRANSLATION_TASK);
        }

        @Test
        void shouldTriggerCancelDocTranslationTask_WhenRespondentLipDataIsMissing() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT, ENGLISH));
            CaseData startEventCaseData = CaseDataBuilder.builder().build();
            startEventCaseData.setCaseDataLiP(new CaseDataLiP());
            stubStartUpdateCaseData(startEventCaseData);
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService).triggerEvent(123L, CANCEL_DOC_TRANSLATION_TASK);
        }

        @Test
        void shouldNotTriggerCancelDocTranslationTask_WhenRespondentLanguageIsWelsh() {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(123L)
                .build();
            caseData.setChangeLanguagePreference(changeLanguagePreference(DEFENDANT, WELSH));
            stubStartUpdateCaseData(respondentCaseData("WELSH"));
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
            verify(coreCaseDataService).startUpdate("123", CANCEL_DOC_TRANSLATION_TASK);
            verify(caseDetailsConverter).toCaseData(any(CaseDetails.class));
            verify(coreCaseDataService, never()).triggerEvent(anyLong(), eq(CANCEL_DOC_TRANSLATION_TASK));
        }
    }
}
