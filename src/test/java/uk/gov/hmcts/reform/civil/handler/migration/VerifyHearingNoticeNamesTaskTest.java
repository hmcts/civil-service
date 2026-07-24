package uk.gov.hmcts.reform.civil.handler.migration;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class VerifyHearingNoticeNamesTaskTest {

    private DocumentDownloadService documentDownloadService;
    private UserService userService;
    private SystemUpdateUserConfiguration userConfig;
    private VerifyHearingNoticeNamesTask task;

    private final Party claimant = new Party()
        .setType(Party.Type.INDIVIDUAL).setIndividualTitle("Mr")
        .setIndividualFirstName("Aslesh").setIndividualLastName("Narra");
    private final Party defendant = new Party()
        .setType(Party.Type.ORGANISATION).setOrganisationName("IQUW Syndicate Management Limited");

    @BeforeEach
    void setUp() {
        documentDownloadService = mock(DocumentDownloadService.class);
        userService = mock(UserService.class);
        userConfig = mock(SystemUpdateUserConfiguration.class);
        task = new VerifyHearingNoticeNamesTask(documentDownloadService, userService, userConfig);

        when(userConfig.getUserName()).thenReturn("system-user");
        when(userConfig.getPassword()).thenReturn("pass");
        when(userService.getAccessToken("system-user", "pass")).thenReturn("Bearer token");
    }

    @Test
    void shouldReportOk_whenAttendeeIsACurrentParty() throws Exception {
        // notice attendee matches the CCD claimant (title-insensitive)
        byte[] pdf = noticeWithAttendee("Aslesh Narra");
        when(documentDownloadService.downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString()))
            .thenReturn(pdf);
        CaseData caseData = caseWithNotice();

        CaseData result = task.migrateCaseData(caseData, caseReference("1732030337525703"));

        assertEquals(caseData, result, "task must return the case unchanged");
        verify(documentDownloadService, times(1))
            .downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString());
    }

    @Test
    void shouldFlagIncorrectAttendee_whenNoticeNamesSomeoneFromAnotherCase() throws Exception {
        // the real defect: header is the CCD parties but the attendee is a foreign name
        byte[] pdf = noticeWithAttendee("Charlie Sansom");
        when(documentDownloadService.downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString()))
            .thenReturn(pdf);
        CaseData caseData = caseWithNotice();

        CaseData result = task.migrateCaseData(caseData, caseReference("1732030337525703"));

        assertEquals(caseData, result, "task must not mutate the case even when an attendee is wrong");
        verify(documentDownloadService, times(1))
            .downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString());
    }

    @Test
    void shouldSkipDownload_whenCaseHasNoHearingNotice() {
        CaseData caseData = CaseData.builder().applicant1(claimant).respondent1(defendant).build();

        CaseData result = task.migrateCaseData(caseData, caseReference("123"));

        assertEquals(caseData, result);
        verify(documentDownloadService, never())
            .downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString());
    }

    @Test
    void shouldSwallowDownloadFailure_andReturnCaseUnchanged() {
        when(documentDownloadService.downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("CDAM unavailable"));
        CaseData caseData = caseWithNotice();

        CaseData result = assertDoesNotThrow(() -> task.migrateCaseData(caseData, caseReference("123")));

        assertEquals(caseData, result);
    }

    @Test
    void shouldThrow_whenCaseReferenceIsNull() {
        CaseData caseData = caseWithNotice();

        Exception exception = assertThrows(IllegalArgumentException.class,
                                           () -> task.migrateCaseData(caseData, null));

        assertEquals("CaseData and CaseReference must not be null", exception.getMessage());
    }

    @Test
    void extractAttendees_pullsNamesUnderTheAttendingHeadings() {
        String text = String.join("\n",
            "in person",
            "Attending in person",
            "Charlie Sansom",
            "The time allocated for the hearing is 1 hour and 30 minutes.");

        assertEquals(List.of("Charlie Sansom"), task.extractAttendees(text));
    }

    @Test
    void extractAttendees_skipsNoticeHeaderBoilerplateInterleavedByPageBreaks() {
        // reproduces the AAT case: a long attendee list crosses a page break, so PDFBox
        // interleaves the repeated notice header (court / claim / date) into the run.
        String text = String.join("\n",
            "Attending in person",
            "Claimant1 Individual",
            "In the county court at Central London",
            "Claim reference 1782920322477820",
            "Claim number 008KA010",
            "Date 07 July 2026",
            "Defendant2Witness2 Witness",
            "The time allocated for the hearing is 1 hour.");

        assertEquals(List.of("Claimant1 Individual", "Defendant2Witness2 Witness"),
                     task.extractAttendees(text));
    }

    @Test
    void extractAttendees_stopsAtTheTrialTerminatorOnNoticeOfTrial() {
        // Notice of Trial says "trial" not "hearing"; without a generalised terminator the
        // parser runs off the end of the attendee list and sweeps in the whole document body.
        String text = String.join("\n",
            "Attending in person",
            "John Doe",
            "Jane Doe",
            "The time allocated for the trial is 1 hour.",
            "The court usually doesn't hear cases between 1pm and 2pm each day.",
            "Trial fees",
            "The trial fee is 619 which must be paid by 30 March 2026.");

        assertEquals(List.of("John Doe", "Jane Doe"), task.extractAttendees(text));
    }

    @Test
    void participantNames_coversPartiesTitleStripped_butNotAForeignName() {
        var participants = task.participantNames(caseWithNotice());

        assertTrue(participants.contains("aslesh narra"), "claimant individual name, title stripped");
        assertTrue(participants.contains("mr aslesh narra".replaceFirst("^mr ", "")));
        assertTrue(participants.contains("iquw syndicate management limited"), "defendant org name");
        assertFalse(participants.contains("charlie sansom"), "a foreign name must not be a participant");
    }

    private CaseData caseWithNotice() {
        CaseDocument notice = new CaseDocument()
            .setDocumentLink(new Document(
                "http://dm-store/documents/abc123", "http://dm-store/documents/abc123/binary",
                "hearing-notice.pdf", "hash", null, null));
        return CaseData.builder()
            .applicant1(claimant)
            .respondent1(defendant)
            .hearingDocuments(wrapElements(notice))
            .build();
    }

    private CaseReference caseReference(String ref) {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(ref);
        return caseReference;
    }

    private byte[] noticeWithAttendee(String attendee) throws Exception {
        List<String> lines = List.of(
            "Notice of Hearing",
            "Mr Aslesh Narra 1st Claimant",
            "IQUW Syndicate Management Limited 1st Defendant",
            "in person",
            "Attending in person",
            attendee,
            "The time allocated for the hearing is 1 hour and 30 minutes.",
            "Hearing fees");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setLeading(16);
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                stream.newLineAtOffset(50, 750);
                for (String line : lines) {
                    stream.showText(line);
                    stream.newLine();
                }
                stream.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
