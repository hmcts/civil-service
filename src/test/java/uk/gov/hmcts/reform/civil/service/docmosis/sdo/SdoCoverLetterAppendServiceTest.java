package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

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
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoCoverLetter;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private static final Party partyDetails = new Party()
        .setPrimaryAddress(buildAddress())
        .setName("Mr.John White");

    private static final SdoCoverLetter PARTY_LETTER_TEMPLATE_DATA = SdoCoverLetter.builder()
        .party(partyDetails)
        .claimReferenceNumber("MC0001")
        .build();

    private static final CaseDocument caseDocument = new CaseDocument()
        .setDocumentType(SDO_ORDER)
        .setDocumentSize(5L)
        .setDocumentName("DocumentName.pdf")
        .setCreatedBy("CIVIL")
        .setCreatedDatetime(LocalDateTime.of(2024,  1, 2,  3,  4))
        .setDocumentLink(new Document()
                             .setDocumentFileName("DocumentName.pdf")
                             .setDocumentBinaryUrl("Binary/url")
                             .setDocumentUrl("url"));
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
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1L)
            .legacyCaseReference("MC0001")
            .build();

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
        return new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName("Stitched document")
            .setDocumentSize(0L)
            .setDocumentType(SDO_ORDER)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(new Document()
                                 .setDocumentUrl("fake-url")
                                 .setDocumentFileName("file-name")
                                 .setDocumentBinaryUrl("binary-url"));
    }

    private static Address buildAddress() {
        Address address = new Address();
        address.setAddressLine1("456 Avenue");
        address.setPostTown("London");
        address.setPostCode("EX12RT");
        return address;
    }
}
