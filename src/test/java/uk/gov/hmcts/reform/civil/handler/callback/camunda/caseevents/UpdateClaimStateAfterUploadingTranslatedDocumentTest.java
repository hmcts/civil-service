package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UpdateClaimStateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED;

@ExtendWith(MockitoExtension.class)
class UpdateClaimStateAfterUploadingTranslatedDocumentTest extends BaseCallbackHandlerTest {

    private UpdateClaimStateAfterUploadingTranslatedDocuments handler;
    @Mock
    private UpdateClaimStateService updateClaimStateService;
    @Mock
    private ToggleConfiguration toggleConfiguration;

    public static final String TASK_ID = "updateClaimStateAfterTranslateDocumentUploadedID";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new UpdateClaimStateAfterUploadingTranslatedDocuments(
            objectMapper,
            updateClaimStateService,
            toggleConfiguration
        );
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo(TASK_ID);
    }

    @Test
    void shouldRunAboutToSubmitSuccessfully() {
        // given
        CaseData caseData = lipVLipBilingualCaseData(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        when(toggleConfiguration.getFeatureToggle()).thenReturn("WA 4");

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
        assertThat(response.getData())
            .extracting("featureToggleWA", "previousCCDState")
            .containsExactly("WA 4", CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED);

    }

    @Test
    void shouldUpdateClaimState_WhenClaimStateIsClaimIssued() {
        // given
        CaseData caseData = lipVLipBilingualCaseData(CaseState.CASE_ISSUED);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name());
    }

    @Test
    void shouldNotUpdateClaimState_WhenCaseIsNotLipVLip() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .claimantBilingualLanguagePreference(Language.BOTH.name())
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.NO)
            .build()
            .toBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isNull();
        assertThat(response.getData())
            .extracting("previousCCDState")
            .isEqualTo(CaseState.CASE_ISSUED.name());
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldNotUpdateClaimState_WhenClaimantLanguagePreferenceIsNotBoth() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .claimantBilingualLanguagePreference(Language.ENGLISH.name())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build()
            .toBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isNull();
        assertThat(response.getData())
            .extracting("previousCCDState")
            .isEqualTo(CaseState.CASE_ISSUED.name());
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldNotUpdateClaimState_WhenClaimantLanguagePreferenceIsNotBothAndRespondentLanguageIsBoth() {
        // given
        CaseData caseData = lipVLipCaseDataWithLanguagePreferences(
            CaseState.CASE_ISSUED,
            Language.ENGLISH,
            Language.BOTH
        );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isNull();
        assertThat(response.getData())
            .extracting("previousCCDState")
            .isEqualTo(CaseState.CASE_ISSUED.name());
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldNotUpdateClaimState_WhenClaimantLanguagePreferenceIsNotBothAndRespondentLanguageIsNotSet() {
        // given
        CaseData caseData = lipVLipCaseDataWithLanguagePreferences(
            CaseState.CASE_ISSUED,
            Language.ENGLISH,
            null
        );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isNull();
        assertThat(response.getData())
            .extracting("previousCCDState")
            .isEqualTo(CaseState.CASE_ISSUED.name());
        verifyNoInteractions(updateClaimStateService);
    }

    @ParameterizedTest
    @EnumSource(value = Language.class, names = {"ENGLISH", "WELSH"})
    void shouldUpdateClaimState_WhenClaimantLanguagePreferenceIsNotBothAndRespondentLanguageIsNotBoth(Language language) {
        // given
        CaseData caseData = lipVLipCaseDataWithLanguagePreferences(
            CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
            language,
            language
        );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldUpdateClaimState_WhenAwaitingApplicantIntentionClaimState() {
        // given
        CaseData caseData = lipVLipBilingualCaseData(CaseState.AWAITING_APPLICANT_INTENTION);
        when(updateClaimStateService.setUpCaseState(caseData)).thenReturn(JUDICIAL_REFERRAL.name());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        verify(updateClaimStateService, times(1)).setUpCaseState(caseData);
    }

    @Test
    void shouldKeepCurrentClaimState_WhenNoTranslatedDocumentStateTransitionApplies() {
        // given
        CaseData caseData = lipVLipBilingualCaseData(CaseState.JUDICIAL_REFERRAL);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.JUDICIAL_REFERRAL.name());
        verifyNoInteractions(updateClaimStateService);
    }

    @Test
    void shouldNotUpdateClaimState_WhenClaimantSignedSettlementAgreement() {
        // given
        CaseData caseData = lipVLipBilingualCaseData(CaseState.AWAITING_APPLICANT_INTENTION);
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1SignedSettlementAgreement(YesOrNo.YES);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // when
        var response = (AboutToStartOrSubmitCallbackResponse)handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
        verifyNoInteractions(updateClaimStateService);
    }

    private CaseData lipVLipBilingualCaseData(CaseState ccdState) {
        return CaseDataBuilder.builder()
            .claimantBilingualLanguagePreference(Language.BOTH.name())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .build()
            .toBuilder()
            .ccdState(ccdState)
            .build();
    }

    private CaseData lipVLipCaseDataWithLanguagePreferences(
        CaseState ccdState,
        Language claimantLanguage,
        Language respondentLanguage
    ) {
        return CaseDataBuilder.builder()
            .claimantBilingualLanguagePreference(claimantLanguage.name())
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .caseDataLip(new CaseDataLiP().setRespondent1LiPResponse(
                new RespondentLiPResponse().setRespondent1ResponseLanguage(
                    respondentLanguage == null ? null : respondentLanguage.name()
                )
            ))
            .build()
            .toBuilder()
            .ccdState(ccdState)
            .build();
    }
}
