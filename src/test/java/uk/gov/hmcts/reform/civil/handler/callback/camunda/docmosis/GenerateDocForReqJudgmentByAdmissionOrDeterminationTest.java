package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        document1.setDocumentType(DEFENDANT_DEFENCE);
        document1.setCreatedDatetime(LocalDateTime.now());
        document1.setDocumentLink(documentLink);
        FORM = document1;
    }

    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldGenerateForm_ifCcjHasBeenRequested() {
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }

    @Test
    void shouldNotGenerateForm_ifCcjHasNotBeenRequested() {
        CaseData caseData = CaseDataBuilder.builder().build();
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.SIGN_A_SETTLEMENT_AGREEMENT);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(claimantLiPResponse);
        caseData.setCaseDataLiP(caseDataLiP);

        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verifyNoInteractions(formGenerator);
    }

    @Test
    void shouldGenerateForm_ifDefaultCcjHasBeenRequested() {
        given(formGenerator.generate(any(CaseEvent.class), any(CaseData.class), anyString())).willReturn(FORM);
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1LiPResponse(new ClaimantLiPResponse());
        caseData.setCaseDataLiP(caseDataLiP);

        CaseEvent event = CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generate(event, caseData, BEARER_TOKEN);
    }
}
