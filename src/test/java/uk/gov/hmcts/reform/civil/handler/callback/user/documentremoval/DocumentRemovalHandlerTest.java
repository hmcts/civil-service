package uk.gov.hmcts.reform.civil.handler.callback.user.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.documentremoval.DocumentRemovalService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalHandlerTest extends BaseCallbackHandlerTest {

    private final String caseId = "72014545415";
    private final String documentId = "382952";
    private DocumentRemovalHandler handler;
    @Mock
    private DocumentRemovalService documentRemovalService;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        handler = new DocumentRemovalHandler(mapper, documentRemovalService);
    }

    @Nested
    class AboutToStart {

        @Test
        void testWithEmptyDocumentsToRemove() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .documentToKeepCollection(new ArrayList<>())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            when(documentRemovalService.getCaseDocumentsList(caseData)).thenReturn(new ArrayList<>());

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("documentToKeepCollection")
                .asInstanceOf(InstanceOfAssertFactories.list(DocumentToKeepCollection.class))
                .size()
                .isEqualTo(0);
        }

        @Test
        void testHandleWithValidDocumentsToRemove() {
            DocumentToKeep docToRemove = DocumentToKeep.builder()
                .caseDocument(CaseDocument.builder().documentLink(Document.builder()
                    .documentFileName("example.pdf")
                    .documentBinaryUrl("http://example.com/doc/123/binary")
                    .build()).build())
                .documentId(documentId).build();

            DocumentToKeepCollection docsToRemoveCollection = DocumentToKeepCollection.builder()
                .value(docToRemove).build();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .documentToKeepCollection(List.of(docsToRemoveCollection))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            when(documentRemovalService.getCaseDocumentsList(caseData)).thenReturn(List.of(docsToRemoveCollection));

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("documentToKeepCollection")
                .asInstanceOf(InstanceOfAssertFactories.list(DocumentToKeepCollection.class))
                .size()
                .isEqualTo(1);

            var expectedData = caseData.toMap(mapper);
            assertEquals(expectedData, response.getData());
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void testWithEmptyDocumentsToRemove() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .documentToKeepCollection(new ArrayList<>())

                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(documentRemovalService.removeDocuments(any(), anyLong(), anyString())).thenReturn(caseData);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("documentToKeepCollection")
                .asInstanceOf(InstanceOfAssertFactories.list(DocumentToKeepCollection.class))
                .size()
                .isEqualTo(0);
        }

        @Test
        void testHandleWithValidDocumentsToRemove() {
            DocumentToKeep docToRemove = DocumentToKeep.builder()
                .caseDocument(CaseDocument.builder().documentLink(Document.builder()
                    .documentFileName("example.pdf")
                    .documentBinaryUrl("http://example.com/doc/123/binary")
                    .build()).build())
                .documentId(documentId).build();

            DocumentToKeepCollection docsToRemoveCollection = DocumentToKeepCollection.builder()
                .value(docToRemove).build();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .documentToKeepCollection(List.of(docsToRemoveCollection))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(documentRemovalService.removeDocuments(any(), anyLong(), anyString())).thenReturn(caseData);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("documentToKeepCollection")
                .asInstanceOf(InstanceOfAssertFactories.list(DocumentToKeepCollection.class))
                .size()
                .isEqualTo(1);

            var expectedData = caseData.toMap(mapper);
            assertEquals(expectedData, response.getData());
        }
    }
}