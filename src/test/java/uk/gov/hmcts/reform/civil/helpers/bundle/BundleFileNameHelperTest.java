package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BundleFileNameHelperTest {

    private CaseData caseData = CaseData.builder().ccdCaseReference(1L)
            .documentWitnessStatement(getWitnessDocs())
            .documentWitnessSummary(getWitnessDocs())
            .documentHearsayNotice(getWitnessDocs())
            .documentReferredInStatement(getOtherDocs())
            .documentWitnessStatementApp2(getWitnessDocs())
            .documentWitnessSummaryApp2(getWitnessDocs())
            .documentHearsayNoticeApp2(getWitnessDocs())
            .documentReferredInStatementApp2(getOtherDocs())
            .documentWitnessStatementRes(getWitnessDocs())
            .documentWitnessSummaryRes(getWitnessDocs())
            .documentHearsayNoticeRes(getWitnessDocs())
            .documentReferredInStatementRes(getOtherDocs())
            .documentWitnessStatementRes2(getWitnessDocs())
            .documentWitnessSummaryRes2(getWitnessDocs())
            .documentHearsayNoticeRes2(getWitnessDocs())
            .documentReferredInStatementRes2(getOtherDocs())
            .documentExpertReport(getExpertDocs())
            .documentJointStatement(getExpertDocs())
            .documentAnswers(getExpertDocs())
            .documentQuestions(getExpertDocs())
            .documentExpertReportApp2(getExpertDocs())
            .documentJointStatementApp2(getExpertDocs())
            .documentAnswersApp2(getExpertDocs())
            .documentQuestionsApp2(getExpertDocs())
            .documentExpertReportRes(getExpertDocs())
            .documentJointStatementRes(getExpertDocs())
            .documentAnswersRes(getExpertDocs())
            .documentQuestionsRes(getExpertDocs())
            .documentExpertReportRes2(getExpertDocs())
            .documentJointStatementRes2(getExpertDocs())
            .documentAnswersRes2(getExpertDocs())
            .documentQuestionsRes2(getExpertDocs())
            .documentEvidenceForTrial(getOtherDocs())
            .documentEvidenceForTrialApp2(getOtherDocs())
            .documentEvidenceForTrialRes(getOtherDocs())
            .documentEvidenceForTrialRes2(getOtherDocs())
            .documentForDisclosure(getOtherDocs())
            .documentForDisclosureApp2(getOtherDocs())
            .documentForDisclosureRes(getOtherDocs())
            .documentForDisclosureRes2(getOtherDocs())
            .documentDisclosureList(getOtherDocs())
            .documentDisclosureListApp2(getOtherDocs())
            .documentDisclosureListRes(getOtherDocs())
            .documentDisclosureListRes2(getOtherDocs())
            .documentCaseSummary(getOtherDocs())
            .documentCaseSummaryApp2(getOtherDocs())
            .documentCaseSummaryRes(getOtherDocs())
            .documentCaseSummaryRes2(getOtherDocs())
            .documentReferredInStatement(getOtherDocs())
            .documentReferredInStatementApp2(getOtherDocs())
            .documentReferredInStatementRes(getOtherDocs())
            .documentReferredInStatementRes2(getOtherDocs())
        .build();

    @ParameterizedTest
    @EnumSource(value = PartyType.class, names = {"CLAIMANT1", "CLAIMANT2", "DEFENDANT1",
        "DEFENDANT2"}, mode =
        EnumSource.Mode.INCLUDE)
    public void validateAllWitnessDocs(PartyType partyType) {

        assertNotNull(BundleFileNameHelper.getWitnessDocsByPartyAndDocType(partyType,
                                                             EvidenceUploadType.WITNESS_STATEMENT, caseData));

        assertNotNull(BundleFileNameHelper.getWitnessDocsByPartyAndDocType(partyType,
                                                             EvidenceUploadType.WITNESS_SUMMARY, caseData));

        assertNotNull(BundleFileNameHelper.getWitnessDocsByPartyAndDocType(partyType,
                                                             EvidenceUploadType.NOTICE_OF_INTENTION, caseData));
    }

    @ParameterizedTest
    @EnumSource(value = PartyType.class, names = {"CLAIMANT1", "CLAIMANT2", "DEFENDANT1",
        "DEFENDANT2"}, mode =
        EnumSource.Mode.INCLUDE)
    public void validateAllExpertsDocs(PartyType partyType) {

        assertNotNull(BundleFileNameHelper.getExpertDocsByPartyAndDocType(partyType,
                                                                           EvidenceUploadType.EXPERT_REPORT, caseData));

        assertNotNull(BundleFileNameHelper.getExpertDocsByPartyAndDocType(partyType,
                                                                           EvidenceUploadType.QUESTIONS_FOR_EXPERTS, caseData));

        assertNotNull(BundleFileNameHelper.getExpertDocsByPartyAndDocType(partyType,
                                                                           EvidenceUploadType.ANSWERS_FOR_EXPERTS, caseData));
    }

    @ParameterizedTest
    @EnumSource(value = PartyType.class, names = {"CLAIMANT1", "CLAIMANT2", "DEFENDANT1",
        "DEFENDANT2"}, mode =
        EnumSource.Mode.INCLUDE)
    public void validateAllOtherDocs(PartyType partyType) {

        assertNotNull(BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                          EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE, caseData));

        assertNotNull(BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                          EvidenceUploadType.DISCLOSURE_LIST, caseData));

        assertNotNull(BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                          EvidenceUploadType.DOCUMENTS_REFERRED, caseData));
        assertNotNull(BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                  EvidenceUploadType.DOCUMENTARY, caseData));
        assertNotNull(BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                  EvidenceUploadType.CASE_SUMMARY, caseData));
    }

    private List<Element<UploadEvidenceDocumentType>> getOtherDocs() {
        List<Element<UploadEvidenceDocumentType>> otherEvidenceDocs = new ArrayList<>();
        otherEvidenceDocs.add(ElementUtils.element(UploadEvidenceDocumentType
                                                       .builder()
                                                       .documentUpload(Document.builder().documentBinaryUrl("Test")
                                                                           .documentFileName("Test").build())
                                                       .typeOfDocument(TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames().get(0))
                                                       .documentIssuedDate(LocalDate.of(2023, 1, 12))
                                                       .build()));
        return otherEvidenceDocs;
    }

    private List<Element<UploadEvidenceWitness>> getWitnessDocs() {
        List<Element<UploadEvidenceWitness>> witnessEvidenceDocs = new ArrayList<>();
        witnessEvidenceDocs.add(ElementUtils.element(UploadEvidenceWitness
                                                         .builder()
                                                         .witnessOptionDocument(Document.builder().documentBinaryUrl(
                                                             "Test")
                                                                                    .documentFileName("Test").build())
                                                         .witnessOptionName("FirstName LastName")
                                                         .witnessOptionUploadDate(LocalDate.of(2023, 2, 10)).build()));
        return witnessEvidenceDocs;
    }

    private List<Element<UploadEvidenceExpert>> getExpertDocs() {
        List<Element<UploadEvidenceExpert>> expertEvidenceDocs = new ArrayList<>();
        expertEvidenceDocs.add(ElementUtils.element(UploadEvidenceExpert
                                                        .builder()
                                                        .expertDocument(Document.builder().documentBinaryUrl("Test")
                                                                            .documentFileName("Test").build())
                                                        .expertOptionUploadDate(LocalDate.of(2023, 1, 12))
                                                        .expertOptionName("FirstName LastName").build()));

        return  expertEvidenceDocs;
    }
}
