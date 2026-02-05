package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_GA_LANGUAGE_UPDATE;

@ExtendWith(MockitoExtension.class)
public class UpdateGaLanguagePreferenceCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    private UpdateGaLanguagePreferenceCallbackHandler handler;
    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CaseDetails mockCaseDetails;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new UpdateGaLanguagePreferenceCallbackHandler(coreCaseDataService, caseDetailsConverter, objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_GA_LANGUAGE_UPDATE);
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantUnspecified() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder().build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantEnglish() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
                .claimantBilingualLanguagePreference("ENGLISH")
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("ENGLISH"))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantWelsh() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
                .claimantBilingualLanguagePreference("WELSH")
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("WELSH"))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantBoth() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
                .claimantBilingualLanguagePreference("BOTH")
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("BOTH"))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentUnspecified() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder().build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentEnglish() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
                .claimantBilingualLanguagePreference("ENGLISH")
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("ENGLISH"))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentWelsh() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
                .claimantBilingualLanguagePreference("WELSH")
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("WELSH"))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentBoth() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
                .claimantBilingualLanguagePreference("BOTH")
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("BOTH"))
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }
    }
}
