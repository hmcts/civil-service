package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getExpertDocsByPartyAndDocType;

@ExtendWith(MockitoExtension.class)
class BundleDocumentsRetrievalTest {

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @Mock
    private BundleRequestDocsOrganizer bundleRequestDocsOrganizer;

    @InjectMocks
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Test
    void shouldReturnCorrectParticularsOfClaimName_forSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .issueDate(LocalDate.of(2023, 12, 4))
            .build();

        String result = bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.PARTICULARS_OF_CLAIM);
        assertEquals("Particulars Of Claim 04/12/2023", result);
        result = bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.MEDICAL_REPORT);
        assertEquals("Medical Report 04/12/2023", result);
        result = bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.SCHEDULE_OF_LOSS);
        assertEquals("Schedule Of Loss 04/12/2023", result);
        result = bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.CERTIFICATE_OF_SUITABILITY);
        assertEquals("Certificate Of Suitability 04/12/2023", result);
    }

    @Test
    void shouldGetAllRemainingExpertsQuestions() {

        PartyType partyType = PartyType.CLAIMANT1;
        EvidenceUploadType evidenceUploadFiles = EvidenceUploadType.NOTICE_OF_INTENTION;
        CaseData caseData = CaseData.builder().build();

        Document documentTest = new Document("testUrl", "binUrl",
            "Name", "hash", null,
            "14 Apr 2024 00:00:00"
        );

        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
            .expertOptionName("Expert1")
            .expertDocument(documentTest)
            .build();

        List<Element<UploadEvidenceExpert>> listOfDocs = new ArrayList<>();
        listOfDocs.add(Element.<UploadEvidenceExpert>builder().value(uploadEvidenceExpert).build());

        List<BundlingRequestDocument> expectedConvertedDocs = List.of(
            BundlingRequestDocument.builder()
                .documentFileName("Name")
                .build()
        );

        try (MockedStatic<BundleFileNameHelper> bundleFileNameHelper = mockStatic(BundleFileNameHelper.class)) {
            bundleFileNameHelper.when(() -> getExpertDocsByPartyAndDocType(partyType, evidenceUploadFiles, caseData))
                .thenReturn(listOfDocs);

            when(conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
                eq(listOfDocs),
                eq(BundleFileNameList.QUESTIONS_TO.getDisplayName()),
                eq(EvidenceUploadType.QUESTIONS_FOR_EXPERTS.name())
            )).thenReturn(expectedConvertedDocs);

            List<BundlingRequestDocument> result =
                bundleDocumentsRetrieval.getAllRemainingExpertQuestions(partyType, evidenceUploadFiles, caseData);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(expectedConvertedDocs, result);
        }
    }

    @Test
    void shouldGetAllOtherPartyQuestions() {

        Document documentTest = new Document("testUrl", "binUrl",
            "Name", "hash", null,
            "14 Apr 2024 00:00:00"
        );

        UploadEvidenceExpert uploadEvidenceExpert = UploadEvidenceExpert.builder()
            .expertOptionName("Expert1")
            .expertOptionOtherParty("James Gordon")
            .expertDocument(documentTest)
            .build();

        List<Element<UploadEvidenceExpert>> listOfDocs = new ArrayList<>();
        listOfDocs.add(Element.<UploadEvidenceExpert>builder().value(uploadEvidenceExpert).build());

        Map<String, List<Element<UploadEvidenceExpert>>> expectedGroupedDocuments = new HashMap<>();
        expectedGroupedDocuments.put("Expert1", listOfDocs);

        List<BundlingRequestDocument> expectedBundlingRequestDocs = List.of(
            BundlingRequestDocument.builder()
                .documentFileName("Name")
                .build()
        );

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().individualFirstName("James").individualLastName("Gordon").type(Party.Type.INDIVIDUAL).build())
            .documentExpertReport(new ArrayList<>())
            .documentQuestionsRes2(listOfDocs)
            .build();

        Set<String> allExpertsNames = Set.of("Expert1");
        PartyType partyType = PartyType.CLAIMANT1;

        when(bundleRequestDocsOrganizer.groupExpertStatementsByName(anyList()))
            .thenReturn(expectedGroupedDocuments);

        when(conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
            any(), anyString(), anyString()))
            .thenReturn(expectedBundlingRequestDocs);

        List<BundlingRequestDocument> result =
            bundleDocumentsRetrieval.getAllOtherPartyQuestions(partyType, caseData, allExpertsNames);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetAllExpertReports() {

        Document documentTest1 = new Document("testUrl1", "binUrl1", "Name1", "hash1", null, "14 Apr 2024 00:00:00");

        UploadEvidenceExpert uploadEvidenceExpert1 = UploadEvidenceExpert.builder()
            .expertOptionName("Expert1")
            .expertDocument(documentTest1)
            .build();

        List<Element<UploadEvidenceExpert>> listOfDocs = new ArrayList<>();
        listOfDocs.add(Element.<UploadEvidenceExpert>builder().value(uploadEvidenceExpert1).build());

        Map<String, List<Element<UploadEvidenceExpert>>> expectedGroupedDocuments = new HashMap<>();
        expectedGroupedDocuments.put("Expert1", listOfDocs);

        List<BundlingRequestDocument> expectedBundlingRequestDocs = List.of(
            BundlingRequestDocument.builder()
                .documentFileName("Name1")
                .build()
        );

        PartyType partyType = PartyType.CLAIMANT1;
        EvidenceUploadType evidenceUploadFiles = EvidenceUploadType.EXPERT_REPORT;
        CaseData caseData = CaseData.builder().build();
        BundleFileNameList bundleFileNameList = BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME;
        Set<String> allExpertsNames = Set.of("Expert1", "Expert2");

        when(bundleRequestDocsOrganizer.groupExpertStatementsByName(anyList()))
            .thenReturn(expectedGroupedDocuments);

        when(conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
            any(), anyString(), anyString()))
            .thenReturn(expectedBundlingRequestDocs);

        List<BundlingRequestDocument> result = bundleDocumentsRetrieval.getAllExpertReports(
            partyType, evidenceUploadFiles, caseData, bundleFileNameList, allExpertsNames);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedBundlingRequestDocs, result);
    }
}
