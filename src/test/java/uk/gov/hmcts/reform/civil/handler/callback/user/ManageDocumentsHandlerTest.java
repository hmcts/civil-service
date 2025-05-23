package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
public class ManageDocumentsHandlerTest extends BaseCallbackHandlerTest {

    private ManageDocumentsHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ManageDocumentsHandler(objectMapper);
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
            Element<ManageDocument> document = new Element<>(
                UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"),
                ManageDocument.builder()
                    .documentType(ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART)
                    .documentName("defendant")
                    .documentLink(Document.builder().build())
                    .build()
            );
            CaseData caseData = CaseData.builder().manageDocuments((List.of(document))).build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getData()).extracting("manageDocuments").isNotNull();
        }
    }
}
