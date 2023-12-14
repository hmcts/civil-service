package uk.gov.hmcts.reform.civil.helpers.bundle;

import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
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

    protected static List<Element<UploadEvidenceWitness>> getWitnessDocsByPartyAndDocType(
        BundleRequestMapper.PartyType partyType,
        EvidenceUploadFiles evidenceUploadFiles, CaseData caseData) {
        switch (evidenceUploadFiles) {
            case WITNESS_STATEMENT : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentWitnessStatement() != null
                        ? caseData.getDocumentWitnessStatement() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentWitnessStatementApp2() != null
                        ? caseData.getDocumentWitnessStatementApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentWitnessStatementRes() != null
                        ? caseData.getDocumentWitnessStatementRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentWitnessStatementRes2() != null
                        ? caseData.getDocumentWitnessStatementRes2() : Collections.emptyList();
                };
            }
            case WITNESS_SUMMARY : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentWitnessSummary() != null
                        ? caseData.getDocumentWitnessSummary() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentWitnessSummaryApp2() != null
                        ? caseData.getDocumentWitnessSummaryApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentWitnessSummaryRes() != null
                        ? caseData.getDocumentWitnessSummaryRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentWitnessSummaryRes2() != null
                        ? caseData.getDocumentWitnessSummaryRes2() : Collections.emptyList();
                };
            }
            case NOTICE_OF_INTENTION : {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentHearsayNotice() != null
                        ? caseData.getDocumentHearsayNotice() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentHearsayNoticeApp2() != null
                        ? caseData.getDocumentHearsayNoticeApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentHearsayNoticeRes() != null
                        ? caseData.getDocumentHearsayNoticeRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentHearsayNoticeRes2() != null
                        ? caseData.getDocumentHearsayNoticeRes2() : Collections.emptyList();
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
                        ? caseData.getDocumentExpertReport() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentExpertReportApp2() != null
                        ? caseData.getDocumentExpertReportApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentExpertReportRes() != null
                        ? caseData.getDocumentExpertReportRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentExpertReportRes2() != null
                        ? caseData.getDocumentExpertReportRes2() : Collections.emptyList();
                };
            }
            case QUESTIONS_FOR_EXPERTS: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentQuestions() != null
                        ? caseData.getDocumentQuestions() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentQuestionsApp2() != null
                        ? caseData.getDocumentQuestionsApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentQuestionsRes() != null
                        ? caseData.getDocumentQuestionsRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentQuestionsRes2() != null
                        ? caseData.getDocumentQuestionsRes2() : Collections.emptyList();
                };
            }
            case ANSWERS_FOR_EXPERTS: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentAnswers() != null
                        ? caseData.getDocumentAnswers() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentAnswersApp2() != null
                        ? caseData.getDocumentAnswersApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentAnswersRes() != null
                        ? caseData.getDocumentAnswersRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentAnswersRes2() != null
                        ? caseData.getDocumentAnswersRes2() : Collections.emptyList();
                };
            }
            case JOINT_STATEMENT: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentJointStatement() != null
                        ? caseData.getDocumentJointStatement() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentJointStatementApp2() != null
                        ? caseData.getDocumentJointStatementApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentJointStatementRes() != null
                        ? caseData.getDocumentJointStatementRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentJointStatementRes2() != null
                        ? caseData.getDocumentJointStatementRes2() : Collections.emptyList();
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
                        ? caseData.getDocumentForDisclosure() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentForDisclosureApp2() != null
                        ? caseData.getDocumentForDisclosureApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentForDisclosureRes() != null
                        ? caseData.getDocumentForDisclosureRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentForDisclosureRes2() != null
                        ? caseData.getDocumentForDisclosureRes2() : Collections.emptyList();
                };
            }
            case DISCLOSURE_LIST: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentDisclosureList() != null
                        ? caseData.getDocumentDisclosureList() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentDisclosureListApp2() != null
                        ? caseData.getDocumentDisclosureListApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentDisclosureListRes() != null
                        ? caseData.getDocumentDisclosureListRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentDisclosureListRes2() != null
                        ? caseData.getDocumentDisclosureListRes2() : Collections.emptyList();
                };
            }
            case DOCUMENTS_REFERRED: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentReferredInStatement() != null
                        ? caseData.getDocumentReferredInStatement() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentReferredInStatementApp2() != null
                        ? caseData.getDocumentReferredInStatementApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentReferredInStatementRes() != null
                        ? caseData.getDocumentReferredInStatementRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentReferredInStatementRes2() != null
                        ? caseData.getDocumentReferredInStatementRes2() : Collections.emptyList();
                };
            }
            case DOCUMENTARY: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentEvidenceForTrial() != null
                        ? caseData.getDocumentEvidenceForTrial() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentEvidenceForTrialApp2() != null
                        ? caseData.getDocumentEvidenceForTrialApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentEvidenceForTrialRes() != null
                        ? caseData.getDocumentEvidenceForTrialRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentEvidenceForTrialRes2() != null
                        ? caseData.getDocumentEvidenceForTrialRes2() : Collections.emptyList();
                };
            }
            case CASE_SUMMARY: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentCaseSummary() != null
                        ? caseData.getDocumentCaseSummary() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentCaseSummaryApp2() != null
                        ? caseData.getDocumentCaseSummaryApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentCaseSummaryRes() != null
                        ? caseData.getDocumentCaseSummaryRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentCaseSummaryRes2() != null
                        ? caseData.getDocumentCaseSummaryRes2() : Collections.emptyList();
                };
            }
            case COSTS: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentCosts() != null
                        ? caseData.getDocumentCosts() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentCostsApp2() != null
                        ? caseData.getDocumentCostsApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentCostsRes() != null
                        ? caseData.getDocumentCostsRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentCostsRes2() != null
                        ? caseData.getDocumentCostsRes2() : Collections.emptyList();
                };
            }
            case SKELETON_ARGUMENT: {
                return switch (partyType) {
                    case CLAIMANT1 -> caseData.getDocumentSkeletonArgument() != null
                        ? caseData.getDocumentSkeletonArgument() : Collections.emptyList();
                    case CLAIMANT2 -> caseData.getDocumentSkeletonArgumentApp2() != null
                        ? caseData.getDocumentSkeletonArgumentApp2() : Collections.emptyList();
                    case DEFENDANT1 -> caseData.getDocumentSkeletonArgumentRes() != null
                        ? caseData.getDocumentSkeletonArgumentRes() : Collections.emptyList();
                    case DEFENDANT2 -> caseData.getDocumentSkeletonArgumentRes2() != null
                        ? caseData.getDocumentSkeletonArgumentRes2() : Collections.emptyList();
                };
            }
            default: return null;
        }
    }
}
