package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
class BundleRequestMapperTest {

    @InjectMocks
    private BundleRequestMapper bundleRequestMapper;
    private static final String TEST_URL = "url";
    private static final String TEST_FILE_TYPE = "Email";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    @Test
    void testBundleRequestMapperWithAllDocs() {
        // Given
        //Add all type of documents and other request details in case data
        CaseData caseData = getCaseData();

        // When
        BundleCreateRequest bundleCreateRequest = bundleRequestMapper.mapCaseDataToBundleCreateRequest(caseData, "sample" +
            ".yaml", "test", "test", 1L
        );

        // Then
        assertNotNull(bundleCreateRequest);
        assertEquals("CL 1 Case summary 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("CL 2 Case summary 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(1).getValue().getDocumentFileName());
        assertEquals("DF 1 Case summary 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(2).getValue().getDocumentFileName());
        assertEquals("DF 2 Case summary 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(3).getValue().getDocumentFileName());
        assertEquals("CL 1 Chronology 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(4).getValue().getDocumentFileName());
        assertEquals("CL 2 Chronology 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(5).getValue().getDocumentFileName());
        assertEquals("DF 1 Chronology 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(6).getValue().getDocumentFileName());
        assertEquals("DF 2 Chronology 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(7).getValue().getDocumentFileName());
        assertEquals("CL 1 Trial Timetable 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(8).getValue().getDocumentFileName());
        assertEquals("CL 2 Trial Timetable 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(9).getValue().getDocumentFileName());
        assertEquals("DF 1 Trial Timetable 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(10).getValue().getDocumentFileName());
        assertEquals("DF 2 Trial Timetable 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getTrialDocuments().get(11).getValue().getDocumentFileName());
        assertEquals("Claim Form 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("Particulars Of Claim 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(1).getValue().getDocumentFileName());
        assertEquals("DF 1 Defence 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(2).getValue().getDocumentFileName());
        assertEquals("CL's reply 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(3).getValue().getDocumentFileName());
        assertEquals("CL 1 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(4).getValue().getDocumentFileName());
        assertEquals("CL 2 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(5).getValue().getDocumentFileName());
        assertEquals("DF 1 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(6).getValue().getDocumentFileName());
        assertEquals("DF 2 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(7).getValue().getDocumentFileName());
        assertEquals("CL 1 Directions Questionnaire 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(0).getValue().getDocumentFileName());
        assertEquals("DF 1 Directions Questionnaire 10/02/2023",
                bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(1).getValue().getDocumentFileName());
        assertEquals("DF 2 Directions Questionnaire 10/02/2023",
                bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(2).getValue().getDocumentFileName());
        assertEquals("DF 2 Directions Questionnaire 11/02/2023",
                bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(3).getValue().getDocumentFileName());
        assertEquals("DF 2 Directions Questionnaire 10/03/2023",
                bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(4).getValue().getDocumentFileName());
        assertEquals("Directions Order 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getOrdersDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("Order 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getOrdersDocuments().get(1).getValue().getDocumentFileName());
        assertEquals("Order 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getOrdersDocuments().get(2).getValue().getDocumentFileName());
        assertEquals("CL 1 - Statement 10/02/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(0).getValue().getDocumentFileName());
        assertEquals("CL 2 - Statement 12/02/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant2WitnessStatements().get(0).getValue().getDocumentFileName());
        assertEquals("DF 1 - Statement 11/02/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getDefendant1WitnessStatements().get(0).getValue().getDocumentFileName());
        assertEquals("DF 2 - Statement 13/02/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getDefendant2WitnessStatements().get(0).getValue().getDocumentFileName());
        assertEquals("Witness Statement cl2Fname 1 12/02/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(1).getValue().getDocumentFileName());
        assertEquals("Witness Statement FirstName LastName 1 14/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(2).getValue().getDocumentFileName());
        assertEquals("Witness Statement df2Fname 1 13/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(3).getValue().getDocumentFileName());
        assertEquals("Witness Statement df1Fname 1 11/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(4).getValue().getDocumentFileName());
        assertEquals("Witness Summary cl1Fname 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(5).getValue().getDocumentFileName());
        assertEquals("Email referred to in the statement of witness 12/12/2022",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1WitnessStatements().get(10).getValue().getDocumentFileName());
        assertEquals("Expert Evidence expert1 Test 12/01/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1ExpertEvidence().get(0).getValue().getDocumentFileName());
        assertEquals("Questions to expert1 12/01/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1ExpertEvidence().get(1).getValue().getDocumentFileName());
        assertEquals("Replies from expert1 12/01/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1ExpertEvidence().get(2).getValue().getDocumentFileName());
        assertEquals("Questions to Other expert 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1ExpertEvidence().get(3).getValue().getDocumentFileName());
        assertEquals("Expert Evidence expert2 Test 12/01/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getClaimant2ExpertEvidence().get(0).getValue().getDocumentFileName());
        assertEquals("Expert Evidence expert3 Test 12/01/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getDefendant1ExpertEvidence().get(0).getValue().getDocumentFileName());
        assertEquals("Expert Evidence expert4 Test 12/01/2023",
                      bundleCreateRequest.getCaseDetails().getCaseData().getDefendant2ExpertEvidence().get(0).getValue().getDocumentFileName());
        assertEquals("Joint statement of experts expert5 Test1 Test2 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getJointStatementOfExperts().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant2DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant1DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant2DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1CostsBudgets().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant2CostsBudgets().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant1CostsBudgets().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant2CostsBudgets().get(0).getValue().getDocumentFileName());

    }

    private CaseData getCaseData() {
        return CaseData.builder().ccdCaseReference(1L)
            .documentWitnessStatement(getWitnessDocs())
            .documentWitnessStatementApp2(getWitnessDocs())
            .documentWitnessStatementRes(getWitnessDocs())
            .documentWitnessStatementRes2(getWitnessDocs())
            .documentWitnessSummary(getWitnessDocs())
            .documentWitnessSummaryApp2(getWitnessDocs())
            .documentWitnessSummaryRes(getWitnessDocs())
            .documentWitnessSummaryRes2(getWitnessDocs())
            .documentHearsayNotice(getWitnessDocs())
            .documentHearsayNoticeApp2(getWitnessDocs())
            .documentHearsayNoticeRes(getWitnessDocs())
            .documentHearsayNoticeRes2(getWitnessDocs())
            .documentReferredInStatement(setupOtherEvidenceDocs("witness"))
            .documentReferredInStatementApp2(setupOtherEvidenceDocs("witness"))
            .documentReferredInStatementRes(setupOtherEvidenceDocs("witness"))
            .documentReferredInStatementRes2(setupOtherEvidenceDocs("witness"))
            .documentExpertReport(getExpertDocs("expert1"))
            .documentExpertReportApp2(getExpertDocs("expert2"))
            .documentExpertReportRes(getExpertDocs("expert3"))
            .documentExpertReportRes2(getExpertDocs("expert4"))
            .documentJointStatement(getExpertDocs("expert5"))
            .documentJointStatementApp2(getExpertDocs("expert6"))
            .documentJointStatementRes(getExpertDocs("expert7"))
            .documentJointStatementRes2(getExpertDocs("expert8"))
            .documentAnswers(getExpertDocs("expert1"))
            .documentAnswersApp2(getExpertDocs("expert2"))
            .documentAnswersRes(getExpertDocs("expert3"))
            .documentAnswersRes2(getExpertDocs("expert4"))
            .documentQuestions(getExpertOtherPartyQuestionDocs("cl1Fname"))
            .documentQuestionsApp2(getExpertOtherPartyQuestionDocs("cl2Fname"))
            .documentQuestionsRes(getExpertOtherPartyQuestionDocs("df1Fname"))
            .documentQuestionsRes2(getExpertOtherPartyQuestionDocs("df2Fname"))
            .documentEvidenceForTrial(getDocumentEvidenceForTrial())
            .documentEvidenceForTrialApp2(getDocumentEvidenceForTrial())
            .documentEvidenceForTrialRes(getDocumentEvidenceForTrial())
            .documentEvidenceForTrialRes2(getDocumentEvidenceForTrial())
            .documentCaseSummary(setupOtherEvidenceDocs(null))
            .documentCaseSummaryApp2(setupOtherEvidenceDocs(null))
            .documentCaseSummaryRes(setupOtherEvidenceDocs(null))
            .documentCaseSummaryRes2(setupOtherEvidenceDocs(null))
            .documentForDisclosure(setupOtherEvidenceDocs(null))
            .defendantResponseDocuments(getDefendantResponseDocs())
            .claimantResponseDocuments(getClaimantResponseDocs())
            .dismissalOrderDocStaff(getOrderDoc(DocumentType.DISMISSAL_ORDER))
            .generalOrderDocStaff(getOrderDoc(DocumentType.GENERAL_ORDER))
            .documentCosts(setupOtherEvidenceDocs(null))
            .documentCostsApp2(setupOtherEvidenceDocs(null))
            .documentCostsRes(setupOtherEvidenceDocs(null))
            .documentCostsRes2(setupOtherEvidenceDocs(null))
            .systemGeneratedCaseDocuments(setupSystemGeneratedCaseDocs())
            .applicant1(Party.builder().individualLastName("lastname").individualFirstName("cl1Fname").partyName(
                "applicant1").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().individualLastName("lastname").individualFirstName("df1Fname").partyName(
                "respondent1").type(Party.Type.INDIVIDUAL).build())
            .addApplicant2(YesOrNo.YES)
            .addRespondent2(YesOrNo.YES)
            .applicant2(Party.builder().individualLastName("lastname").individualFirstName("cl2Fname").partyName(
                "applicant2").type(Party.Type.INDIVIDUAL).build())
            .respondent2(Party.builder().individualLastName("lastname").individualFirstName("df2Fname").partyName(
                "respondent2").type(Party.Type.INDIVIDUAL).build())
            .hearingDate(LocalDate.now())
            .submittedDate(LocalDateTime.of(2023, 2, 10, 2,
                2, 2))
            .servedDocumentFiles(setupParticularsOfClaimDocs())
            .build();
    }

    private ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(TEST_FILE_NAME).documentUrl(TEST_URL).build();
        particularsOfClaim.add(ElementUtils.element(document));
        return ServedDocumentFiles.builder().particularsOfClaimDocument(particularsOfClaim).build();
    }

    private List<Element<UploadEvidenceExpert>> getExpertOtherPartyQuestionDocs(String partyName) {
        String expertName = "";
        String otherParty = "";
        if (partyName.equals("cl1Fname")) {
            expertName = "expert3";
            otherParty = "df1Fname";
        } else if (partyName.equals("cl2Fname")) {
            expertName = "expert4";
            otherParty = "df2Fname";
        } else if (partyName.equals("df1Fname")) {
            expertName = "expert1";
            otherParty = "cl1Fname";
        } else {
            expertName = "expert2";
            otherParty = "cl2Fname";
        }
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionOtherParty(otherParty)
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName(expertName).build()));
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionOtherParty("wrong party name")
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName("Other expert").build()));

        return  expertEvidenceDocs;
    }

    private List<Element<CaseDocument>> getClaimantResponseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            CaseDocument.builder()
                .documentType(DocumentType.CLAIMANT_DEFENCE)
                .createdBy("Claimant")
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private List<Element<CaseDocument>> getDefendantResponseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            CaseDocument.builder()
                .documentType(DocumentType.DEFENDANT_DEFENCE)
                .createdBy("Defendant")
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private List<Element<CaseDocument>> getOrderDoc(DocumentType docType) {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            CaseDocument.builder()
                .documentType(docType)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private List<Element<UploadEvidenceDocumentType>> getDocumentEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        Arrays.stream(TypeOfDocDocumentaryEvidenceOfTrial.values()).toList().forEach(type -> {
            otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                           .builder()
                                                           .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                               .documentFileName(TEST_FILE_NAME).build())
                                                           .typeOfDocument(type.getDisplayNames().get(0))
                                                           .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                           .build()));
        });
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build())
                                                       .typeOfDocument("Other")
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        return otherEvidenceDocs;
    }

    private List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs(String witnessOptionName) {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .witnessOptionName(witnessOptionName)
                                                       .typeOfDocument(TEST_FILE_TYPE)
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).build())
                                                       .documentIssuedDate(LocalDate.of(2022, 12, 12))
                                                       .createdDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)).build()));
        return otherEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs(String expertName) {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName(expertName).build()));

        return  expertEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs() {
        List<String> witnessNames = new ArrayList<>(Arrays.asList("cl1Fname", "df1Fname", "cl2Fname", "df2Fname", "FirstName LastName"));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        LocalDateTime createdDateTime = LocalDateTime.of(2023, 12, 12, 8, 8, 5);
        witnessNames.forEach(witnessName -> {
            witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                             .builder()
                                                             .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                     TEST_URL)
                                                                                        .documentFileName(TEST_FILE_NAME).build())
                                                             .witnessOptionName(witnessName)
                                                             .witnessOptionUploadDate(LocalDate.of(2023, 2, 10).plusDays(witnessNames.indexOf(witnessName)))
                                                             .createdDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)).build()));
        });
        return witnessEvidenceDocs;
    }

    private List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentClaim =
            CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).documentLink(Document.builder().documentUrl(
                TEST_URL).documentFileName(TEST_FILE_NAME).categoryID("detailsOfClaim").build()).createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                 2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQDef1 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF1_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQApp1 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).categoryID(DocCategory.APP1_DQ.getValue())
                        .documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef22 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 2, 11, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQDef21 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                2, 2)).build();
        CaseDocument caseDocumentDQDef23 =
                CaseDocument.builder()
                        .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                        .documentLink(Document.builder().documentUrl(TEST_URL)
                                .categoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                .documentFileName(TEST_FILE_NAME).build())
                        .createdDatetime(LocalDateTime.of(2023, 3, 10, 2,
                                2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef22));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef21));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef23));
        CaseDocument caseDocumentDJ =
            CaseDocument.builder()
                .documentType(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDJ));
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
