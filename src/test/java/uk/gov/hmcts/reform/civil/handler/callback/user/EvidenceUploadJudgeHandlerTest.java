package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadJudgeHandler.class,
    JacksonAutoConfiguration.class
})
public class EvidenceUploadJudgeHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EvidenceUploadJudgeHandler handler;

    public static final String REFERENCE_NUMBER = "000DC001";

    @Nested
    class AboutToStartCallback {

        @Test
        void aboutToStartCallback_placeholder() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void aboutToSubmitCallback_placeholder() {
            CaseData caseData = CaseDataBuilder.builder().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldPopulateConfirmation_DocumentAndNote() {
            String header = "# Document uploaded and note added \n # " + REFERENCE_NUMBER;
            String body = "## You have uploaded: \n * A Fancy Name\n";

            Document testDocument = new Document("testurl",
                                                 "testBinUrl",
                                                 "A Fancy Name",
                                                 "hash");
            var documentAndNote = DocumentAndNote.builder().document(testDocument).build();

            List<Element<DocumentAndNote>> documentList = new ArrayList<>();
            documentList.add(Element.<DocumentAndNote>builder().value(documentAndNote).build());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentAndNote(documentList)
                .caseNoteType(CaseNoteType.DOCUMENT_AND_NOTE)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                      .confirmationHeader(header)
                                                                      .confirmationBody(String.format(body))
                                                                      .build());

        }

        @Test
        void submittedCallback_documentOnly() {
            String header = "# Document uploaded \n # " + REFERENCE_NUMBER;
            String body = "## You have uploaded: \n * A Fancy Name\n";

            Document testDocument = new Document("testurl",
                                                 "testBinUrl",
                                                 "A Fancy Name",
                                                 "hash");
            var documentAndNote = DocumentWithName.builder().document(testDocument).build();

            List<Element<DocumentWithName>> documentList = new ArrayList<>();
            documentList.add(Element.<DocumentWithName>builder().value(documentAndNote).build());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentOnly(documentList)
                .caseNoteType(CaseNoteType.DOCUMENT_ONLY)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }
    }

}
