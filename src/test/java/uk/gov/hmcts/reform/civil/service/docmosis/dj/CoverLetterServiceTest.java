package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.JudgementCoverLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoverLetterServiceTest {

    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    private static final GaCaseDataEnricher GA_CASE_DATA_ENRICHER = new GaCaseDataEnricher();
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private CivilStitchService civilStitchService;
    @InjectMocks
    private CoverLetterService coverLetterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGenerateDocumentWithCoverLetter_whenStitchingEnabled() {
        CaseDocument coverLetter = mock(CaseDocument.class);
        CaseData caseData = gaCaseData(builder -> builder
            .legacyCaseReference("001MC001"));
        Party party = PartyBuilder.builder().individual().build();

        DocumentMetaData metaData = new DocumentMetaData(mock(Document.class), "doc", LocalDate.now().toString());
        CaseDocument stitchedDoc = mock(CaseDocument.class);

        when(coverLetter.getDocumentLink()).thenReturn(mock(Document.class));
        when(documentGeneratorService.generateDocmosisDocument(any(), any())).thenReturn(new DocmosisDocument(
            "file",
            "bytes".getBytes()
        ));
        when(documentManagementService.uploadDocument(anyString(), any(PDF.class))).thenReturn(coverLetter);
        when(civilStitchService.generateStitchedCaseDocument(
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(stitchedDoc);

        CaseDocument result = coverLetterService.generateDocumentWithCoverLetter(
            party,
            caseData,
            List.of(metaData),
            "docName",
            "auth"
        );

        assertThat(result).isEqualTo(stitchedDoc);
    }

    @Test
    void shouldGenerateBinaryDocument_whenStitchingEnabled() {
        byte[] expectedBytes = "pdf-content".getBytes();
        CaseDocument stitchedDoc = CaseDocument.builder().documentLink(Document.builder().documentUrl(
            "http://docstore/documents/1234").documentFileName("file.pdf").build()).build();
        Document document = Document.builder().documentUrl("http://docstore/documents/1234").documentFileName("file.pdf").build();
        DocumentMetaData metaData = new DocumentMetaData(document, "doc", LocalDate.now().toString());
        CaseData caseData = gaCaseData(builder -> builder
            .legacyCaseReference("001MC001"));
        Party party = PartyBuilder.builder().individual().build();

        when(documentManagementService.uploadDocument(anyString(), any(PDF.class))).thenReturn(stitchedDoc);
        when(documentGeneratorService.generateDocmosisDocument(any(), any())).thenReturn(new DocmosisDocument(
            "file",
            expectedBytes
        ));
        when(civilStitchService.generateStitchedCaseDocument(
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(stitchedDoc);
        when(documentDownloadService.downloadDocument(anyString(), anyString()))
            .thenReturn(new DownloadedDocumentResponse(
                new ByteArrayResource(expectedBytes),
                "TEST_DOCUMENT_1.pdf",
                "application/pdf"
            ));
        byte[] result = coverLetterService.generateDocumentWithCoverLetterBinary(
            party,
            caseData,
            List.of(metaData),
            "docName",
            "auth"
        );

        assertThat(result).isEqualTo(expectedBytes);
    }

    @Test
    void shouldThrowException_whenDownloadFails() {
        CaseDocument stitchedDoc = CaseDocument.builder().documentLink(Document.builder().documentUrl(
            "http://docstore/documents/1234").documentFileName("file.pdf").build()).build();
        Document document = Document.builder().documentUrl("http://docstore/documents/1234").documentFileName("file.pdf").build();
        DocumentMetaData metaData = new DocumentMetaData(document, "doc", LocalDate.now().toString());
        CaseData caseData = gaCaseData(builder -> builder
            .legacyCaseReference("001MC001"));
        Party party = PartyBuilder.builder().individual().build();

        when(documentGeneratorService.generateDocmosisDocument(any(), any())).thenReturn(new DocmosisDocument(
            "file",
            "bytes".getBytes()
        ));
        when(documentManagementService.uploadDocument(anyString(), any(PDF.class))).thenReturn(stitchedDoc);
        when(civilStitchService.generateStitchedCaseDocument(
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(stitchedDoc);
        when(documentDownloadService.downloadDocument(any(), any()))
            .thenThrow(new DocumentDownloadException("Download failed", new IOException()));

        assertThatThrownBy(() ->
                               coverLetterService.generateDocumentWithCoverLetterBinary(
                                   party,
                                   caseData,
                                   List.of(metaData),
                                   "docName",
                                   "auth"
                               )
        ).isInstanceOf(DocumentDownloadException.class);
    }

    @Test
    void shouldBuildTemplateDataCorrectly() {
        CaseData caseData = gaCaseData(builder -> builder
            .legacyCaseReference("001MC001"));
        Party party = PartyBuilder.builder().individual().build();

        JudgementCoverLetter result = coverLetterService.buildTemplateData(party, caseData);

        assertThat(result).isEqualTo(
            JudgementCoverLetter.builder()
                .claimNumber(caseData.getLegacyCaseReference())
                .partyName(party.getPartyName())
                .address(party.getPrimaryAddress())
                .build());
    }

    @Test
    void shouldThrowDocumentDownloadException_whenReadAllBytesFails() throws IOException {
        byte[] dummyBytes = "dummy".getBytes();
        CaseDocument stitchedDoc = CaseDocument.builder()
            .documentLink(Document.builder()
                              .documentUrl("http://docstore/documents/1234")
                              .documentFileName("file.pdf")
                              .build())
            .build();

        Document document = Document.builder()
            .documentUrl("http://docstore/documents/1234")
            .documentFileName("file.pdf")
            .build();

        DocumentMetaData metaData = new DocumentMetaData(document, "doc", LocalDate.now().toString());
        CaseData caseData = gaCaseData(builder -> builder
            .legacyCaseReference("001MC001"));
        Party party = PartyBuilder.builder().individual().build();

        when(documentGeneratorService.generateDocmosisDocument(any(), any()))
            .thenReturn(new DocmosisDocument("file", dummyBytes));
        when(documentManagementService.uploadDocument(anyString(), any(PDF.class)))
            .thenReturn(stitchedDoc);
        when(civilStitchService.generateStitchedCaseDocument(any(), any(), any(), any(), any()))
            .thenReturn(stitchedDoc);

        ByteArrayResource resource = mock(ByteArrayResource.class);
        when(resource.getInputStream()).thenThrow(new IOException("Simulated read failure"));
        when(documentDownloadService.downloadDocument(anyString(), anyString()))
            .thenReturn(new DownloadedDocumentResponse(resource, "file.pdf", "application/pdf"));

        assertThatThrownBy(() ->
                               coverLetterService.generateDocumentWithCoverLetterBinary(
                                   party,
                                   caseData,
                                   List.of(metaData),
                                   "docName",
                                   "auth"
                               )
        ).isInstanceOf(DocumentDownloadException.class)
            .hasMessageContaining("file.pdf");
    }

    private CaseData gaCaseData(UnaryOperator<CaseData.CaseDataBuilder<?, ?>> customiser) {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withLocationName("Nottingham County Court and Family Court (and Crown)")
            .withGaCaseManagementLocation(GACaseLocation.builder()
                                              .siteName("testing")
                                              .address("london court")
                                              .baseLocation("000000")
                                              .postcode("BA 117")
                                              .build())
            .build();

        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = GA_CASE_DATA_ENRICHER.enrich(converted, gaCaseData);

        return customiser.apply(enriched.toBuilder()).build();
    }
}
