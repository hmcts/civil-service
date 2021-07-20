package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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

    @Nested
    class BuildCaseNote {
        @BeforeEach
        void setUp() {
            given(idamClient.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);
        }

        @Test
        void shouldBuildNote_whenInvoked() {
            String note = "new note";
            CaseNote caseNote = caseNoteService.buildCaseNote(BEARER_TOKEN, note);

            assertThat(caseNote).isEqualTo(caseNoteForToday(note));
            verify(idamClient).getUserDetails(BEARER_TOKEN);
        }
    }

    @Test
    void shouldAddNoteToList_WhenNullList() {
        CaseNote caseNote = caseNoteForToday("new note");
        List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToList(caseNote, null);

        assertThat(unwrapElements(caseNotes)).contains(caseNote);
    }

    @Test
    void shouldAddNoteToList_WhenEmptyList() {
        CaseNote caseNote = caseNoteForToday("new note");
        List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToList(caseNote, new ArrayList<>());

        assertThat(unwrapElements(caseNotes)).contains(caseNote);
    }

    @Test
    void shouldAddNoteToListWithNewestAtTop_WhenExistingNotes() {
        LocalDate today = LocalDate.now();
        CaseNote newNote = caseNoteWithDate(today);
        CaseNote oldNote = caseNoteWithDate(today.minusDays(5));

        List<Element<CaseNote>> caseNotes = caseNoteService.addNoteToList(newNote, wrapElements(oldNote));

        assertThat(unwrapElements(caseNotes)).isEqualTo(List.of(newNote, oldNote));
    }

    private CaseNote caseNoteForToday(String note) {
        return CaseNote.builder()
            .note(note)
            .createdBy(USER_DETAILS.getFullName())
            .createdOn(LocalDate.now())
            .build();
    }

    private CaseNote caseNoteWithDate(LocalDate date) {
        return CaseNote.builder()
            .note("note")
            .createdBy(USER_DETAILS.getFullName())
            .createdOn(date)
            .build();
    }
}
