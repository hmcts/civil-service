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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_GA_LANGUAGE_UPDATE;

@ExtendWith(MockitoExtension.class)
public class UpdateGaLanguagePreferenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    private UpdateGaLanguagePreferenceCallbackHandler handler;
    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CaseDetails mockCaseDetails;
    @Mock
    private GaCaseDataEnricher gaCaseDataEnricher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new UpdateGaLanguagePreferenceCallbackHandler(coreCaseDataService, caseDetailsConverter, objectMapper, gaCaseDataEnricher);
        lenient().when(gaCaseDataEnricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_GA_LANGUAGE_UPDATE);
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantUnspecified() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder().build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantEnglish() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("ENGLISH")
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build())
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantWelsh() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("WELSH")
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build())
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantApplicantBoth() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("BOTH")
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build())
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentUnspecified() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder().build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentEnglish() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("ENGLISH")
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build())
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentWelsh() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("WELSH")
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build())
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldUpdateLanguagePreference_forClaimantRespondentBoth() {
            CaseData caseData = CaseData.builder()
                .parentClaimantIsApplicant(YesOrNo.NO)
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
                .build();
            CaseData civilCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("BOTH")
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build())
                .build();
            when(coreCaseDataService.getCase(any())).thenReturn(mockCaseDetails);
            when(caseDetailsConverter.toCaseData(mockCaseDetails)).thenReturn(civilCaseData);

            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(responseCaseData.getApplicantBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getRespondentBilingualLanguagePreference()).isEqualTo(YesOrNo.YES);
        }
    }
}
