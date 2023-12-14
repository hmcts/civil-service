package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.service.CaseWorkerDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse.InterlocutoryJudgementDocGenerator;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private CaseWorkerDocumentService caseWorkerDocumentService;

    private GenerateInterlocutoryJudgementHandler handler;

    @BeforeEach
    public void setup() {
        ObjectMapper mapper = new ObjectMapper();
        handler = new GenerateInterlocutoryJudgementHandler(
            mapper,
            interlocutoryJudgementDocGenerator,
            caseWorkerDocumentService
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

