package uk.gov.hmcts.reform.civil.helpers.bundle;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BundleTestUtil {

    private static final String TEST_URL = "url";
    private static final String TEST_FILE_TYPE = "Email";
    private static final String TEST_FILE_NAME = "testFileName.pdf";

    public static CaseData getCaseDataWithNoId() {
        return CaseData.builder().ccdCaseReference(1L)
            .systemGeneratedCaseDocuments(setupSystemGeneratedCaseDocsNoId())
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
            .build();
    }

    public static CaseData getCaseData() {
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

    public CaseData getCaseDataNoCategoryId() {
        return CaseData.builder().ccdCaseReference(1L)
            .documentWitnessStatement(getWitnessDocsCategoryId())
            .documentWitnessStatementApp2(getWitnessDocsCategoryId())
            .documentWitnessStatementRes(getWitnessDocsCategoryId())
            .documentWitnessStatementRes2(getWitnessDocsCategoryId())
            .documentWitnessSummary(getWitnessDocsCategoryId())
            .documentWitnessSummaryApp2(getWitnessDocsCategoryId())
            .documentWitnessSummaryRes(getWitnessDocsCategoryId())
            .documentWitnessSummaryRes2(getWitnessDocsCategoryId())
            .documentHearsayNotice(getWitnessDocsCategoryId())
            .documentHearsayNoticeApp2(getWitnessDocsCategoryId())
            .documentHearsayNoticeRes(getWitnessDocsCategoryId())
            .documentHearsayNoticeRes2(getWitnessDocsCategoryId())
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

    private static ServedDocumentFiles setupParticularsOfClaimDocs() {
        List<Element<Document>> particularsOfClaim = new ArrayList<>();
        Document document = Document.builder().documentFileName(TEST_FILE_NAME).documentUrl(TEST_URL).build();
        particularsOfClaim.add(ElementUtils.element(document));
        List<Element<DocumentWithRegex>> docs = new ArrayList<>();
        DocumentWithRegex doc = DocumentWithRegex.builder().document(Document.builder()
                                                                         .documentFileName(TEST_FILE_NAME)
                                                                         .documentUrl(TEST_URL).build()).build();
        docs.add(ElementUtils.element(doc));
        return ServedDocumentFiles.builder()
            .particularsOfClaimDocument(particularsOfClaim)
            .medicalReport(docs)
            .certificateOfSuitability(docs)
            .scheduleOfLoss(docs)
            .other(docs)
            .build();
    }

    private static List<Element<UploadEvidenceExpert>> getExpertOtherPartyQuestionDocs(String partyName) {
        String expertName;
        String otherParty;
        switch (partyName) {
            case "cl1Fname" -> {
                expertName = "expert3";
                otherParty = "df1Fname";
            }
            case "cl2Fname" -> {
                expertName = "expert4";
                otherParty = "df2Fname";
            }
            case "df1Fname" -> {
                expertName = "expert1";
                otherParty = "cl1Fname";
            }
            default -> {
                expertName = "expert2";
                otherParty = "cl2Fname";
            }
        }
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionOtherParty(otherParty)
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName(expertName).build()));
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionOtherParty("wrong party name")
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName("Other expert").build()));

        return  expertEvidenceDocs;
    }

    private static List<Element<CaseDocument>> getClaimantResponseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            CaseDocument.builder()
                .documentType(DocumentType.CLAIMANT_DEFENCE)
                .createdBy("Claimant")
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).categoryID("").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<CaseDocument>> getDefendantResponseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            CaseDocument.builder()
                .documentType(DocumentType.DEFENDANT_DEFENCE)
                .createdBy("Defendant")
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).categoryID("").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<CaseDocument>> getOrderDoc(DocumentType docType) {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            CaseDocument.builder()
                .documentType(docType)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).categoryID("").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<UploadEvidenceDocumentType>> getDocumentEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        Arrays.stream(TypeOfDocDocumentaryEvidenceOfTrial.values()).toList().forEach(type -> otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                       .typeOfDocument(type.getDisplayNames().get(0))
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build())));
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                       .typeOfDocument("Other")
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        return otherEvidenceDocs;
    }

    private static List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs(String witnessOptionName) {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .witnessOptionName(witnessOptionName)
                                                       .typeOfDocument(TEST_FILE_TYPE)
                                                       .documentUpload(Document.builder().documentBinaryUrl(TEST_URL)
                                                                           .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                       .documentIssuedDate(LocalDate.of(2022, 12, 12))
                                                       .createdDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)).build()));
        return otherEvidenceDocs;
    }

    private static List<Element<UploadEvidenceExpert>> getExpertDocs(String expertName) {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl(TEST_URL)
                                                                            .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                        .expertOptionExpertise("Test")
                                                        .expertOptionExpertises("Test1 Test2")
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName(expertName).build()));

        return expertEvidenceDocs;
    }

    private static List<Element<UploadEvidenceWitness>> getWitnessDocs() {
        List<String> witnessNames = new ArrayList<>(Arrays.asList("cl1Fname", "df1Fname", "cl2Fname", "df2Fname", "FirstName LastName"));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessNames.forEach(witnessName -> witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 TEST_URL)
                                                                                    .documentFileName(TEST_FILE_NAME).build())
                                                         .witnessOptionName(witnessName)
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 2, 10).plusDays(witnessNames.indexOf(witnessName)))
                                                         .createdDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)).build())));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocsCategoryId() {
        List<String> witnessNames = new ArrayList<>(Arrays.asList("cl1Fname", "df1Fname", "cl2Fname", "df2Fname", "FirstName LastName"));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessNames.forEach(witnessName -> witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                                 TEST_URL)
                                                                                    .documentFileName(TEST_FILE_NAME).categoryID("").build())
                                                         .witnessOptionName(witnessName)
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 2, 10).plusDays(witnessNames.indexOf(witnessName)))
                                                         .createdDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)).build())));
        return witnessEvidenceDocs;
    }

    private static List<Element<CaseDocument>> setupSystemGeneratedCaseDocsNoId() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDQDef1 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("ONE").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQApp1 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("TWO").build())
                .createdDatetime(LocalDateTime.of(2023, 3, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef22 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("THREE").build())
                .createdDatetime(LocalDateTime.of(2023, 4, 11, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef21 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("FOUR").build())
                .createdDatetime(LocalDateTime.of(2023, 5, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef23 =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("FIVE").build())
                .createdDatetime(LocalDateTime.of(2023, 6, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef22));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef21));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef23));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<CaseDocument>> setupSystemGeneratedCaseDocs() {
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
        CaseDocument caseDocumentDQNoId =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL)
                                  .documentFileName("DQ_NO_CATEGORY_ID").build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQApp1LiP =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).categoryID(DocCategory.DQ_APP1.getValue())
                                  .documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 3, 11, 2,
                                                  2, 2)).build();
        CaseDocument caseDocumentDQDef1LiP =
            CaseDocument.builder()
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .documentLink(Document.builder().documentUrl(TEST_URL).categoryID(DocCategory.DQ_DEF1.getValue())
                                  .documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 3, 12, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef22));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef21));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef23));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQNoId));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1LiP));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1LiP));
        CaseDocument caseDocumentDJ =
            CaseDocument.builder()
                .documentType(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)
                .documentLink(Document.builder().documentUrl(TEST_URL).documentFileName(TEST_FILE_NAME).build())
                .createdDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2)).build();
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDJ));
        return systemGeneratedCaseDocuments;
    }

}
