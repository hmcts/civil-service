package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse.RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator;

@ExtendWith(MockitoExtension.class)
class GenerateDocForReqJudgmentByAdmissionOrDeterminationTest extends BaseCallbackHandlerTest {

    @Mock
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private GenerateDocForReqJudgmentByAdmissionOrDetermination handler;

    @Mock
    private RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator formGenerator;

    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Mock
    private FeatureToggleService featureToggleService;

    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(DEFENDANT_DEFENCE)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @BeforeEach
    public void setup() {
        mapper.registerModule(new JavaTimeModule());
        handler = new GenerateDocForReqJudgmentByAdmissionOrDetermination(
            mapper,
            formGenerator,
            systemGeneratedDocumentService,
            featureToggleService
        );

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC);
        assertThat(handler.handledEvents()).contains(GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC);
        assertThat(handler.handledEvents()).contains(GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC);
    }

    @Test
    void shouldGenerateForm_ifCcjHasBeenRequested() {
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                    .build())
                .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_ifCcjHasNotBeenRequested() {

        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.SIGN_A_SETTLEMENT_AGREEMENT)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verifyNoInteractions(formGenerator);
    }

    @Test
    void shouldGenerateForm_ifDefaultCcjHasBeenRequested() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        CaseEvent event = CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifCcjHasBeenRequestedWhenWelsh() {
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateJudgementForm_ifCcjHasBeenRequestedWhenWelsh() {
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentDeterminationWithCcjHasBeenRequestedWhenFTisOff() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        CaseEvent event = GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(
                                 ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                     .build())
                             .build())
            .claimantBilingualLanguagePreference("ENGLISH")
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentDeterminationWithCcjHasBeenRequestedWhenFTisOn_claimantIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                                         .build())
                             .build())
            .claimantBilingualLanguagePreference("WELSH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentDeterminationWithCcjHasBeenRequestedWhenFTisOn_DefendantIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build())
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .claimantBilingualLanguagePreference("ENGLISH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentDeterminationWithCcjHasBeenRequestedWhenFTisOn_DefendantDocLangIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build())
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build())
            .claimantBilingualLanguagePreference("BOTH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentAdmissionWithCcjHasBeenRequestedWhenFTisOff() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(
                                 ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                     .build())
                             .build())
            .claimantBilingualLanguagePreference("ENGLISH")
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentAdmissionWithCcjHasBeenRequestedWhenFTisOn_claimantIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().applicant1LiPResponse(ClaimantLiPResponse.builder().applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                                         .build())
                             .build())
            .claimantBilingualLanguagePreference("WELSH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentAdmissionWithCcjHasBeenRequestedWhenFTisOn_DefendantIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build())
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .claimantBilingualLanguagePreference("ENGLISH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldGenerateForm_ifJudgmentAdmissionWithCcjHasBeenRequestedWhenFTisOn_DefendantDocLangIsWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build())
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build())
            .claimantBilingualLanguagePreference("BOTH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGeneratePreTranslatedDocumentsWhenEventIsDefaultJudgement() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseEvent event = CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("ENGLISH").build())
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build())
            .claimantBilingualLanguagePreference("BOTH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));;
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }
}
