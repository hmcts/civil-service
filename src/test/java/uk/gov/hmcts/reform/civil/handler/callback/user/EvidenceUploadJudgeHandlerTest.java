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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class EvidenceUploadJudgeHandlerTest extends BaseCallbackHandlerTest {

    private EvidenceUploadJudgeHandler handler;

    private ObjectMapper objectMapper;

    @Mock
    private CaseNoteService caseNoteService;

    @Mock
    private Time time;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new EvidenceUploadJudgeHandler(objectMapper, caseNoteService);
    }

    public static final String REFERENCE_NUMBER = "000DC001";
    private static final String UPLOAD_TIMESTAMP = "14 Apr 2024 00:00:00";

    @Nested
    class AboutToStartCallback {
        @Test
        void aboutToStartCallback_placeholder() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.NOTE_ONLY);
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
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.NOTE_ONLY);
            caseData.setCaseNoteTA("test note");
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            CaseNote expectedCaseNote = createCaseNote(time.now());
            List<Element<CaseNote>> updatedCaseNotes = wrapElements(expectedCaseNote);

            when(caseNoteService.buildCaseNote(params.getParams().get(BEARER_TOKEN).toString(), "test note"))
                .thenReturn(expectedCaseNote);
            when(caseNoteService.addNoteToListEnd(expectedCaseNote, caseData.getCaseNotes())).thenReturn(updatedCaseNotes);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("caseNotesTA")
                .isEqualTo(objectMapper.convertValue(updatedCaseNotes, new TypeReference<>() {}));
            assertThat(response.getData()).extracting("documentAndName").isNull();
            assertThat(response.getData()).extracting("documentAndNote").isNull();
        }

        @Test
        void shouldCopyDocumentAndNameToAdd_whenDocumentWithNameIsNull() {
            Document document = new Document();
            document.setDocumentFileName("fileName");
            DocumentWithName testDocument = new DocumentWithName();
            testDocument.setDocumentName("testDocument");
            testDocument.setDocument(document);
            testDocument.setCreatedBy("bill bob");
            List<Element<DocumentWithName>> documentWithNameToAdd = wrapElements(testDocument);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.DOCUMENT_ONLY);
            caseData.setDocumentAndNameToAdd(documentWithNameToAdd);
            when(caseNoteService.buildJudgeCaseNoteDocumentAndName(any(), any())).thenReturn(documentWithNameToAdd);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("documentAndName").isEqualTo(objectMapper.convertValue(documentWithNameToAdd, new TypeReference<>() {}));
            assertThat(response.getData()).extracting("caseNotesTA").isNull();
            assertThat(response.getData()).extracting("documentAndNote").isNull();
        }

        @Test
        void shouldAddDocument_whenDocumentWithNameIsNotNull() {
            Document document = new Document();
            document.setDocumentFileName("fileName");
            DocumentWithName testDocument = new DocumentWithName();
            testDocument.setDocumentName("testDocument");
            testDocument.setDocument(document);
            testDocument.setCreatedBy("John Doe");
            List<Element<DocumentWithName>> documentWithNameStart = wrapElements(testDocument);
            List<Element<DocumentWithName>> documentWithNameToAdd = wrapElements(testDocument);
            final List<Element<DocumentWithName>> documentWithNameEnd = wrapElements(testDocument, testDocument);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.DOCUMENT_ONLY);
            caseData.setDocumentAndNameToAdd(documentWithNameToAdd);
            caseData.setDocumentAndName(documentWithNameStart);
            when(caseNoteService.buildJudgeCaseNoteDocumentAndName(any(), any())).thenReturn(documentWithNameToAdd);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("documentAndName").isEqualTo(objectMapper.convertValue(documentWithNameEnd, new TypeReference<>() {}));
            assertThat(response.getData()).extracting("caseNotesTA").isNull();
            assertThat(response.getData()).extracting("documentAndNote").isNull();
        }

        @Test
        void shouldAddNote_whenDocumentWithNoteIsNotNull() {
            Document document = new Document();
            document.setDocumentFileName("fileName");
            DocumentAndNote testDocument = new DocumentAndNote();
            testDocument.setDocumentName("testDocument");
            testDocument.setDocument(document);
            testDocument.setDocumentNote("Note");
            testDocument.setCreatedBy("john smith");
            List<Element<DocumentAndNote>> documentAndNoteToAdd = wrapElements(testDocument);
            List<Element<DocumentAndNote>> documentAndNoteStart = wrapElements(testDocument);
            final List<Element<DocumentAndNote>> documentAndNoteEnd = wrapElements(testDocument, testDocument);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.DOCUMENT_AND_NOTE);
            caseData.setDocumentAndNoteToAdd(documentAndNoteToAdd);
            caseData.setDocumentAndNote(documentAndNoteStart);
            when(caseNoteService.buildJudgeCaseNoteAndDocument(any(), any())).thenReturn(documentAndNoteToAdd);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).extracting("documentAndNote").isEqualTo(objectMapper.convertValue(documentAndNoteEnd, new TypeReference<>() {}));
            assertThat(response.getData()).extracting("caseNotesTA").isNull();
            assertThat(response.getData()).extracting("documentAndName").isNull();
        }

        @Test
        void shouldCopyDocumentAndNoteToAdd_whenDocumentWithNoteIsNull() {
            Document document = new Document();
            document.setDocumentFileName("fileName");
            DocumentAndNote testDocument = new DocumentAndNote();
            testDocument.setDocumentName("testDocument");
            testDocument.setDocument(document);
            testDocument.setDocumentNote("Note");
            testDocument.setCreatedBy("Jason Bourne");
            List<Element<DocumentAndNote>> documentAndNoteToAdd = wrapElements(testDocument);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.DOCUMENT_AND_NOTE);
            caseData.setDocumentAndNoteToAdd(documentAndNoteToAdd);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(caseNoteService.buildJudgeCaseNoteAndDocument(any(), any())).thenReturn(documentAndNoteToAdd);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("documentAndNote").isEqualTo(objectMapper.convertValue(documentAndNoteToAdd, new TypeReference<>() {}));
            assertThat(response.getData()).extracting("caseNotesTA").isNull();
            assertThat(response.getData()).extracting("documentAndName").isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldPopulateConfirmation_DocumentAndNote() {
            final String header = "# Document uploaded and note added \n # " + REFERENCE_NUMBER;
            final String body = "## You have uploaded: \n * A Fancy Name\n";

            Document testDocument = new Document("testurl",
                                                 "testBinUrl",
                                                 "A Fancy Name",
                                                 "hash", null, UPLOAD_TIMESTAMP);
            DocumentAndNote documentAndNote = new DocumentAndNote();
            documentAndNote.setDocument(testDocument);

            List<Element<DocumentAndNote>> documentList = new ArrayList<>();
            documentList.add(Element.<DocumentAndNote>builder().value(documentAndNote).build());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setDocumentAndNoteToAdd(documentList);
            caseData.setCaseNoteType(CaseNoteType.DOCUMENT_AND_NOTE);
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                      .confirmationHeader(header)
                                                                      .confirmationBody(String.format(body))
                                                                      .build());

        }

        @Test
        void submittedCallback_documentOnly() {
            final String header = "# Document uploaded \n # " + REFERENCE_NUMBER;
            final String body = "## You have uploaded: \n * A Fancy Name\n";

            Document testDocument = new Document("testurl",
                                                 "testBinUrl",
                                                 "A Fancy Name",
                                                 "hash", null, UPLOAD_TIMESTAMP);
            DocumentWithName documentAndNote = new DocumentWithName();
            documentAndNote.setDocument(testDocument);

            List<Element<DocumentWithName>> documentList = new ArrayList<>();
            documentList.add(Element.<DocumentWithName>builder().value(documentAndNote).build());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setDocumentAndNameToAdd(documentList);
            caseData.setCaseNoteType(CaseNoteType.DOCUMENT_ONLY);
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

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setCaseNoteType(CaseNoteType.NOTE_ONLY);
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .build());
        }
    }

    private CaseNote createCaseNote(LocalDateTime timeStamp) {
        return CaseNote.builder()
            .createdOn(timeStamp)
            .createdBy("John Doe")
            .note("test note")
            .build();
    }

}
