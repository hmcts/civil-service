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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
        Party party = new Party()
            .setPartyName("PartyName")
            .setType(Party.Type.INDIVIDUAL);

        Document witnessDocument = new Document()
            .setDocumentUrl("https://example.com/document.pdf")
            .setDocumentBinaryUrl("https://example.com/documentBinary.pdf")
            .setDocumentFileName("document.pdf")
            .setCategoryID("SomeCategoryID");

        UploadEvidenceWitness uploadEvidenceWitness = new UploadEvidenceWitness()
            .setWitnessOptionName("WitnessOptionName1")
            .setWitnessOptionDocument(witnessDocument)
            .setWitnessOptionUploadDate(LocalDate.of(2023, 2, 5));
        List<Element<UploadEvidenceWitness>> listOfUploadEvidenceWitness =
            List.of(new Element<UploadEvidenceWitness>().setValue(uploadEvidenceWitness));

        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementsMap = new HashMap<>();
        witnessStatementsMap.put("Witness1", listOfUploadEvidenceWitness);

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            witnessStatementsMap, displayName, documentType, party);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldPreserveOriginalWitnessIndexWhenFilteringOtherWitnessEvidence() {
        when(featureToggleService.isAmendBundleEnabled()).thenReturn(true);
        String displayName = "%s %s %s";
        String documentType = "documentType";

        Element<UploadEvidenceWitness> unbundledWitness = new Element<UploadEvidenceWitness>().setValue(
            new UploadEvidenceWitness()
                .setWitnessOptionName("Witness One")
                .setWitnessOptionDocument(new Document()
                                              .setDocumentUrl("https://example.com/unbundled.pdf")
                                              .setDocumentBinaryUrl("https://example.com/unbundledBinary.pdf")
                                              .setDocumentFileName("unbundled.pdf")
                                              .setCategoryID("UnbundledFolder"))
                .setWitnessOptionUploadDate(LocalDate.of(2023, 2, 5))
        );
        Element<UploadEvidenceWitness> bundledWitness = new Element<UploadEvidenceWitness>().setValue(
            new UploadEvidenceWitness()
                .setWitnessOptionName("Witness Two")
                .setWitnessOptionDocument(new Document()
                                              .setDocumentUrl("https://example.com/bundled.pdf")
                                              .setDocumentBinaryUrl("https://example.com/bundledBinary.pdf")
                                              .setDocumentFileName("bundled.pdf")
                                              .setCategoryID("SomeCategoryID"))
                .setWitnessOptionUploadDate(LocalDate.of(2023, 2, 6))
        );

        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementsMap = new HashMap<>();
        witnessStatementsMap.put("Witness1", List.of(unbundledWitness, bundledWitness));

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            witnessStatementsMap, displayName, documentType, null);

        assertEquals(1, result.size());
        assertEquals("Witness Two 2 06/02/2023", result.getFirst().getDocumentFileName());
    }

    @Test
    void shouldConvertWitnessEvidenceToBundleRequestDocs() {
        String fileNamePrefix = "fileNamePrefix";
        String documentType = "documentType";
        boolean isWitnessSelf = true;
        PartyType partyType = PartyType.CLAIMANT1;

        Document witnessDocument = new Document()
            .setDocumentUrl("https://example.com/document.pdf")
            .setDocumentBinaryUrl("https://example.com/documentBinary.pdf")
            .setDocumentFileName("document.pdf")
            .setCategoryID("SomeCategoryID");

        UploadEvidenceWitness uploadEvidenceWitness = new UploadEvidenceWitness()
            .setWitnessOptionName("WitnessOptionName1")
            .setWitnessOptionDocument(witnessDocument)
            .setWitnessOptionUploadDate(LocalDate.of(2023, 2, 5));

        List<Element<UploadEvidenceWitness>> listOfUploadEvidenceWitness =
            List.of(new Element<UploadEvidenceWitness>().setValue(uploadEvidenceWitness));

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

        Document document = new Document()
            .setDocumentUrl("https://example.com/document.pdf")
            .setDocumentBinaryUrl("https://example.com/documentBinary.pdf")
            .setDocumentFileName("document.pdf")
            .setCategoryID("SomeCategoryID");

        UploadEvidenceDocumentType uploadEvidenceDocumentType1 = new UploadEvidenceDocumentType()
            .setDocumentIssuedDate(LocalDate.of(2023, 4, 24))
            .setBundleName("BundleName")
            .setDocumentUpload(document);

        UploadEvidenceDocumentType uploadEvidenceDocumentType2 = new UploadEvidenceDocumentType()
            .setDocumentIssuedDate(LocalDate.of(2024, 4, 24))
            .setDocumentUpload(document)
            .setBundleName("BundleName");

        List<Element<UploadEvidenceDocumentType>> listOfUploadDocumentType =
            List.of(
                new Element<UploadEvidenceDocumentType>().setValue(uploadEvidenceDocumentType1),
                new Element<UploadEvidenceDocumentType>().setValue(uploadEvidenceDocumentType2)
            );

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            listOfUploadDocumentType, fileNamePrefix, documentType, partyType);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReadPartyBeforeDocumentsReferredBranch() {
        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType()
            .setDocumentIssuedDate(LocalDate.of(2023, 4, 24))
            .setDocumentUpload(new Document()
                                   .setDocumentUrl("https://example.com/document.pdf")
                                   .setDocumentBinaryUrl("https://example.com/documentBinary.pdf")
                                   .setDocumentFileName("document.pdf")
                                   .setCategoryID("SomeCategoryID"));
        List<Element<UploadEvidenceDocumentType>> evidenceUploadDocuments =
            List.of(new Element<UploadEvidenceDocumentType>().setValue(uploadEvidenceDocumentType));
        String fileNamePrefix = "%s %s %s";
        String documentType = EvidenceUploadType.DOCUMENTS_REFERRED.name();

        assertThrows(
            NullPointerException.class,
            () -> conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                evidenceUploadDocuments,
                fileNamePrefix,
                documentType,
                null
            )
        );
    }

    @Test
    void shouldUseOriginalDocumentFileNameWhenRequested() {
        String fileNamePrefix = "DOC_FILE_NAME";
        String documentType = EvidenceUploadType.COSTS.name();
        PartyType partyType = PartyType.CLAIMANT1;

        Document document = new Document()
            .setDocumentUrl("https://example.com/document.pdf")
            .setDocumentBinaryUrl("https://example.com/documentBinary.pdf")
            .setDocumentFileName("document.pdf")
            .setCategoryID("SomeCategoryID");

        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType()
            .setDocumentIssuedDate(LocalDate.of(2023, 4, 24))
            .setDocumentUpload(document);

        List<Element<UploadEvidenceDocumentType>> listOfUploadDocumentType =
            List.of(new Element<UploadEvidenceDocumentType>().setValue(uploadEvidenceDocumentType));

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            listOfUploadDocumentType, fileNamePrefix, documentType, partyType);

        assertNotNull(result);
        assertEquals("document", result.getFirst().getDocumentFileName());
    }

    @Test
    void shouldUseOriginalDocumentFileNameWithDateWhenRequested() {
        String fileNamePrefix = "DOC_FILE_NAME %s";
        String documentType = EvidenceUploadType.COSTS.name();
        PartyType partyType = PartyType.CLAIMANT1;

        Document document = new Document()
            .setDocumentUrl("https://example.com/document.pdf")
            .setDocumentBinaryUrl("https://example.com/documentBinary.pdf")
            .setDocumentFileName("document.pdf")
            .setCategoryID("SomeCategoryID");

        UploadEvidenceDocumentType uploadEvidenceDocumentType = new UploadEvidenceDocumentType()
            .setDocumentIssuedDate(LocalDate.of(2023, 4, 24))
            .setCreatedDatetime(LocalDateTime.of(2023, 4, 30, 10, 0))
            .setDocumentUpload(document);

        List<Element<UploadEvidenceDocumentType>> listOfUploadDocumentType =
            List.of(new Element<UploadEvidenceDocumentType>().setValue(uploadEvidenceDocumentType));

        List<BundlingRequestDocument> result = conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            listOfUploadDocumentType, fileNamePrefix, documentType, partyType);

        assertNotNull(result);
        assertEquals("document 30/04/2023", result.getFirst().getDocumentFileName());
    }
}
