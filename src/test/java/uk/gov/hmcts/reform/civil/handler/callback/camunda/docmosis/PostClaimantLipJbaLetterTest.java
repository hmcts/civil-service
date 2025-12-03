package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.CoverLetterService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_CLAIMANT_LIP_JBA_LETTER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class PostClaimantLipJbaLetterTest {

    @InjectMocks
    private PostClaimantLipJbaLetterHandler handler;

    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private CoverLetterService coverLetterService;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandlePostJbaLetterToLiPClaimant_whenApplicantIsLiP() {
        Document document = new Document();
        document.setDocumentUrl("http://docstore/documents/1234");
        document.setDocumentFileName("dj.pdf");

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentLink(document);
        caseDocument.setDocumentName("JBA Document");
        caseDocument.setDocumentType(JUDGMENT_BY_ADMISSION_CLAIMANT);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);
        caseData.setLegacyCaseReference("001MC001");
        caseData.setApplicant1(PartyBuilder.builder().individual("John").build());
        caseData.setRespondent1(PartyBuilder.builder().individual("Mike").build());
        caseData.setSystemGeneratedCaseDocuments(List.of(element(caseDocument)));
        caseData.setApplicant1Represented(NO);

        when(coverLetterService.generateDocumentWithCoverLetterBinary(any(Party.class), any(CaseData.class), any(), anyString(), anyString()))
            .thenReturn("PDF".getBytes());
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(Map.of());

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.handle(callbackParams);

        assertThat(response).isNotNull();
        verify(bulkPrintService).printLetter(
            "PDF".getBytes(),
            "001MC001",
            "001MC001",
            "claimant_jba_letter",
            List.of("Mr. John Rambo")
        );
    }

    @Test
    void shouldNotPrintOrGenerate_whenApplicantIsNotLiP() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);
        caseData.setLegacyCaseReference("001MC001");
        caseData.setApplicant1(PartyBuilder.builder().individual("John").build());
        caseData.setRespondent1(PartyBuilder.builder().individual("Mike").build());
        caseData.setApplicant1Represented(YES);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(Map.of());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.handle(callbackParams);

        assertThat(response).isNotNull();
        verifyNoInteractions(bulkPrintService);
        verifyNoInteractions(coverLetterService);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId() {
        CallbackParams callbackParams = mock(CallbackParams.class);
        String activityId = handler.camundaActivityId(callbackParams);
        assertThat(activityId).isEqualTo("PostClaimantLipJBALetter");
    }

    @Test
    void shouldReturnCorrectHandledEvents() {
        List<CaseEvent> events = handler.handledEvents();
        assertThat(events).containsExactly(POST_CLAIMANT_LIP_JBA_LETTER);
    }
}
