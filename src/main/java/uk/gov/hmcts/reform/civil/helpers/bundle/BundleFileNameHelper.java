package uk.gov.hmcts.reform.civil.helpers.bundle;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

public class BundleFileNameHelper {

    private BundleFileNameHelper() {
    }

    protected static List<Element<UploadEvidenceWitness>> getWitnessDocsByPartyAndDocType(
        BundleRequestMapper.PartyType partyType,
        EvidenceUploadFiles evidenceUploadFiles, CaseData caseData) {
        switch (evidenceUploadFiles) {
            case WITNESS_STATEMENT : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentWitnessStatement() != null
                        ? caseData.getDocumentWitnessStatement() : null;
                    case CLAIMANT2 -> caseData.getDocumentWitnessStatementApp2() != null
                        ? caseData.getDocumentWitnessStatementApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentWitnessStatementRes() != null
                        ? caseData.getDocumentWitnessStatementRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentWitnessStatementRes2() != null
                        ? caseData.getDocumentWitnessStatementRes2() : null;
                };
            }
            case WITNESS_SUMMARY : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentWitnessSummary() != null
                        ? caseData.getDocumentWitnessSummary() : null;
                    case CLAIMANT2 -> caseData.getDocumentWitnessSummaryApp2() != null
                        ? caseData.getDocumentWitnessSummaryApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentWitnessSummaryRes() != null
                        ? caseData.getDocumentWitnessSummaryRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentWitnessSummaryRes2() != null
                        ? caseData.getDocumentWitnessSummaryRes2() : null;
                };
            }
            case NOTICE_OF_INTENTION : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentHearsayNotice() != null
                        ? caseData.getDocumentHearsayNotice() : null;
                    case CLAIMANT2 -> caseData.getDocumentHearsayNoticeApp2() != null
                        ? caseData.getDocumentHearsayNoticeApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentHearsayNoticeRes() != null
                        ? caseData.getDocumentHearsayNoticeRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentHearsayNoticeRes2() != null
                        ? caseData.getDocumentHearsayNoticeRes2() : null;
                };
            }
            default: return null;
        }
    }

    protected static List<Element<UploadEvidenceExpert>> getExpertDocsByPartyAndDocType(
        BundleRequestMapper.PartyType partyType,
        EvidenceUploadFiles evidenceUploadFiles, CaseData caseData) {
        switch (evidenceUploadFiles) {
            case EXPERT_REPORT : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentExpertReport() != null
                        ? caseData.getDocumentExpertReport() : null;
                    case CLAIMANT2 -> caseData.getDocumentExpertReportApp2() != null
                        ? caseData.getDocumentExpertReportApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentExpertReportRes() != null
                        ? caseData.getDocumentExpertReportRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentExpertReportRes2() != null
                        ? caseData.getDocumentExpertReportRes2() : null;
                };
            }
            case QUESTIONS_FOR_EXPERTS: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentQuestions() != null
                        ? caseData.getDocumentQuestions() : null;
                    case CLAIMANT2 -> caseData.getDocumentQuestionsApp2() != null
                        ? caseData.getDocumentQuestionsApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentQuestionsRes() != null
                        ? caseData.getDocumentQuestionsRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentQuestionsRes2() != null
                        ? caseData.getDocumentQuestionsRes2() : null;
                };
            }
            case ANSWERS_FOR_EXPERTS: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentAnswers() != null
                        ? caseData.getDocumentAnswers() : null;
                    case CLAIMANT2 -> caseData.getDocumentAnswersApp2() != null
                        ? caseData.getDocumentAnswersApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentAnswersRes() != null
                        ? caseData.getDocumentAnswersRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentAnswersRes2() != null
                        ? caseData.getDocumentAnswersRes2() : null;
                };
            }
            case JOINT_STATEMENT: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentJointStatement() != null
                        ? caseData.getDocumentJointStatement() : null;
                    case CLAIMANT2 -> caseData.getDocumentJointStatementApp2() != null
                        ? caseData.getDocumentJointStatementApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentJointStatementRes() != null
                        ? caseData.getDocumentJointStatementRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentJointStatementRes2() != null
                        ? caseData.getDocumentJointStatementRes2() : null;
                };
            }
            default: return null;
        }
    }

    protected static List<Element<UploadEvidenceDocumentType>> getEvidenceUploadDocsByPartyAndDocType(
        BundleRequestMapper.PartyType partyType,
        EvidenceUploadFiles evidenceUploadFiles, CaseData caseData) {
        switch (evidenceUploadFiles) {
            case DOCUMENTS_FOR_DISCLOSURE: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentForDisclosure() != null
                        ? caseData.getDocumentForDisclosure() : null;
                    case CLAIMANT2 -> caseData.getDocumentForDisclosureApp2() != null
                        ? caseData.getDocumentForDisclosureApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentForDisclosureRes() != null
                        ? caseData.getDocumentForDisclosureRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentForDisclosureRes2() != null
                        ? caseData.getDocumentForDisclosureRes2() : null;
                };
            }
            case DISCLOSURE_LIST: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentDisclosureList() != null
                        ? caseData.getDocumentDisclosureList() : null;
                    case CLAIMANT2 -> caseData.getDocumentDisclosureListApp2() != null
                        ? caseData.getDocumentDisclosureListApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentDisclosureListRes() != null
                        ? caseData.getDocumentDisclosureListRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentDisclosureListRes2() != null
                        ? caseData.getDocumentDisclosureListRes2() : null;
                };
            }
            case DOCUMENTS_REFERRED: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentReferredInStatement() != null
                        ? caseData.getDocumentReferredInStatement() : null;
                    case CLAIMANT2 -> caseData.getDocumentReferredInStatementApp2() != null
                        ? caseData.getDocumentReferredInStatementApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentReferredInStatementRes() != null
                        ? caseData.getDocumentReferredInStatementRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentReferredInStatementRes2() != null
                        ? caseData.getDocumentReferredInStatementRes2() : null;
                };
            }
            case DOCUMENTARY: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentEvidenceForTrial() != null
                        ? caseData.getDocumentEvidenceForTrial() : null;
                    case CLAIMANT2 -> caseData.getDocumentEvidenceForTrialApp2() != null
                        ? caseData.getDocumentEvidenceForTrialApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentEvidenceForTrialRes() != null
                        ? caseData.getDocumentEvidenceForTrialRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentEvidenceForTrialRes2() != null
                        ? caseData.getDocumentEvidenceForTrialRes2() : null;
                };
            }
            case CASE_SUMMARY: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentCaseSummary() != null
                        ? caseData.getDocumentCaseSummary() : null;
                    case CLAIMANT2 -> caseData.getDocumentCaseSummaryApp2() != null
                        ? caseData.getDocumentCaseSummaryApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentCaseSummaryRes() != null
                        ? caseData.getDocumentCaseSummaryRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentCaseSummaryRes2() != null
                        ? caseData.getDocumentCaseSummaryRes2() : null;
                };
            }
            case COSTS: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentCosts() != null
                        ? caseData.getDocumentCosts() : null;
                    case CLAIMANT2 -> caseData.getDocumentCostsApp2() != null
                        ? caseData.getDocumentCostsApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentCostsRes() != null
                        ? caseData.getDocumentCostsRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentCostsRes2() != null
                        ? caseData.getDocumentCostsRes2() : null;
                };
            }
            case SKELETON_ARGUMENT: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentSkeletonArgument() != null
                        ? caseData.getDocumentSkeletonArgument() : null;
                    case CLAIMANT2 -> caseData.getDocumentSkeletonArgumentApp2() != null
                        ? caseData.getDocumentSkeletonArgumentApp2() : null;
                    case DEFENDANT1 -> caseData.getDocumentSkeletonArgumentRes() != null
                        ? caseData.getDocumentSkeletonArgumentRes() : null;
                    case DEFENDANT2 -> caseData.getDocumentSkeletonArgumentRes2() != null
                        ? caseData.getDocumentSkeletonArgumentRes2() : null;
                };
            }
            default: return null;
        }
    }
}
