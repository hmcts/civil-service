package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoCoverLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_COVER_LETTER;

@ExtendWith(MockitoExtension.class)
class SdoCoverLetterAppendServiceTest {

    private static final ObjectMapper GA_OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());
    private final GaCaseDataEnricher gaCaseDataEnricher = new GaCaseDataEnricher();

    @InjectMocks
    private SdoCoverLetterAppendService coverLetterAppendService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private CivilStitchService civilStitchService;

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentDownloadService documentDownloadService;

    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final Party partyDetails = Party.builder()
        .primaryAddress(Address.builder()
                .addressLine1("456 Avenue")
                .postTown("London")
                .postCode("EX12RT")
                .build())
        .name("Mr.John White")
        .build();

    private static final SdoCoverLetter PARTY_LETTER_TEMPLATE_DATA = SdoCoverLetter.builder()
        .party(partyDetails)
        .claimReferenceNumber("MC0001")
        .build();

    private static final CaseDocument caseDocument = CaseDocument.builder()
        .documentType(SDO_ORDER)
        .documentSize(5L)
        .documentName("DocumentName.pdf")
        .createdBy("CIVIL")
        .createdDatetime(LocalDateTime.of(2024,  1, 2,  3,  4))
        .documentLink(Document.builder().documentFileName("DocumentName.pdf").documentBinaryUrl("Binary/url").documentUrl("url").build())
        .build();
    private static final byte[] STITCHED_DOC_BYTES = new byte[]{1, 2, 3, 4};

    private List<DocumentMetaData> specClaimTimelineDocuments;

    @BeforeEach
    void setup() {
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
                .willReturn(DocmosisDocument.builder().bytes(new byte[]{1, 2, 3, 4, 5, 6}).build());
        given(documentManagementService.uploadDocument(any(), any(PDF.class))).willReturn(caseDocument);
        byte[] bytes = new ByteArrayResource(STITCHED_DOC_BYTES).getByteArray();
        given(documentDownloadService.downloadDocument(
            any(), any(), anyString(), anyString()
        )).willReturn(bytes);
        when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(SDO_ORDER),
                                                             anyString())).thenReturn(buildStitchedDocument());

        specClaimTimelineDocuments = List.of(
            new DocumentMetaData(caseDocument.getDocumentLink(), "Cover letter", LocalDate.now().toString()),
            new DocumentMetaData(caseDocument.getDocumentLink(), "SDO Document to attach", LocalDate.now().toString())
        );
    }

    @Test
    void shouldGenerateMailableLetterSuccessfully() {
        // Given
        CaseData caseData = gaCaseData(builder -> builder
            .ccdCaseReference(1L)
            .legacyCaseReference("MC0001"));

        // When
        byte[] mailableLetter = coverLetterAppendService.makeSdoDocumentMailable(caseData, BEARER_TOKEN, partyDetails, SDO_ORDER,
                                                                              caseDocument
        );

        // Then
        assertThat(mailableLetter).isEqualTo(STITCHED_DOC_BYTES);
        verify(documentGeneratorService).generateDocmosisDocument(PARTY_LETTER_TEMPLATE_DATA, SDO_COVER_LETTER);
        verify(civilStitchService).generateStitchedCaseDocument(specClaimTimelineDocuments, "DocumentName.pdf", 1L,
            SDO_ORDER, BEARER_TOKEN);
    }

    private CaseDocument buildStitchedDocument() {
        return CaseDocument.builder()
            .createdBy("John")
            .documentName("Stitched document")
            .documentSize(0L)
            .documentType(SDO_ORDER)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
    }

    private CaseData gaCaseData(UnaryOperator<CaseData.CaseDataBuilder<?, ?>> customiser) {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withCcdCaseReference(CaseDataBuilder.CASE_ID)
            .withGeneralAppParentCaseReference(CaseDataBuilder.PARENT_CASE_ID)
            .withLocationName("Nottingham County Court and Family Court (and Crown)")
            .withGaCaseManagementLocation(GACaseLocation.builder()
                                              .siteName("testing")
                                              .address("london court")
                                              .baseLocation("2")
                                              .postcode("BA 117")
                                              .build())
            .build();

        CaseData converted = GA_OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        CaseData enriched = gaCaseDataEnricher.enrich(converted, gaCaseData);

        return customiser.apply(enriched.toBuilder()).build();
    }
}
