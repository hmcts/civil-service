package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ConversionToBundleRequestDocsTest {

    @Mock
    private BundleRequestDocsOrganizer requestDocsOrganizer;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @Test
    void shouldConvertOtherWitnessEvidenceToBundleRequestDocs() {
        String displayName = "displayName";
        String documentType = "documentType";
        Party party = Party.builder()
            .partyName("PartyName")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Document witnessDocument = Document.builder()
            .documentUrl("http://example.com/document.pdf")
            .documentBinaryUrl("http://example.com/documentBinary.pdf")
            .documentFileName("document.pdf")
            .categoryID("SomeCategoryID")
            .build();

        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessOptionName1")
            .witnessOptionDocument(witnessDocument)
            .witnessOptionUploadDate(LocalDate.of(2023, 2, 5))
            .build();
        List<Element<UploadEvidenceWitness>> listOfUploadEvidenceWitness =
            List.of(Element.<UploadEvidenceWitness>builder().value(uploadEvidenceWitness).build());

        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementsMap = new HashMap<>();
        witnessStatementsMap.put("Witness1", listOfUploadEvidenceWitness);

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            witnessStatementsMap, displayName, documentType, party);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldConvertWitnessEvidenceToBundleRequestDocs() {
        String fileNamePrefix = "fileNamePrefix";
        String documentType = "documentType";
        boolean isWitnessSelf = true;
        PartyType partyType = PartyType.CLAIMANT1;

        Document witnessDocument = Document.builder()
            .documentUrl("http://example.com/document.pdf")
            .documentBinaryUrl("http://example.com/documentBinary.pdf")
            .documentFileName("document.pdf")
            .categoryID("SomeCategoryID")
            .build();

        UploadEvidenceWitness uploadEvidenceWitness = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessOptionName1")
            .witnessOptionDocument(witnessDocument)
            .witnessOptionUploadDate(LocalDate.of(2023, 2, 5))
            .build();

        List<Element<UploadEvidenceWitness>> listOfUploadEvidenceWitness =
            List.of(Element.<UploadEvidenceWitness>builder().value(uploadEvidenceWitness).build());

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            listOfUploadEvidenceWitness, fileNamePrefix, documentType, partyType, isWitnessSelf);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldConvertEvidenceUploadToBundleRequestDocs() {
        String fileNamePrefix = "fileNamePrefix";
        String documentType = EvidenceUploadType.SKELETON_ARGUMENT.name();
        PartyType partyType = PartyType.CLAIMANT1;

        Document document = Document.builder()
            .documentUrl("http://example.com/document.pdf")
            .documentBinaryUrl("http://example.com/documentBinary.pdf")
            .documentFileName("document.pdf")
            .categoryID("SomeCategoryID")
            .build();

        UploadEvidenceDocumentType uploadEvidenceDocumentType1 = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2023, 4, 24))
            .bundleName("BundleName")
            .documentUpload(document)
            .build();

        UploadEvidenceDocumentType uploadEvidenceDocumentType2 = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2024, 4, 24))
            .documentUpload(document)
            .bundleName("BundleName")
            .build();

        List<Element<UploadEvidenceDocumentType>> listOfUploadDocumentType =
            List.of(
                Element.<UploadEvidenceDocumentType>builder().value(uploadEvidenceDocumentType1).build(),
                Element.<UploadEvidenceDocumentType>builder().value(uploadEvidenceDocumentType2).build()
            );

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            listOfUploadDocumentType, fileNamePrefix, documentType, partyType);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}

