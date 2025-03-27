package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.service.docmosis.cosc.CertificateOfDebtGenerator;


import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_COSC_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CERTIFICATE_OF_DEBT_PAYMENT;

@ExtendWith(MockitoExtension.class)
class GenerateCoscDocumentHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private GenerateCoscDocumentHandler handler;
    @Mock
    private CertificateOfDebtGenerator certificateOfDebtGenerator;
    @Mock
    private CivilStitchService civilStitchService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateCoscDocumentHandler(certificateOfDebtGenerator, mapper, civilStitchService);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldGenerate_cosc_doc() {
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("cosc document")
            .documentSize(0L)
            .documentType(CERTIFICATE_OF_DEBT_PAYMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        when(certificateOfDebtGenerator.generateDoc(any(CaseData.class), anyString(),
                                                    eq(DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT))).thenReturn(document);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31DaysForCosc().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_COSC_DOCUMENT.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(certificateOfDebtGenerator).generateDoc(any(CaseData.class),
                                                       eq("BEARER_TOKEN"), eq(DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT));
        assertThat(response.getData())
            .extracting("coSCApplicationStatus")
            .isEqualTo("PROCESSED");
        assertThat(response.getData())
            .extracting("coSCApplicationStatus")
            .isEqualTo("PROCESSED");
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .contains("cosc document");

    }

    @Test
    void shouldGenerate_cosc_bilingualdoc() {
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("cosc document")
            .documentSize(0L)
            .documentType(CERTIFICATE_OF_DEBT_PAYMENT)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        when(certificateOfDebtGenerator
                 .generateDoc(any(CaseData.class), anyString(), eq(DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT))).thenReturn(document);
        when(certificateOfDebtGenerator
                 .generateDoc(any(CaseData.class), anyString(), eq(DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT_WELSH))).thenReturn(document);

        CaseData caseData = CaseDataBuilder.builder()
            .buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31DaysForCosc().toBuilder()
            .ccdCaseReference(Long.valueOf("12345"))
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1ResponseLanguage(Language.BOTH.toString())
                                                         .build()).build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_COSC_DOCUMENT.name());

        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(CERTIFICATE_OF_DEBT_PAYMENT),
                                                             anyString())).thenReturn(document);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(certificateOfDebtGenerator, times(1))
            .generateDoc(any(CaseData.class), eq("BEARER_TOKEN"), eq(DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT));
        verify(certificateOfDebtGenerator, times(1))
            .generateDoc(any(CaseData.class), eq("BEARER_TOKEN"), eq(DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT_WELSH));

        assertThat(response.getData())
            .extracting("coSCApplicationStatus")
            .isEqualTo("PROCESSED");
        List<?> documentsList = (List<?>) response.getData().get("systemGeneratedCaseDocuments");
        assertThat(documentsList)
            .extracting("value")
            .extracting("documentName")
            .contains("cosc document");

    }
}
