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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        .setType(Party.Type.INDIVIDUAL).setIndividualFirstName("John").setIndividualLastName("Smith");
    private final Party defendant = new Party()
        .setType(Party.Type.INDIVIDUAL).setIndividualFirstName("Jane").setIndividualLastName("Doe");

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
    void shouldReportMatch_whenNoticeContainsCurrentNames() throws Exception {
        byte[] pdf = pdfContaining(claimant.getPartyName() + " v " + defendant.getPartyName());
        when(documentDownloadService.downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString()))
            .thenReturn(pdf);
        CaseData caseData = caseWithNotice();

        CaseData result = task.migrateCaseData(caseData, caseReference("123"));

        assertEquals(caseData, result, "task must return the case unchanged");
        verify(documentDownloadService, times(1))
            .downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString());
        verify(userService, times(1)).getAccessToken("system-user", "pass");
    }

    @Test
    void shouldReportStale_whenNoticeMissesACurrentName() throws Exception {
        byte[] pdf = pdfContaining("Old Claimant v Jane Doe");
        when(documentDownloadService.downloadDocument(any(CaseDocument.class), anyString(), anyString(), anyString()))
            .thenReturn(pdf);
        CaseData caseData = caseWithNotice();

        CaseData result = task.migrateCaseData(caseData, caseReference("123"));

        assertEquals(caseData, result, "task must not mutate the case even when the notice is stale");
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

    private byte[] pdfContaining(String text) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
