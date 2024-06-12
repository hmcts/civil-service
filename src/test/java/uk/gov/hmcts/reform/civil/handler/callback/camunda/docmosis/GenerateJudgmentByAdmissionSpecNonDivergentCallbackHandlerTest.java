package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse.RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;

@ExtendWith(MockitoExtension.class)
class GenerateJudgmentByAdmissionSpecNonDivergentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private GenerateJudgmentByAdmissionSpecNonDivergentCallbackHandler handler;

    @Mock
    private RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator formGenerator;

    @Mock
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

    private static final List<CaseDocument> listForm = new ArrayList<>();
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @BeforeEach
    void addDocs() {
        listForm.add(CaseDocument.builder()
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
            .build());
    }

    @Test
    void shouldGenerateClaimantForm() {
        CaseEvent event = CaseEvent.GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT;
        given(formGenerator.generateNonDivergentDocs(any(CaseData.class), anyString(), any(CaseEvent.class))).willReturn(listForm);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generateNonDivergentDocs(caseData, BEARER_TOKEN, event);
    }

    @Test
    void shouldGenerateDefendantForm() {
        CaseEvent event = CaseEvent.GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT;
        given(formGenerator.generateNonDivergentDocs(any(CaseData.class), anyString(), any(CaseEvent.class))).willReturn(listForm);
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build())
                             .build())
            .build();

        handler.handle(callbackParamsOf(caseData, event, ABOUT_TO_SUBMIT));
        verify(formGenerator).generateNonDivergentDocs(caseData, BEARER_TOKEN, event);
    }
}
