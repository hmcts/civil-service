package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
class ManageDocumentsHandlerTest extends BaseCallbackHandlerTest {

    private ManageDocumentsHandler handler;
    private ObjectMapper objectMapper;
    @Mock
    private TaskListService taskListService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ManageDocumentsHandler(objectMapper, taskListService);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void handleEventsReturnsTheExpectedCallbackEvent() {
            assertThat(handler.handledEvents()).contains(MANAGE_DOCUMENTS);
        }

        @Test
        void shouldUploadManageDocumentsSuccessfully() {
            //Given
            ManageDocument manageDocument = new ManageDocument();
            manageDocument.setDocumentType(ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART);
            manageDocument.setDocumentName("defendant");
            Document document1 = new Document()
                .setDocumentUrl("http://test.com")
                .setDocumentBinaryUrl("http://test.com/binary")
                .setDocumentFileName("document")
                .setCategoryID("ApplicantTestCategory");
            manageDocument.setDocumentLink(document1);
            Element<ManageDocument> document = new Element<>(
                UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
                manageDocument
            );

            CaseData caseDataBefore = CaseDataBuilder.builder().build();
            caseDataBefore.setManageDocuments(List.of(document));

            ManageDocument manageDocument1 = new ManageDocument();
            manageDocument1.setDocumentType(ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART);
            manageDocument1.setDocumentName("defendant");
            Document document3 = new Document()
                .setDocumentUrl("http://test2.com")
                .setDocumentBinaryUrl("http://test2.com/binary")
                .setDocumentFileName("document2");
            manageDocument1.setDocumentLink(document3);
            Element<ManageDocument> document2 = new Element<>(
                UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f45"),
                manageDocument1
            );
            CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();
            caseData.setManageDocuments(List.of(document, document2));

            CallbackParams params = callbackParamsOf(caseData, caseDataBefore, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            List<Element<ManageDocument>> manageDocuments = convertToMap(response.getData().get("manageDocuments"));

            assertEquals("http://test.com", manageDocuments.get(0).getValue().getDocumentLink().getDocumentUrl());
            assertEquals("ApplicantTestCategory", manageDocuments.get(0).getValue().getDocumentLink().getCategoryID());

            assertEquals("http://test2.com", manageDocuments.get(1).getValue().getDocumentLink().getDocumentUrl());
            assertNull(manageDocuments.get(1).getValue().getDocumentLink().getCategoryID());

            //Then
            assertThat(response.getData()).extracting("manageDocuments").isNotNull();
        }
    }

    public List<Element<ManageDocument>> convertToMap(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }
}
