package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingFormGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_FORM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateHearingFormHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class GenerateHearingFormHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateHearingFormHandler handler;
    @MockBean
    private HearingFormGenerator hearingFormGenerator;

    @Test
    public void shouldGenerateForm_when1v1() {
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(HEARING_FORM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments().size()).isEqualTo(1);
    }

    @Test
    public void shouldGenerate2Forms_whenListHave1PreviousForm() {
        CaseDocument document = CaseDocument.builder()
            .createdBy("John")
            .documentName("document name")
            .documentSize(0L)
            .documentType(HEARING_FORM)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();

        List<CaseDocument> documents = new ArrayList<>();
        documents.add(document);
        when(hearingFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(document));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .hearingDocuments(systemGeneratedCaseDocuments)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_FORM.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(hearingFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments().size()).isEqualTo(2);
    }
}
