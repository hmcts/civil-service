package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
class BundleRequestMapperTest {

    @InjectMocks
    private BundleRequestMapper bundleRequestMapper;
    private static final String TEST_URL = "url";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    @Test
    void testBundleRequestMapperWithAllDocs() {
        // Given
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = getWitnessDocs();
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = getExpertDocs();
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = setupOtherEvidenceDocs();
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = setupSystemGeneratedCaseDocs();
        ServedDocumentFiles servedDocumentFiles = setupParticularsOfClaimDocs();
        //Add all type of documents and other request details in case data
        CaseData caseData = getCaseData(witnessEvidenceDocs, expertEvidenceDocs, otherEvidenceDocs,
                                        systemGeneratedCaseDocuments, servedDocumentFiles);

        // When
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
            ".yaml", "test", "test", 1L
        );

        // Then
        assertNotNull(bundleCreateRequest);
    }

    private CaseData getCaseData(List<Element<UploadEvidenceWitness>> witnessEvidenceDocs,
                                 List<Element<UploadEvidenceExpert>> expertEvidenceDocs,
                                 List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs,
                                 List<Element<CaseDocument>> systemGeneratedCaseDocuments,
                                 ServedDocumentFiles servedDocumentFiles) {
        return CaseData.builder().ccdCaseReference(1L)
            .documentWitnessStatement(witnessEvidenceDocs)
            .documentWitnessSummary(witnessEvidenceDocs)
            .documentHearsayNotice(witnessEvidenceDocs)
            .documentReferredInStatement(otherEvidenceDocs)
            .documentWitnessStatementRes(witnessEvidenceDocs)
            .documentWitnessSummaryRes(witnessEvidenceDocs)
            .documentHearsayNoticeRes(witnessEvidenceDocs)
            .documentReferredInStatementRes(otherEvidenceDocs)
            .documentWitnessStatementRes2(witnessEvidenceDocs)
            .documentWitnessSummaryRes2(witnessEvidenceDocs)
            .documentHearsayNoticeRes2(witnessEvidenceDocs)
            .documentReferredInStatementRes2(otherEvidenceDocs)
            .documentExpertReport(expertEvidenceDocs)
            .documentJointStatement(expertEvidenceDocs)
            .documentAnswers(expertEvidenceDocs)
            .documentQuestions(expertEvidenceDocs)
            .documentExpertReportRes(expertEvidenceDocs)
            .documentJointStatementRes(expertEvidenceDocs)
            .documentAnswersRes(expertEvidenceDocs)
            .documentQuestionsRes(expertEvidenceDocs)
            .documentExpertReportRes2(expertEvidenceDocs)
            .documentJointStatementRes2(expertEvidenceDocs)
            .documentAnswersRes2(expertEvidenceDocs)
            .documentQuestionsRes2(expertEvidenceDocs)
            .documentEvidenceForTrial(getDocumentEvidenceForTrial())
            .documentEvidenceForTrialRes(getDocumentEvidenceForTrial())
            .orderSDODocumentDJ(Document.builder().documentFileName("DJ SDO Order")
                                    .documentBinaryUrl(TEST_URL).documentUrl(TEST_URL).build())
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .servedDocumentFiles(servedDocumentFiles)
            .applicant1(Party.builder().individualLastName("lastname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .addApplicant2(YesOrNo.YES)
            .addRespondent2(YesOrNo.YES)
            .applicant2(Party.builder().individualLastName("lastname").partyName("applicant2").type(Party.Type.INDIVIDUAL).build())
            .respondent2(Party.builder().individualLastName("lastname").partyName("respondent2").type(Party.Type.INDIVIDUAL).build())
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .build();
    }

    private List<Element<UploadEvidenceDocumentType>> getDocumentEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build())
                                                       .typeOfDocument(TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames().get(0))
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build())
                                                       .typeOfDocument(TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames().get(0))
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build())
                                                       .typeOfDocument("Agreed Directions")
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        return otherEvidenceDocs;
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(TEST_FILE_NAME).documentUrl(TEST_URL).build();
        particularsOfClaim.add(ElementUtils.element(document));
        return ServedDocumentFiles.builder().particularsOfClaimDocument(particularsOfClaim).build();
    }

    private List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build()).build()));
        return otherEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs() {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName("FirstName LastName").build()));

        return  expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs() {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 TEST_URL)
                                                                                    .documentFileName(TEST_FILE_NAME).build())
                                                         .witnessOptionName("FirstName LastName")
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 2, 10)).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(
                TEST_URL).documentFileName(TEST_FILE_NAME).build()).createdDatetime(LocalDateTime.now()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQ =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.now()).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQ));
        return systemGeneratedCaseDocuments;
    }

    @Test
    void testBundleCreateRequestMapperForEmptyDetails() {
        // Given
        CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .applicant1(Party.builder().individualLastName("lastname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .build();

        // When
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
                                                                                                           ".yaml",
                                                                                                       "test", "test",
                                                                                                       1L
        );
        // Then
        assertNotNull(bundleCreateRequest);
    }

    @Test
    void testBundleCreateRequestMapperForOneRespondentAndOneApplicant() {
        // Given: Casedata with Applicant2 and Respondent2 as NO
        CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .hearingDate(LocalDate.now())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .addApplicant2(YesOrNo.NO)
            .addRespondent2(YesOrNo.NO)
            .applicant1(Party.builder().individualLastName("lastname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").partyName("respondent1").type(Party.Type.INDIVIDUAL).build())
            .build();

        // When: mapCaseDataToBundleCreateRequest is called
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
                                                                                                           ".yaml",
                                                                                                       "test", "test",
                                                                                                       1L
        );
        // Then: hasApplicant2 and hasRespondant2 should return false
        assertEquals(false, bundleCreateRequest.getCaseDetails().getCaseData().isHasApplicant2());
        assertEquals(false, bundleCreateRequest.getCaseDetails().getCaseData().isHasRespondant2());
    }

    @Test
    void shouldFilterEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> list =
            bundleRequestMapper.filterDocumentaryEvidenceForTrialDocs(getDocumentEvidenceForTrial(),
                                                                      TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(), true);
        assertEquals(1, list.size());
    }
}
