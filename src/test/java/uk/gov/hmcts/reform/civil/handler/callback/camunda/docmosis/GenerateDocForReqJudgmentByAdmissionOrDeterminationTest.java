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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimLipResponseFormGenerator;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDocForReqJudgmentByAdmissionOrDetermination.class,
    JacksonAutoConfiguration.class,
})
public class GenerateDocForReqJudgmentByAdmissionOrDeterminationTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateDocForReqJudgmentByAdmissionOrDetermination handler;
    @MockBean
    private RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator formGenerator;

    @MockBean
    private SystemGeneratedDocumentService systemGeneratedDocumentService;

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

    @Test
    void shouldGenerateForm_ifCcjHasBeenRequested() {
        CaseEvent event = CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
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
}
