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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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

    public static final CaseDocument FORM;

    static {
        Document documentLink = new Document();
        documentLink.setDocumentUrl("fake-url");
        documentLink.setDocumentFileName("file-name");
        documentLink.setDocumentBinaryUrl("binary-url");

        CaseDocument document1 = new CaseDocument();
        document1.setCreatedBy("John");
        document1.setDocumentName("document name");
        document1.setDocumentSize(0L);
        document1.setDocumentType(INTERLOCUTORY_JUDGEMENT);
        document1.setCreatedDatetime(LocalDateTime.now());
        document1.setDocumentLink(documentLink);
        FORM = document1;
    }

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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1(new Party().setType(Party.Type.INDIVIDUAL));
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantResponseOnCourtDecision(
            ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotHideInterlocutoryJudgementDocWhenClaimantHasWelshPreferenceAndWelshToggleDisabled() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1(new Party().setType(Party.Type.INDIVIDUAL));
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantResponseOnCourtDecision(
            ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setClaimantBilingualLanguagePreference("WELSH");

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(0);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideInterlocutoryJudgementDocWhenClaimantHasWelshPreference() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1(new Party().setType(Party.Type.INDIVIDUAL));
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantResponseOnCourtDecision(
            ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setClaimantBilingualLanguagePreference("WELSH");

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldHideInterlocutoryJudgementDocWhenDefendantHasWelshPreference() {
        //Given
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        given(interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            any(CaseData.class),
            anyString()
        )).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantResponseOnCourtDecision(
            ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_PLAN);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("WELSH");
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getPreTranslationDocuments()).hasSize(1);
        assertThat(updatedData.getPreTranslationDocumentType()).isEqualTo(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT);
        verify(interlocutoryJudgementDocGenerator).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateDocWhenCourtDecisionInFavourClaimant() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().build();
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(interlocutoryJudgementDocGenerator, times(0)).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateDocWhenChoosesHowToProceedSignSettlementAgreement() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().build();
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.SIGN_A_SETTLEMENT_AGREEMENT);
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_DEFENDANT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));
        verify(interlocutoryJudgementDocGenerator, times(0)).generateInterlocutoryJudgementDoc(caseData, BEARER_TOKEN);
    }
}

