package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.JudgmentByDeterminationDocGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_DETERMINATION_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class GenerateJudgmentByDeterminationFormHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private GenerateJudgmentByDeterminationFormHandler handler;

    @Mock
    private JudgmentByDeterminationDocGenerator formGenerator;

    private static final CaseDocument FORM = CaseDocument.builder()
        .createdBy("John")
        .documentName("document name")
        .documentSize(0L)
        .documentType(JUDGMENT_BY_DETERMINATION_CLAIMANT)
        .createdDatetime(LocalDateTime.now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldReturnCorrectCamundaTaskID() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder()
                                                 .request(CallbackRequest.builder().eventId(
                                                     "GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT").build()).build())).isEqualTo(
            "GenerateClaimantJudgmentByDeterminationDoc");
    }

    @Test
    void shouldGenerateForm() {
        List<CaseDocument> docs = new ArrayList<CaseDocument>();
        docs.add(FORM);
        given(formGenerator.generateDocs(any(CaseData.class), anyString(), anyString())).willReturn(docs);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());

        handler.handle(params);
        CaseEvent event = GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT;
        verify(formGenerator).generateDocs(caseData, BEARER_TOKEN, event.name());
    }

    @Test
    void shouldGenerateFormForDefendant() {
        List<CaseDocument> docs = new ArrayList<CaseDocument>();
        docs.add(FORM);
        given(formGenerator.generateDocs(any(CaseData.class), anyString(), anyString())).willReturn(docs);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT.name());
        handler.handle(params);
        CaseEvent event = GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT;
        verify(formGenerator).generateDocs(caseData, BEARER_TOKEN, event.name());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(
            GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT,
            GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT);
    }
}
