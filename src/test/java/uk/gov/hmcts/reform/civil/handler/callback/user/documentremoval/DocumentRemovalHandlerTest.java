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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocumentToKeep;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.documentremoval.DocumentRemovalCaseDataDTO;
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
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setDocumentToKeepCollection(new ArrayList<>());
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
            CaseDocumentToKeep caseDocumentToKeep = new CaseDocumentToKeep();
            caseDocumentToKeep.setDocumentUrl("http://example.com/doc/123");
            caseDocumentToKeep.setDocumentFilename("example.pdf");
            caseDocumentToKeep.setDocumentBinaryUrl("http://example.com/doc/123/binary");
            DocumentToKeep docToRemove = new DocumentToKeep();
            docToRemove.setCaseDocumentToKeep(caseDocumentToKeep);
            docToRemove.setDocumentId(documentId);

            DocumentToKeepCollection docsToRemoveCollection = new DocumentToKeepCollection();
            docsToRemoveCollection.setValue(docToRemove);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setDocumentToKeepCollection(List.of(docsToRemoveCollection));

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
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setDocumentToKeepCollection(new ArrayList<>());
            DocumentRemovalCaseDataDTO documentRemovalCaseDataDTO = new DocumentRemovalCaseDataDTO();
            documentRemovalCaseDataDTO.setCaseData(caseData);
            documentRemovalCaseDataDTO.setDocumentsMarkedForDelete(new ArrayList<>());

            when(documentRemovalService.removeDocuments(any(), anyLong(), anyString())).thenReturn(documentRemovalCaseDataDTO);
            when(documentRemovalService.removeDocuments(any(), anyLong(), anyString())).thenReturn(documentRemovalCaseDataDTO);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData())
                .extracting("documentToKeepCollection")
                .asInstanceOf(InstanceOfAssertFactories.list(DocumentToKeepCollection.class))
                .size()
                .isEqualTo(0);
        }

        @Test
        void testHandleWithValidDocumentsToRemove() {
            CaseDocumentToKeep caseDocumentToKeep = new CaseDocumentToKeep();
            caseDocumentToKeep.setDocumentFilename("example.pdf");
            caseDocumentToKeep.setDocumentUrl("http://example.com/doc/123");
            caseDocumentToKeep.setDocumentBinaryUrl("http://example.com/doc/123/binary");
            DocumentToKeep docToKeep = new DocumentToKeep();
            docToKeep.setCaseDocumentToKeep(caseDocumentToKeep);
            docToKeep.setDocumentId(documentId);

            DocumentToKeepCollection docsToKeepCollection = new DocumentToKeepCollection();
            docsToKeepCollection.setValue(docToKeep);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setDocumentToKeepCollection(List.of(docsToKeepCollection));

            DocumentRemovalCaseDataDTO documentRemovalCaseDataDTO = new DocumentRemovalCaseDataDTO();
            documentRemovalCaseDataDTO.setCaseData(caseData);
            documentRemovalCaseDataDTO.setDocumentsMarkedForDelete(new ArrayList<>());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(documentRemovalService.removeDocuments(any(), anyLong(), anyString())).thenReturn(documentRemovalCaseDataDTO);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("documentToKeepCollection")
                .asInstanceOf(InstanceOfAssertFactories.list(DocumentToKeepCollection.class))
                .size()
                .isEqualTo(1);

            var expectedData = caseData.toMap(mapper);
            assertEquals(expectedData, response.getData());
        }

        @Test
        void testWithWarningsWhenSystemGeneratedDocumentsAreRemoved() {
            CaseDocumentToKeep caseDocumentToKeep = new CaseDocumentToKeep();
            caseDocumentToKeep.setDocumentFilename("example.pdf");
            caseDocumentToKeep.setDocumentUrl("http://example.com/doc/123");
            caseDocumentToKeep.setDocumentBinaryUrl("http://example.com/doc/123/binary");
            DocumentToKeep docToKeep = new DocumentToKeep();
            docToKeep.setCaseDocumentToKeep(caseDocumentToKeep);
            docToKeep.setDocumentId(documentId);
            docToKeep.setSystemGenerated(YesOrNo.YES);

            DocumentToKeepCollection docsToKeepCollection = new DocumentToKeepCollection();
            docsToKeepCollection.setValue(docToKeep);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setDocumentToKeepCollection(List.of(docsToKeepCollection));

            DocumentRemovalCaseDataDTO documentRemovalCaseDataDTO = new DocumentRemovalCaseDataDTO();
            documentRemovalCaseDataDTO.setCaseData(caseData);
            CaseDocumentToKeep caseDocumentToKeepSys = new CaseDocumentToKeep();
            caseDocumentToKeepSys.setDocumentFilename("System Doc");
            DocumentToKeep documentToKeepSys = new DocumentToKeep();
            documentToKeepSys.setCaseDocumentToKeep(caseDocumentToKeepSys);
            documentToKeepSys.setDocumentId("123");
            documentToKeepSys.setSystemGenerated(YesOrNo.YES);
            CaseDocumentToKeep caseDocumentToKeepUsr = new CaseDocumentToKeep();
            caseDocumentToKeepUsr.setDocumentFilename("User Doc");
            DocumentToKeep documentToKeepUsr = new DocumentToKeep();
            documentToKeepUsr.setCaseDocumentToKeep(caseDocumentToKeepUsr);
            documentToKeepUsr.setDocumentId("456");
            documentToKeepUsr.setSystemGenerated(YesOrNo.NO);
            documentRemovalCaseDataDTO.setDocumentsMarkedForDelete(
                List.of(documentToKeepSys, documentToKeepUsr));

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(documentRemovalService.removeDocuments(any(), anyLong(), anyString())).thenReturn(documentRemovalCaseDataDTO);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getWarnings())
                .contains("System Generated Document System Doc (123) will be removed from the case");
            assertThat(response.getWarnings())
                .contains("User Document User Doc (456) will be removed from the case");
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
