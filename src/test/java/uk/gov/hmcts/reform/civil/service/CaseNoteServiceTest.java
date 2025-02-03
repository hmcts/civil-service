package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseNoteServiceTest {

    @InjectMocks
    private CaseNoteService caseNoteService;

    @Mock
    private UserService userService;

    @Mock
    private Time time;
    private final LocalDateTime timeNow = LocalDateTime.of(2023, 10, 9, 1, 1, 1);

    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .forename("John")
        .surname("Smith")
        .build();

    private static final String BEARER_TOKEN = "Bearer Token";

    @Nested
    class BuildCaseNote {

        @Test
        void shouldBuildNote_whenInvoked() {
            when(time.now()).thenReturn(timeNow);
            given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

            String note = "new note";
            CaseNote caseNote = caseNoteService.buildCaseNote(BEARER_TOKEN, note);
            CaseNote expectedNote = caseNoteForToday(note);

            assertCaseNoteEquals(caseNote, expectedNote);
            verify(userService).getUserDetails(BEARER_TOKEN);
        }

        @Test
        void shouldAddNoteToList_WhenNullList() {
            CaseNote caseNote = caseNoteForToday("new note");
            List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToListStart(caseNote, null);

            assertThat(unwrapElements(caseNotes)).contains(caseNote);
        }

        @Test
        void shouldAddNoteToList_WhenEmptyList() {
            CaseNote caseNote = caseNoteForToday("new note");
            List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToListStart(caseNote, new ArrayList<>());

            assertThat(unwrapElements(caseNotes)).contains(caseNote);
        }

        @Test
        void shouldAddNoteToListWithNewestAtTop_WhenExistingNotes() {
            when(time.now()).thenReturn(timeNow);

            LocalDateTime today = time.now();
            CaseNote newNote = caseNoteWithDate(today);
            CaseNote oldNote = caseNoteWithDate(today.minusDays(5));

            List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToListStart(newNote, wrapElements(oldNote));

            assertThat(unwrapElements(caseNotes)).isEqualTo(List.of(newNote, oldNote));
        }

        @Test
        void shouldBuildJudgeNote_whenInvokedAndDocumentAndNote() {
            given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

            Document document = Document.builder().documentFileName("fileName").build();
            DocumentAndNote testDocument = DocumentAndNote.builder().documentName("testDocument").document(document).documentNote("Note").build();

            var builtDoc = caseNoteService.buildJudgeCaseNoteAndDocument(testDocument, BEARER_TOKEN);

            assertDocumentAndNoteEquals(builtDoc.get(0).getValue(), "testDocument", document, "Note");
        }

        @Test
        void shouldBuildJudgeNote_whenInvokedAndDocumentAndName() {
            given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

            Document document = Document.builder().documentFileName("fileName").build();
            DocumentWithName testDocument = DocumentWithName.builder().documentName("testDocument").document(document).build();

            var builtDoc = caseNoteService.buildJudgeCaseNoteDocumentAndName(testDocument, BEARER_TOKEN);

            assertDocumentWithNameEquals(builtDoc.get(0).getValue(), "testDocument", document);
        }
    }

    private void assertCaseNoteEquals(CaseNote actual, CaseNote expected) {
        assertThat(actual.getNote()).isEqualTo(expected.getNote());
        assertThat(actual.getCreatedBy()).isEqualTo(expected.getCreatedBy());
        assertThat(actual.getCreatedOn()).isCloseTo(expected.getCreatedOn(), within(1, ChronoUnit.SECONDS));
    }

    private void assertDocumentAndNoteEquals(DocumentAndNote actual, String expectedName, Document expectedDocument, String expectedNote) {
        assertThat(actual.getDocumentName()).isEqualTo(expectedName);
        assertThat(actual.getDocument()).isEqualTo(expectedDocument);
        assertThat(actual.getDocumentNote()).isEqualTo(expectedNote);
        assertThat(actual.getCreatedBy()).isEqualTo(USER_DETAILS.getFullName());
    }

    private void assertDocumentWithNameEquals(DocumentWithName actual, String expectedName, Document expectedDocument) {
        assertThat(actual.getDocumentName()).isEqualTo(expectedName);
        assertThat(actual.getDocument()).isEqualTo(expectedDocument);
        assertThat(actual.getCreatedBy()).isEqualTo(USER_DETAILS.getFullName());
    }

    private CaseNote caseNoteForToday(String note) {
        return CaseNote.builder()
            .note(note)
            .createdBy(USER_DETAILS.getFullName())
            .createdOn(time.now())
            .build();
    }

    private CaseNote caseNoteWithDate(LocalDateTime timestamp) {
        return CaseNote.builder()
            .note("note")
            .createdBy(USER_DETAILS.getFullName())
            .createdOn(timestamp)
            .build();
    }
}
