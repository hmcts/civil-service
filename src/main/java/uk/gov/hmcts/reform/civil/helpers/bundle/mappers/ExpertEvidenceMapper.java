package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class ExpertEvidenceMapper implements ManageDocMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Set<String> allExpertsNames = bundleDocumentsRetrieval.getAllExpertsNames(
            partyType,
            EvidenceUploadType.EXPERT_REPORT,
            caseData
        );
        Set<String> allJointExpertsNames = bundleDocumentsRetrieval.getAllExpertsNames(
            partyType,
            EvidenceUploadType.JOINT_STATEMENT,
            caseData
        );
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
            partyType,
            EvidenceUploadType.EXPERT_REPORT,
            caseData,
            BundleFileNameList.EXPERT_EVIDENCE,
            allExpertsNames
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllOtherPartyQuestions(partyType,
            caseData, allExpertsNames
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
            partyType,
            EvidenceUploadType.ANSWERS_FOR_EXPERTS,
            caseData,
            BundleFileNameList.REPLIES_FROM,
            allExpertsNames
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllRemainingExpertQuestions(
            partyType,
            EvidenceUploadType.QUESTIONS_FOR_EXPERTS,
            caseData
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllRemainingExpertReports(
            partyType,
            EvidenceUploadType.ANSWERS_FOR_EXPERTS,
            caseData,
            BundleFileNameList.REPLIES_FROM,
            allExpertsNames,
            allJointExpertsNames
        ));

        //ManageDocuments
        addManageDocuments(caseData, partyType, bundlingRequestDocuments);
        return wrapElements(bundlingRequestDocuments);
    }

    private void addManageDocuments(CaseData caseData,
                                    PartyType partyType,
                                    List<BundlingRequestDocument> bundlingRequestDocuments) {
        List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
        if (manageDocuments == null || manageDocuments.isEmpty()) {
            return;
        }

        List<DocumentCategory> documentCategories = switch (partyType) {
            case CLAIMANT1 -> List.of(
                APPLICANT_ONE_EXPERT_REPORT,
                APPLICANT_ONE_EXPERT_QUESTIONS,
                APPLICANT_ONE_EXPERT_ANSWERS,
                APPLICANT_ONE_EXPERT_JOINT_STATEMENT
            );
            case CLAIMANT2 -> List.of(
                APPLICANT_TWO_EXPERT_REPORT,
                APPLICANT_TWO_EXPERT_QUESTIONS,
                APPLICANT_TWO_EXPERT_ANSWERS,
                APPLICANT_TWO_EXPERT_JOINT_STATEMENT
            );
            case DEFENDANT1 -> List.of(
                RESPONDENT_ONE_EXPERT_REPORT,
                RESPONDENT_ONE_EXPERT_QUESTIONS,
                RESPONDENT_ONE_EXPERT_ANSWERS,
                RESPONDENT_ONE_EXPERT_JOINT_STATEMENT
            );
            case DEFENDANT2 -> List.of(
                RESPONDENT_TWO_EXPERT_REPORT,
                RESPONDENT_TWO_EXPERT_QUESTIONS,
                RESPONDENT_TWO_EXPERT_ANSWERS,
                RESPONDENT_TWO_EXPERT_JOINT_STATEMENT
            );
        };

        documentCategories.forEach(category ->
                                       manageDocuments.forEach(md -> addDocumentByCategoryId(
                                           md,
                                           bundlingRequestDocuments,
                                           category
                                       )));
    }
}
