package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

public interface ManageDocMapper {

    default void addDocumentByCategoryId(Element<ManageDocument> md,
                                         List<BundlingRequestDocument> bundlingRequestDocuments,
                                         DocumentCategory docCategory) {
        if (docCategory.getCategoryId().equals(md.getValue().getDocumentLink().getCategoryID())) {
            bundlingRequestDocuments.add(
                buildBundlingRequestDoc(
                    generateDocName(getDocumentNameBasedOfCategory(docCategory), md.getValue().getDocumentName(),
                                    null, LocalDateTime.parse(md.getValue().getDocumentLink().getUploadTimestamp()).toLocalDate()),
                     md.getValue().getDocumentLink(),
                     md.getValue().getDocumentType().name()
                 )
            );
        }
    }

    default String getDocumentNameBasedOfCategory(DocumentCategory docCategory) {
        return switch (docCategory) {
            case APPLICANT_ONE_WITNESS_STATEMENT -> "Applicant1 witness Statement";
            case APPLICANT_ONE_WITNESS_OTHER_STATEMENT -> "Applicant1 other Witness Statement";
            case APPLICANT_ONE_WITNESS_HEARSAY -> "Applicant1 hearsay Notice";
            case APPLICANT_ONE_WITNESS_SUMMARY -> "Applicant1 witness Summary";
            case APPLICANT_ONE_WITNESS_REFERRED -> "Applicant1 documents Referred";
            case APPLICANT_TWO_WITNESS_STATEMENT -> "Applicant2 witness Statement";
            case APPLICANT_TWO_WITNESS_OTHER_STATEMENT -> "Applicant2 other Witness Statement";
            case APPLICANT_TWO_WITNESS_HEARSAY -> "Applicant2 hearsay Notice";
            case APPLICANT_TWO_WITNESS_SUMMARY -> "Applicant2 witness Summary";
            case APPLICANT_TWO_WITNESS_REFERRED -> "Applicant2 documents Referred";
            case RESPONDENT_ONE_WITNESS_STATEMENT -> "Respondent1 witness Statement";
            case RESPONDENT_ONE_WITNESS_OTHER_STATEMENT -> "Respondent1 other Witness Statement";
            case RESPONDENT_ONE_WITNESS_HEARSAY -> "Respondent1 hearsay Notice";
            case RESPONDENT_ONE_WITNESS_SUMMARY -> "Respondent1 witness Summary";
            case RESPONDENT_ONE_WITNESS_REFERRED -> "Respondent1 documents Referred";
            case RESPONDENT_TWO_WITNESS_STATEMENT -> "Respondent2 witness Statement";
            case RESPONDENT_TWO_WITNESS_OTHER_STATEMENT -> "Respondent2 other Witness Statement";
            case RESPONDENT_TWO_WITNESS_HEARSAY -> "Respondent2 hearsay Notice";
            case RESPONDENT_TWO_WITNESS_SUMMARY -> "Respondent2 witness Summary";
            case RESPONDENT_TWO_WITNESS_REFERRED -> "Respondent2 documents Referred";
            case APPLICANT_ONE_EXPERT_REPORT -> "Applicant1 Expert Report";
            case APPLICANT_ONE_EXPERT_QUESTIONS -> "Applicant1 Questions to Experts";
            case APPLICANT_ONE_EXPERT_ANSWERS -> "Applicant1 Answers to Questions";
            case APPLICANT_ONE_EXPERT_JOINT_STATEMENT -> "Applicant1 Joint Statement";
            case APPLICANT_TWO_EXPERT_REPORT -> "Applicant2 Expert Report";
            case APPLICANT_TWO_EXPERT_QUESTIONS -> "Applicant2 Questions to Experts";
            case APPLICANT_TWO_EXPERT_ANSWERS -> "Applicant2 Answers to Questions";
            case APPLICANT_TWO_EXPERT_JOINT_STATEMENT -> "Applicant2 Joint Statement";
            case RESPONDENT_ONE_EXPERT_REPORT -> "Respondent Expert Report";
            case RESPONDENT_ONE_EXPERT_QUESTIONS -> "Respondent Questions to Experts";
            case RESPONDENT_ONE_EXPERT_ANSWERS -> "Respondent Answers to Questions";
            case RESPONDENT_ONE_EXPERT_JOINT_STATEMENT -> "Respondent Joint Statement";
            case RESPONDENT_TWO_EXPERT_REPORT -> "Respondent2 Expert Report";
            case RESPONDENT_TWO_EXPERT_QUESTIONS -> "Respondent2 Questions to Experts";
            case RESPONDENT_TWO_EXPERT_ANSWERS -> "Respondent2 Answers to Questions";
            case RESPONDENT_TWO_EXPERT_JOINT_STATEMENT -> "Respondent2 Joint Statement";
            case APPLICANT_ONE_DISCLOSURE -> "Applicant1 Disclosure";
            case APPLICANT_ONE_DISCLOSURE_LIST -> "Applicant1 Disclosure List";
            case APPLICANT_TWO_DISCLOSURE -> "Applicant2 Disclosure";
            case APPLICANT_TWO_DISCLOSURE_LIST -> "Applicant2 Disclosure List";
            case RESPONDENT_ONE_DISCLOSURE -> "Respondent1 Disclosure";
            case RESPONDENT_ONE_DISCLOSURE_LIST -> "Respondent1 Disclosure List";
            case RESPONDENT_TWO_DISCLOSURE -> "Respondent2 Disclosure";
            case RESPONDENT_TWO_DISCLOSURE_LIST -> "Respondent2 Disclosure List";
            case APPLICANT_ONE_UPLOADED_PRECEDENT_H -> "Applicant1 Uploaded Precedent H";
            case APPLICANT_ONE_PRECEDENT_AGREED -> "Applicant1 Precedent Agreed";
            case APPLICANT_ONE_ANY_PRECEDENT_H -> "Applicant1 Any Precedent H";
            case APPLICANT_TWO_UPLOADED_PRECEDENT_H -> "Applicant2 Uploaded Precedent H";
            case APPLICANT_TWO_PRECEDENT_AGREED -> "Applicant2 Precedent Agreed";
            case APPLICANT_TWO_ANY_PRECEDENT_H -> "Applicant2 Any Precedent H";
            case RESPONDENT_ONE_UPLOADED_PRECEDENT_H -> "Respondent1 Uploaded Precedent H";
            case RESPONDENT_ONE_PRECEDENT_AGREED -> "Respondent1 Precedent Agreed";
            case RESPONDENT_ONE_ANY_PRECEDENT_H -> "Respondent1 Any Precedent H";
            case RESPONDENT_TWO_UPLOADED_PRECEDENT_H -> "Respondent2 Uploaded Precedent H";
            case RESPONDENT_TWO_PRECEDENT_AGREED -> "Respondent2 Precedent Agreed";
            case RESPONDENT_TWO_ANY_PRECEDENT_H -> "Respondent2 Any Precedent H";
            case APPLICANT_ONE_TRIAL_SKELETON -> "Applicant1 Trial Skeleton";
            case APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE -> "Applicant1 Trial Document Correspondence";
            case APPLICANT_ONE_TRIAL_DOC_TIME_TABLE -> "Applicant1 Trial Document Timetable";
            case APPLICANT_TWO_TRIAL_SKELETON -> "Applicant2 Trial Skeleton";
            case APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE -> "Applicant2 Trial Document Correspondence";
            case APPLICANT_TWO_TRIAL_DOC_TIME_TABLE -> "Applicant2 Trial Document Timetable";
            case RESPONDENT_ONE_TRIAL_SKELETON -> "Respondent1 Trial Skeleton";
            case RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE -> "Respondent1 Trial Document Correspondence";
            case RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE -> "Respondent1 Trial Document Timetable";
            case RESPONDENT_TWO_TRIAL_SKELETON -> "Respondent2 Trial Skeleton";
            case RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE -> "Respondent2 Trial Document Correspondence";
            case RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE -> "Respondent2 Trial Document Timetable";
            default -> throw new IllegalStateException("Unexpected value: " + docCategory);
        };
    }
}
