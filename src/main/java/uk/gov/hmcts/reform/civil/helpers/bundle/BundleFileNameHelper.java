package uk.gov.hmcts.reform.civil.helpers.bundle;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

public class BundleFileNameHelper {

    private BundleFileNameHelper() {
    }

    public static List<Element<UploadEvidenceWitness>> getWitnessDocsByPartyAndDocType(PartyType partyType,
        EvidenceUploadType evidenceUploadFiles, CaseData caseData) {
        return switch (evidenceUploadFiles) {
            case WITNESS_STATEMENT -> getDocsByParty(
                partyType,
                caseData.getDocumentWitnessStatement(),
                caseData.getDocumentWitnessStatementApp2(),
                caseData.getDocumentWitnessStatementRes(),
                caseData.getDocumentWitnessStatementRes2()
            );
            case WITNESS_SUMMARY -> getDocsByParty(
                partyType,
                caseData.getDocumentWitnessSummary(),
                caseData.getDocumentWitnessSummaryApp2(),
                caseData.getDocumentWitnessSummaryRes(),
                caseData.getDocumentWitnessSummaryRes2()
            );
            case NOTICE_OF_INTENTION -> getDocsByParty(
                partyType,
                caseData.getDocumentHearsayNotice(),
                caseData.getDocumentHearsayNoticeApp2(),
                caseData.getDocumentHearsayNoticeRes(),
                caseData.getDocumentHearsayNoticeRes2()
            );
            default -> null;
        };
    }

    public static List<Element<UploadEvidenceExpert>> getExpertDocsByPartyAndDocType(
        PartyType partyType,
        EvidenceUploadType evidenceUploadFiles, CaseData caseData) {
        return switch (evidenceUploadFiles) {
            case EXPERT_REPORT -> getDocsByParty(
                partyType,
                caseData.getDocumentExpertReport(),
                caseData.getDocumentExpertReportApp2(),
                caseData.getDocumentExpertReportRes(),
                caseData.getDocumentExpertReportRes2()
            );
            case QUESTIONS_FOR_EXPERTS -> getDocsByParty(
                partyType,
                caseData.getDocumentQuestions(),
                caseData.getDocumentQuestionsApp2(),
                caseData.getDocumentQuestionsRes(),
                caseData.getDocumentQuestionsRes2()
            );
            case ANSWERS_FOR_EXPERTS -> getDocsByParty(
                partyType,
                caseData.getDocumentAnswers(),
                caseData.getDocumentAnswersApp2(),
                caseData.getDocumentAnswersRes(),
                caseData.getDocumentAnswersRes2()
            );
            case JOINT_STATEMENT -> getDocsByParty(
                partyType,
                caseData.getDocumentJointStatement(),
                caseData.getDocumentJointStatementApp2(),
                caseData.getDocumentJointStatementRes(),
                caseData.getDocumentJointStatementRes2()
            );
            default -> null;
        };
    }

    public static List<Element<UploadEvidenceDocumentType>> getEvidenceUploadDocsByPartyAndDocType(
        PartyType partyType,
        EvidenceUploadType evidenceUploadFiles, CaseData caseData) {
        return switch (evidenceUploadFiles) {
            case DOCUMENTS_FOR_DISCLOSURE -> getDocsByParty(
                partyType,
                caseData.getDocumentForDisclosure(),
                caseData.getDocumentForDisclosureApp2(),
                caseData.getDocumentForDisclosureRes(),
                caseData.getDocumentForDisclosureRes2()
            );
            case DISCLOSURE_LIST -> getDocsByParty(
                partyType,
                caseData.getDocumentDisclosureList(),
                caseData.getDocumentDisclosureListApp2(),
                caseData.getDocumentDisclosureListRes(),
                caseData.getDocumentDisclosureListRes2()
            );
            case DOCUMENTS_REFERRED -> getDocsByParty(
                partyType,
                caseData.getDocumentReferredInStatement(),
                caseData.getDocumentReferredInStatementApp2(),
                caseData.getDocumentReferredInStatementRes(),
                caseData.getDocumentReferredInStatementRes2()
            );
            case DOCUMENTARY -> getDocsByParty(
                partyType,
                caseData.getDocumentEvidenceForTrial(),
                caseData.getDocumentEvidenceForTrialApp2(),
                caseData.getDocumentEvidenceForTrialRes(),
                caseData.getDocumentEvidenceForTrialRes2()
            );
            case CASE_SUMMARY -> getDocsByParty(
                partyType,
                caseData.getDocumentCaseSummary(),
                caseData.getDocumentCaseSummaryApp2(),
                caseData.getDocumentCaseSummaryRes(),
                caseData.getDocumentCaseSummaryRes2()
            );
            case COSTS -> getDocsByParty(
                partyType,
                caseData.getDocumentCosts(),
                caseData.getDocumentCostsApp2(),
                caseData.getDocumentCostsRes(),
                caseData.getDocumentCostsRes2()
            );
            case SKELETON_ARGUMENT -> getDocsByParty(
                partyType,
                caseData.getDocumentSkeletonArgument(),
                caseData.getDocumentSkeletonArgumentApp2(),
                caseData.getDocumentSkeletonArgumentRes(),
                caseData.getDocumentSkeletonArgumentRes2()
            );
            default -> null;
        };
    }

    private static <T> List<Element<T>> getDocsByParty(PartyType partyType,
                                                       List<Element<T>> claimant1Docs,
                                                       List<Element<T>> claimant2Docs,
                                                       List<Element<T>> defendant1Docs,
                                                       List<Element<T>> defendant2Docs) {
        return emptyIfNull(switch (partyType) {
            case CLAIMANT1 -> claimant1Docs;
            case CLAIMANT2 -> claimant2Docs;
            case DEFENDANT1 -> defendant1Docs;
            case DEFENDANT2 -> defendant2Docs;
        });
    }

    private static <T> List<Element<T>> emptyIfNull(List<Element<T>> documents) {
        return documents != null ? documents : Collections.emptyList();
    }
}
