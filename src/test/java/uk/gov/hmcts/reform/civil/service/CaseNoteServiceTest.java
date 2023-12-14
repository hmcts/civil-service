package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.idam.client.IdamClient;
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

@SpringBootTest(classes = {
    CaseNoteService.class,
})
class CaseNoteServiceTest {

    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .forename("John")
        .surname("Smith")
        .build();

    private static final String BEARER_TOKEN = "Bearer Token";

    @Autowired
    private CaseNoteService caseNoteService;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private Time time;
    private LocalDateTime timeNow = LocalDateTime.of(2023, 10, 9, 1, 1, 1);

    @Nested
    class BuildCaseNote {
        @BeforeEach
        void setUp() {

            given(idamClient.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);
            when(time.now()).thenReturn(timeNow);
        }

        @Test
        void shouldBuildNote_whenInvoked() {
            String note = "new note";
            CaseNote caseNote = caseNoteService.buildCaseNote(BEARER_TOKEN, note);

            assertThat(caseNote.getNote()).isEqualTo(caseNoteForToday(note).getNote());
            assertThat(caseNote.getCreatedBy()).isEqualTo(caseNoteForToday(note).getCreatedBy());
            assertThat(caseNote.getCreatedOn()).isCloseTo(
                caseNoteForToday(note).getCreatedOn(),
                within(1, ChronoUnit.SECONDS)
            );
            verify(idamClient).getUserDetails(BEARER_TOKEN);
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
            LocalDateTime today = time.now();
            CaseNote newNote = caseNoteWithDate(today);
            CaseNote oldNote = caseNoteWithDate(today.minusDays(5));

            List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToListStart(newNote, wrapElements(oldNote));

            assertThat(unwrapElements(caseNotes)).isEqualTo(List.of(newNote, oldNote));
        }

        @Test
        void shouldBuildJudgeNote_whenInvokedAndDocumentAndNote() {
            Document document = Document.builder().documentFileName("fileName").build();
            DocumentAndNote testDocument = DocumentAndNote.builder().documentName("testDocument").document(document).documentNote("Note").build();
            when(idamClient.getUserDetails(BEARER_TOKEN)).thenReturn(USER_DETAILS);

            var builtDoc = caseNoteService.buildJudgeCaseNoteAndDocument(testDocument, BEARER_TOKEN);

            assertThat(builtDoc.get(0).getValue().getDocumentName()).isEqualTo("testDocument");;
            assertThat(builtDoc.get(0).getValue().getDocument()).isEqualTo(document);
            assertThat(builtDoc.get(0).getValue().getDocumentNote()).isEqualTo("Note");
            assertThat(builtDoc.get(0).getValue().getCreatedBy()).isEqualTo("John Smith");;
        }

        @Test
        void shouldBuildJudgeNote_whenInvokedAndDocumentAndName() {
            Document document = Document.builder().documentFileName("fileName").build();
            DocumentWithName testDocument = DocumentWithName.builder().documentName("testDocument").document(document).build();
            when(idamClient.getUserDetails(BEARER_TOKEN)).thenReturn(USER_DETAILS);

            var builtDoc = caseNoteService.buildJudgeCaseNoteDocumentAndName(testDocument, BEARER_TOKEN);

            assertThat(builtDoc.get(0).getValue().getDocumentName()).isEqualTo("testDocument");;
            assertThat(builtDoc.get(0).getValue().getDocument()).isEqualTo(document);
            assertThat(builtDoc.get(0).getValue().getCreatedBy()).isEqualTo("John Smith");;
        }

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
