package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BundleRequestDocsOrganizerTest {

    @InjectMocks
    private BundleRequestDocsOrganizer bundleRequestDocsOrganizer;

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    @Test
    void shouldGroupWitnessStatementsByName() {

        List<Element<UploadEvidenceWitness>> witnessEvidenceList = new ArrayList<>();

        UploadEvidenceWitness witness1 = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessA")
            .witnessOptionUploadDate(LocalDate.of(2023, 2, 5))
            .build();
        witnessEvidenceList.add(Element.<UploadEvidenceWitness>builder().value(witness1).build());

        UploadEvidenceWitness witness2 = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessA")
            .witnessOptionUploadDate(LocalDate.of(2023, 3, 10))
            .build();
        witnessEvidenceList.add(Element.<UploadEvidenceWitness>builder().value(witness2).build());

        UploadEvidenceWitness witness3 = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessB")
            .witnessOptionUploadDate(LocalDate.of(2023, 4, 15))
            .build();
        witnessEvidenceList.add(Element.<UploadEvidenceWitness>builder().value(witness3).build());

        Map<String, List<Element<UploadEvidenceWitness>>> result =
            bundleRequestDocsOrganizer.groupWitnessStatementsByName(witnessEvidenceList);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("witnessa"));
        assertTrue(result.containsKey("witnessb"));
        assertEquals(2, result.get("witnessa").size());
        assertEquals(1, result.get("witnessb").size());
    }

    @Test
    void shouldSortWitnessListByDate() {
        UploadEvidenceWitness witness1 = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessA")
            .witnessOptionUploadDate(LocalDate.of(2023, 2, 5))
            .build();

        UploadEvidenceWitness witness2 = UploadEvidenceWitness.builder()
            .witnessOptionName("WitnessB")
            .witnessOptionUploadDate(LocalDate.of(2023, 3, 10))
            .build();

        List<Element<UploadEvidenceWitness>> witnessEvidenceList = new ArrayList<>();
        witnessEvidenceList.add(Element.<UploadEvidenceWitness>builder().value(witness1).build());
        witnessEvidenceList.add(Element.<UploadEvidenceWitness>builder().value(witness2).build());

        bundleRequestDocsOrganizer.sortWitnessListByDate(witnessEvidenceList, false);

        assertEquals(witness2, witnessEvidenceList.get(0).getValue());
        assertEquals(witness1, witnessEvidenceList.get(1).getValue());
    }

    @Test
    void shouldSortExpertListByDate() {

        UploadEvidenceExpert expert1 = UploadEvidenceExpert.builder()
            .expertOptionName("ExpertA")
            .expertOptionUploadDate(LocalDate.of(2023, 2, 5))
            .build();

        UploadEvidenceExpert expert2 = UploadEvidenceExpert.builder()
            .expertOptionName("ExpertB")
            .expertOptionUploadDate(LocalDate.of(2023, 3, 10))
            .build();

        List<Element<UploadEvidenceExpert>> expertEvidenceList = new ArrayList<>();
        expertEvidenceList.add(Element.<UploadEvidenceExpert>builder().value(expert1).build());
        expertEvidenceList.add(Element.<UploadEvidenceExpert>builder().value(expert2).build());

        bundleRequestDocsOrganizer.sortExpertListByDate(expertEvidenceList);

        assertEquals(expert2, expertEvidenceList.get(0).getValue());
        assertEquals(expert1, expertEvidenceList.get(1).getValue());
    }

    @Test
    void shouldGroupExpertStatementsByName() {

        UploadEvidenceExpert expert1 = UploadEvidenceExpert.builder()
            .expertOptionName("ExpertA")
            .expertOptionUploadDate(LocalDate.of(2023, 2, 5))
            .build();

        UploadEvidenceExpert expert2 = UploadEvidenceExpert.builder()
            .expertOptionName("ExpertA")
            .expertOptionUploadDate(LocalDate.of(2023, 3, 10))
            .build();

        UploadEvidenceExpert expert3 = UploadEvidenceExpert.builder()
            .expertOptionName("ExpertB")
            .expertOptionUploadDate(LocalDate.of(2023, 4, 15))
            .build();

        List<Element<UploadEvidenceExpert>> expertEvidenceList = new ArrayList<>();
        expertEvidenceList.add(Element.<UploadEvidenceExpert>builder().value(expert1).build());
        expertEvidenceList.add(Element.<UploadEvidenceExpert>builder().value(expert2).build());
        expertEvidenceList.add(Element.<UploadEvidenceExpert>builder().value(expert3).build());

        Map<String, List<Element<UploadEvidenceExpert>>> result =
            bundleRequestDocsOrganizer.groupExpertStatementsByName(expertEvidenceList);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("experta"));
        assertTrue(result.containsKey("expertb"));
        assertEquals(2, result.get("experta").size());
        assertEquals(1, result.get("expertb").size());
    }

    @Test
    void shouldSortEvidenceUploadByDate() {

        UploadEvidenceDocumentType doc1 = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2023, 2, 5))
            .build();

        UploadEvidenceDocumentType doc2 = UploadEvidenceDocumentType.builder()
            .documentIssuedDate(LocalDate.of(2023, 3, 10))
            .build();

        List<Element<UploadEvidenceDocumentType>> documentList = new ArrayList<>();
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(doc1).build());
        documentList.add(Element.<UploadEvidenceDocumentType>builder().value(doc2).build());

        bundleRequestDocsOrganizer.sortEvidenceUploadByDate(documentList, false);

        assertEquals(doc2, documentList.get(0).getValue());
        assertEquals(doc1, documentList.get(1).getValue());
    }

    @Test
    void shouldFilterEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> list =
            bundleRequestDocsOrganizer.filterDocumentaryEvidenceForTrialDocs(getDocumentEvidenceForTrial(),
                                                                      TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(), true);
        assertEquals(1, list.size());
    }

    private List<Element<UploadEvidenceDocumentType>> getDocumentEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        Arrays.stream(TypeOfDocDocumentaryEvidenceOfTrial.values()).toList().forEach(type -> {
            otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                           .builder()
                                                           .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                               .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                           .typeOfDocument(type.getDisplayNames().get(0))
                                                           .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                           .build()));
        });
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                       .typeOfDocument("Other")
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        return otherEvidenceDocs;
    }
}
