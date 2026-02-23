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
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BundleTestUtil {

    private BundleTestUtil() {
    }

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

    public static CaseData getCaseDataNoCategoryId() {
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
        Document document = new Document().setDocumentFileName(TEST_FILE_NAME).setDocumentUrl(TEST_URL);
        particularsOfClaim.add(ElementUtils.element(document));
        List<Element<DocumentWithRegex>> docs = new ArrayList<>();
        DocumentWithRegex doc = new DocumentWithRegex(new Document().setDocumentFileName(TEST_FILE_NAME).setDocumentUrl(TEST_URL));
        docs.add(ElementUtils.element(doc));
        return new ServedDocumentFiles()
            .setParticularsOfClaimDocument(particularsOfClaim)
            .setMedicalReport(docs)
            .setCertificateOfSuitability(docs)
            .setScheduleOfLoss(docs)
            .setOther(docs);
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
        expertEvidenceDocs.add(ElementUtils.element(new UploadEvidenceExpert()
                                                        .setExpertDocument(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                              .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                        .setExpertOptionExpertise("Test")
                                                        .setExpertOptionOtherParty(otherParty)
                                                        .setExpertOptionExpertises("Test1 Test2")
                                                        .setExpertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .setExpertOptionName(expertName)));
        expertEvidenceDocs.add(ElementUtils.element(new UploadEvidenceExpert()
                                                        .setExpertDocument(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                              .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                        .setExpertOptionExpertise("Test")
                                                        .setExpertOptionOtherParty("wrong party name")
                                                        .setExpertOptionExpertises("Test1 Test2")
                                                        .setExpertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .setExpertOptionName("Other expert")));

        return  expertEvidenceDocs;
    }

    private static List<Element<CaseDocument>> getClaimantResponseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            new CaseDocument()
                .setDocumentType(DocumentType.CLAIMANT_DEFENCE)
                .setCreatedBy("Claimant")
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<CaseDocument>> getDefendantResponseDocs() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            new CaseDocument()
                .setDocumentType(DocumentType.DEFENDANT_DEFENCE)
                .setCreatedBy("Defendant")
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<CaseDocument>> getOrderDoc(DocumentType docType) {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDC =
            new CaseDocument()
                .setDocumentType(docType)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDC));
        return systemGeneratedCaseDocuments;
    }

    private static List<Element<UploadEvidenceDocumentType>> getDocumentEvidenceForTrial() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        Arrays.stream(TypeOfDocDocumentaryEvidenceOfTrial.values()).toList().forEach(type -> otherEvidenceDocs.add(ElementUtils.element(new UploadEvidenceDocumentType()
                                                       .setDocumentUpload(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                           .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                       .setTypeOfDocument(type.getDisplayNames().get(0))
                                                       .setDocumentIssuedDate(LocalDate.of(2023, 1, 12)))));
        otherEvidenceDocs.add(ElementUtils.element(new UploadEvidenceDocumentType()
                                                       .setDocumentUpload(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                           .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                       .setTypeOfDocument("Other")
                                                       .setDocumentIssuedDate(LocalDate.of(2023, 1, 12))));
        return otherEvidenceDocs;
    }

    private static List<Element<UploadEvidenceDocumentType>> setupOtherEvidenceDocs(String witnessOptionName) {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(new UploadEvidenceDocumentType()
                                                       .setWitnessOptionName(witnessOptionName)
                                                       .setTypeOfDocument(TEST_FILE_TYPE)
                                                       .setDocumentUpload(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                           .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                       .setDocumentIssuedDate(LocalDate.of(2022, 12, 12))
                                                       .setCreatedDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5))));
        return otherEvidenceDocs;
    }

    private static List<Element<UploadEvidenceExpert>> getExpertDocs(String expertName) {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(new UploadEvidenceExpert()
                                                        .setExpertDocument(new Document().setDocumentBinaryUrl(TEST_URL)
                                                                              .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                        .setExpertOptionExpertise("Test")
                                                        .setExpertOptionExpertises("Test1 Test2")
                                                        .setExpertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .setExpertOptionName(expertName)));

        return expertEvidenceDocs;
    }

    private static List<Element<UploadEvidenceWitness>> getWitnessDocs() {
        List<String> witnessNames = new ArrayList<>(Arrays.asList("cl1Fname", "df1Fname", "cl2Fname", "df2Fname", "FirstName LastName"));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessNames.forEach(witnessName -> witnessEvidenceDocs.add(ElementUtils.element(new UploadEvidenceWitness()
                                                         .setWitnessOptionDocument(new Document().setDocumentBinaryUrl(
                                                             TEST_URL)
                                                                                         .setDocumentFileName(TEST_FILE_NAME))
                                                         .setWitnessOptionName(witnessName)
                                                         .setWitnessOptionUploadDate(LocalDate.of(2023, 2, 10).plusDays(witnessNames.indexOf(witnessName)))
                                                         .setCreatedDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)))));
        return witnessEvidenceDocs;
    }

    private static List<Element<UploadEvidenceWitness>> getWitnessDocsCategoryId() {
        List<String> witnessNames = new ArrayList<>(Arrays.asList("cl1Fname", "df1Fname", "cl2Fname", "df2Fname", "FirstName LastName"));
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessNames.forEach(witnessName -> witnessEvidenceDocs.add(ElementUtils.element(new UploadEvidenceWitness()
                                                         .setWitnessOptionDocument(new Document().setDocumentBinaryUrl(
                                                             TEST_URL)
                                                                                         .setDocumentFileName(TEST_FILE_NAME).setCategoryID(""))
                                                         .setWitnessOptionName(witnessName)
                                                         .setWitnessOptionUploadDate(LocalDate.of(2023, 2, 10).plusDays(witnessNames.indexOf(witnessName)))
                                                         .setCreatedDatetime(LocalDateTime.of(2023, 12, 12, 8, 8, 5)))));
        return witnessEvidenceDocs;
    }

    private static List<Element<CaseDocument>> setupSystemGeneratedCaseDocsNoId() {
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        CaseDocument caseDocumentDQDef1 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setDocumentFileName("ONE"))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQApp1 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setDocumentFileName("TWO"))
                .setCreatedDatetime(LocalDateTime.of(2023, 3, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef22 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setDocumentFileName("THREE"))
                .setCreatedDatetime(LocalDateTime.of(2023, 4, 11, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef21 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setDocumentFileName("FOUR"))
                .setCreatedDatetime(LocalDateTime.of(2023, 5, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef23 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setDocumentFileName("FIVE"))
                .setCreatedDatetime(LocalDateTime.of(2023, 6, 10, 2,
                                                  2, 2));
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
            new CaseDocument().setDocumentType(DocumentType.SEALED_CLAIM).setDocumentLink(new Document().setDocumentUrl(
                TEST_URL).setDocumentFileName(TEST_FILE_NAME).setCategoryID("detailsOfClaim")).setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                                                                                                  2, 2));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentClaim));
        CaseDocument caseDocumentDQDef1 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setCategoryID(DocCategory.DEF1_DEFENSE_DQ.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQApp1 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setCategoryID(DocCategory.APP1_DQ.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef22 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setCategoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 11, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef21 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setCategoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef23 =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setCategoryID(DocCategory.DEF2_DEFENSE_DQ.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 3, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQNoId =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL)
                                  .setDocumentFileName("DQ_NO_CATEGORY_ID"))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQApp1LiP =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setCategoryID(DocCategory.DQ_APP1.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 3, 11, 2,
                                                  2, 2));
        CaseDocument caseDocumentDQDef1LiP =
            new CaseDocument()
                .setDocumentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setCategoryID(DocCategory.DQ_DEF1.getValue())
                                  .setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 3, 12, 2,
                                                  2, 2));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef22));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef21));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef23));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQNoId));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQApp1LiP));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDQDef1LiP));
        CaseDocument caseDocumentDJ =
            new CaseDocument()
                .setDocumentType(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)
                .setDocumentLink(new Document().setDocumentUrl(TEST_URL).setDocumentFileName(TEST_FILE_NAME))
                .setCreatedDatetime(LocalDateTime.of(2023, 2, 10, 2,
                                                  2, 2));
        systemGeneratedCaseDocuments.add(ElementUtils.element(caseDocumentDJ));
        return systemGeneratedCaseDocuments;
    }

    public static void assertTrialDocumentFileNames(final BundleCreateRequest bundleCreateRequest) {
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
    }

    public static void assertStatementsOfCaseDocuments(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("Claim Form 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("Particulars Of Claim 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(1).getValue().getDocumentFileName());
        assertEquals("Medical Report 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(2).getValue().getDocumentFileName());
        assertEquals("Schedule Of Loss 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(3).getValue().getDocumentFileName());
        assertEquals("Certificate Of Suitability 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(4).getValue().getDocumentFileName());
        assertEquals("Other 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(5).getValue().getDocumentFileName());
        assertEquals("DF 1 Defence 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(6).getValue().getDocumentFileName());
        assertEquals("CL's reply 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(7).getValue().getDocumentFileName());
        assertEquals("CL 1 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(8).getValue().getDocumentFileName());
        assertEquals("CL 2 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(9).getValue().getDocumentFileName());
        assertEquals("DF 1 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(10).getValue().getDocumentFileName());
        assertEquals("DF 2 reply to part 18 request 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getStatementsOfCaseDocuments().get(11).getValue().getDocumentFileName());
    }

    public static void assertDirectionsQuestionnairesWithCategoryIds(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("CL 1 Directions Questionnaire 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(0).getValue().getDocumentFileName());
        assertEquals("DF 1 Directions Questionnaire 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(1).getValue().getDocumentFileName());
        assertEquals("DF 1 Directions Questionnaire 12/03/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(2).getValue().getDocumentFileName());
        assertEquals("DF 2 Directions Questionnaire 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(3).getValue().getDocumentFileName());
        assertEquals("DF 2 Directions Questionnaire 11/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(4).getValue().getDocumentFileName());
        assertEquals("DF 2 Directions Questionnaire 10/03/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(5).getValue().getDocumentFileName());
        assertEquals("Directions Questionnaire 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(6).getValue().getDocumentFileName());
    }

    public static void assertDirectionsQuestionnaires(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("Directions Questionnaire 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(0).getValue().getDocumentFileName());
        assertEquals("Directions Questionnaire 10/03/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(1).getValue().getDocumentFileName());
        assertEquals("Directions Questionnaire 11/04/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(2).getValue().getDocumentFileName());
        assertEquals("Directions Questionnaire 10/05/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(3).getValue().getDocumentFileName());
        assertEquals("Directions Questionnaire 10/06/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDirectionsQuestionnaires().get(4).getValue().getDocumentFileName());
    }

    public static void assertOrdersDocuments(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("Directions Order 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getOrdersDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("Order 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getOrdersDocuments().get(1).getValue().getDocumentFileName());
        assertEquals("Order 10/02/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getOrdersDocuments().get(2).getValue().getDocumentFileName());
    }

    public static void assertJointStatementOfExperts(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("Joint statement of experts expert5 Test1 Test2 12/01/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getJointStatementOfExperts().get(0).getValue().getDocumentFileName());
    }

    public static void assertCostsBudgets(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1CostsBudgets().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant2CostsBudgets().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant1CostsBudgets().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName 12/12/2023",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant2CostsBudgets().get(0).getValue().getDocumentFileName());
    }

    public static void assertExpertEvidences(final BundleCreateRequest bundleCreateRequest) {
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
    }

    public static void assertWitnessStatements(final BundleCreateRequest bundleCreateRequest) {
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
    }

    public static void assertDisclosedDocuments(final BundleCreateRequest bundleCreateRequest) {
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant1DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getClaimant2DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant1DisclosedDocuments().get(0).getValue().getDocumentFileName());
        assertEquals("testFileName",
                     bundleCreateRequest.getCaseDetails().getCaseData().getDefendant2DisclosedDocuments().get(0).getValue().getDocumentFileName());
    }

    public static void assertBundleCreateRequestIsValid(BundleCreateRequest result) {
        assertNotNull(result, "BundleCreateRequest should not be null");
        assertNotNull(result.getCaseDetails(), "CaseDetails should not be null");
        assertNotNull(result.getCaseDetails().getCaseData(), "CaseData should not be null");
    }
}
