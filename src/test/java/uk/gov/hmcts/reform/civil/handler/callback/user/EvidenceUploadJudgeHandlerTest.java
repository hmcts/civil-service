package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadJudgeHandler.class,
    JacksonAutoConfiguration.class,
    CaseNoteService.class
})
public class EvidenceUploadJudgeHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EvidenceUploadJudgeHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CaseNoteService caseNoteService;

    @MockBean
    private Time time;

    public static final String REFERENCE_NUMBER = "000DC001";

    @Nested
    class AboutToStartCallback {

        @Test
        void aboutToStartCallback_placeholder() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseNoteType(CaseNoteType.NOTE_ONLY)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).extracting("caseNoteType").isNull();

        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldPopulateNoteDateTime_whenNoteIsAddedToCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseNoteType(CaseNoteType.NOTE_ONLY)
                .caseNoteTA("test note")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            CaseNote expectedCaseNote = createCaseNote(time.now(), "John Doe", "test note");
            List<Element<CaseNote>> updatedCaseNotes = wrapElements(expectedCaseNote);

            when(caseNoteService.buildCaseNote(params.getParams().get(BEARER_TOKEN).toString(), "test note"))
                .thenReturn(expectedCaseNote);
            when(caseNoteService.addNoteToListEnd(expectedCaseNote, caseData.getCaseNotes())).thenReturn(updatedCaseNotes);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("caseNotesTA")
                .isEqualTo(objectMapper.convertValue(updatedCaseNotes, new TypeReference<>() {}));

        }

        @Test
        void shouldNotPopulateNoteDateTime_whenNoteIsAddedToCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseNoteType(CaseNoteType.DOCUMENT_ONLY)
                .caseNoteTA(null)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("caseNotesTA").isNull();

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
                                                 "hash", null);
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
                                                 "hash", null);
            var documentAndNote = DocumentWithName.builder().document(testDocument).build();

            List<Element<DocumentWithName>> documentList = new ArrayList<>();
            documentList.add(Element.<DocumentWithName>builder().value(documentAndNote).build());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentAndName(documentList)
                .caseNoteType(CaseNoteType.DOCUMENT_ONLY)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }

        @Test
        void submittedCallback_noteOnly() {
            String header = "# Case note added \n # " + REFERENCE_NUMBER;

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .caseNoteType(CaseNoteType.NOTE_ONLY)
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .build());
        }
    }

    private CaseNote createCaseNote(LocalDateTime timeStamp, String createdBy, String note) {
        return CaseNote.builder()
            .createdOn(timeStamp)
            .createdBy(createdBy)
            .note(note)
            .build();
    }

}
