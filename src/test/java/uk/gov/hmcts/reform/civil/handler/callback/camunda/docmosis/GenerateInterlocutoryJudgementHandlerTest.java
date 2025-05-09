package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse.InterlocutoryJudgementDocGenerator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.INTERLOCUTORY_JUDGEMENT;

@ExtendWith(MockitoExtension.class)
class GenerateInterlocutoryJudgementHandlerTest extends BaseCallbackHandlerTest {

    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(INTERLOCUTORY_JUDGEMENT)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())

        .build();
    private static final String BEARER_TOKEN = "BEARER_TOKEN";
    @Mock
    private InterlocutoryJudgementDocGenerator interlocutoryJudgementDocGenerator;
    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;
    @Mock
    private FeatureToggleService featureToggleService;

    private GenerateInterlocutoryJudgementHandler handler;

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        handler = new GenerateInterlocutoryJudgementHandler(
            mapper,
            interlocutoryJudgementDocGenerator,
            systemGeneratedDocumentService,
            featureToggleService
        );

    }

    @Test
    void shouldGenerateInterlocutoryJudgementDoc() {
        //Given
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .caseDataLiP(CaseDataLiP.builder()

                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotHideInterlocutoryJudgementDocWhenClaimantHasWelshPreferenceAndWelshToggleDisabled() {
        //Given
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .build())
            .claimantBilingualLanguagePreference("WELSH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideInterlocutoryJudgementDocWhenClaimantHasWelshPreference() {
        //Given
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .build())
            .claimantBilingualLanguagePreference("WELSH")
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideInterlocutoryJudgementDocWhenDefendantHasWelshPreference() {
        //Given
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build())
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideInterlocutoryJudgementDocWhenClaimantHasWelshDocPreference() {
        //Given
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .applicant1Represented(YesOrNo.NO)
            .applicant1DQ(Applicant1DQ.builder().applicant1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideInterlocutoryJudgementDocWhenDefendantHasWelshDocPreference() {
        //Given
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .respondent1Represented(YesOrNo.NO)
            .respondent1DQ(Respondent1DQ.builder().respondent1DQLanguage(WelshLanguageRequirements.builder().documents(
                Language.WELSH).build()).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateDocWhenCourtDecisionInFavourClaimant() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(interlocutoryJudgementDocGenerator, times(0)).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateDocWhenChoosesHowToProceedSignSettlementAgreement() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).build())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.SIGN_A_SETTLEMENT_AGREEMENT)
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(interlocutoryJudgementDocGenerator, times(0)).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }
}

