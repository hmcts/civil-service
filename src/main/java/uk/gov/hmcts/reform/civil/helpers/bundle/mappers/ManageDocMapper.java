package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import uk.gov.hmcts.reform.civil.enums.DocCategory;
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
                                         DocCategory docCategory) {
        if (docCategory.getValue().equals(md.getValue().getDocumentLink().getCategoryID())) {
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

    default String getDocumentNameBasedOfCategory(DocCategory docCategory) {
        return switch (docCategory) {
            case APP1_DQ -> "Applicant1 directions Questionnaire %s %s";
            case APP1_REPLIES_TO_FURTHER_INFORMATION -> "Applicant1 Replies To FurtherInformation %s %s";
            case APP1_REQUEST_FOR_FURTHER_INFORMATION -> "Applicant Requests For Further Information %s %s";
            case APP1_REQUEST_SCHEDULE_OF_LOSS -> "Applicant1 Schedules Of Loss %s %s";
            case APP1_REPLY -> "Applicant1 reply %s %s";
            case CLAIMANT1_DETAILS_OF_CLAIM -> "Applicant1 details Of Claim %s %s";
            case PARTICULARS_OF_CLAIM -> "Applicant1 particulars Of Claim %s %s";
            case APP2_DQ -> "Applicant2 Directions Questionnaire %s %s";
            case APP2_REPLIES_TO_FURTHER_INFORMATION -> "Applicant2 Replies To Further Information %s %s";
            case APP2_REQUEST_FOR_FURTHER_INFORMATION -> "Applicant2 Requests For Further Information %s %s";
            case APP2_REQUEST_SCHEDULE_OF_LOSS -> "Applicant2 Schedules Of Loss %s %s";
            case APP2_REPLY -> "Applicant2 Reply %s %s";
            case CLAIMANT2_DETAILS_OF_CLAIM -> "Applicant2 Details Of Claim %s %s";
            case APP2_PARTICULARS_OF_CLAIM -> "Applicant2 Particulars Of Claim %s %s";
            case DEF1_DEFENSE_DQ -> "Defendant1 Defense Directions Questionnaire %s %s";
            case DEF2_DEFENSE_DQ -> "Defendant2 Defense Directions Questionnaire %s %s";
            case DEF1_SCHEDULE_OF_LOSS -> "Defendant1 Schedules Of Loss %s %s";
            case DEF2_SCHEDULE_OF_LOSS -> "Defendant2 Schedules Of Loss %s %s";
            default -> throw new IllegalStateException("Unexpected value: " + docCategory);
        };
    }

    default String getDocumentNameBasedOfCategory(DocumentCategory docCategory) {
        return switch (docCategory) {
            case APPLICANT_ONE_WITNESS_STATEMENT -> "Applicant1 witness Statement %s %s";
            case APPLICANT_ONE_WITNESS_OTHER_STATEMENT -> "Applicant1 other Witness Statement %s %s";
            case APPLICANT_ONE_WITNESS_HEARSAY -> "Applicant1 hearsay Notice %s %s";
            case APPLICANT_ONE_WITNESS_SUMMARY -> "Applicant1 witness Summary %s %s";
            case APPLICANT_ONE_WITNESS_REFERRED -> "Applicant1 documents Referred %s %s";
            case APPLICANT_TWO_WITNESS_STATEMENT -> "Applicant2 witness Statement %s %s";
            case APPLICANT_TWO_WITNESS_OTHER_STATEMENT -> "Applicant2 other Witness Statement %s %s";
            case APPLICANT_TWO_WITNESS_HEARSAY -> "Applicant2 hearsay Notice %s %s";
            case APPLICANT_TWO_WITNESS_SUMMARY -> "Applicant2 witness Summary %s %s";
            case APPLICANT_TWO_WITNESS_REFERRED -> "Applicant2 documents Referred %s %s";
            case RESPONDENT_ONE_WITNESS_STATEMENT -> "Respondent1 witness Statement %s %s";
            case RESPONDENT_ONE_WITNESS_OTHER_STATEMENT -> "Respondent1 other Witness Statement %s %s";
            case RESPONDENT_ONE_WITNESS_HEARSAY -> "Respondent1 hearsay Notice %s %s";
            case RESPONDENT_ONE_WITNESS_SUMMARY -> "Respondent1 witness Summary %s %s";
            case RESPONDENT_ONE_WITNESS_REFERRED -> "Respondent1 documents Referred %s %s";
            case RESPONDENT_TWO_WITNESS_STATEMENT -> "Respondent2 witness Statement %s %s";
            case RESPONDENT_TWO_WITNESS_OTHER_STATEMENT -> "Respondent2 other Witness Statement %s %s";
            case RESPONDENT_TWO_WITNESS_HEARSAY -> "Respondent2 hearsay Notice %s %s";
            case RESPONDENT_TWO_WITNESS_SUMMARY -> "Respondent2 witness Summary %s %s";
            case RESPONDENT_TWO_WITNESS_REFERRED -> "Respondent2 documents Referred %s %s";
            case APPLICANT_ONE_EXPERT_REPORT -> "Applicant1 Expert Report %s %s";
            case APPLICANT_ONE_EXPERT_QUESTIONS -> "Applicant1 Questions to Experts %s %s";
            case APPLICANT_ONE_EXPERT_ANSWERS -> "Applicant1 Answers to Questions %s %s";
            case APPLICANT_ONE_EXPERT_JOINT_STATEMENT -> "Applicant1 Joint Statement %s %s";
            case APPLICANT_TWO_EXPERT_REPORT -> "Applicant2 Expert Report %s %s";
            case APPLICANT_TWO_EXPERT_QUESTIONS -> "Applicant2 Questions to Experts %s %s";
            case APPLICANT_TWO_EXPERT_ANSWERS -> "Applicant2 Answers to Questions %s %s";
            case APPLICANT_TWO_EXPERT_JOINT_STATEMENT -> "Applicant2 Joint Statement %s %s";
            case RESPONDENT_ONE_EXPERT_REPORT -> "Respondent Expert Report %s %s";
            case RESPONDENT_ONE_EXPERT_QUESTIONS -> "Respondent Questions to Experts %s %s";
            case RESPONDENT_ONE_EXPERT_ANSWERS -> "Respondent Answers to Questions %s %s";
            case RESPONDENT_ONE_EXPERT_JOINT_STATEMENT -> "Respondent Joint Statement %s %s";
            case RESPONDENT_TWO_EXPERT_REPORT -> "Respondent2 Expert Report %s %s";
            case RESPONDENT_TWO_EXPERT_QUESTIONS -> "Respondent2 Questions to Experts %s %s";
            case RESPONDENT_TWO_EXPERT_ANSWERS -> "Respondent2 Answers to Questions %s %s";
            case RESPONDENT_TWO_EXPERT_JOINT_STATEMENT -> "Respondent2 Joint Statement %s %s";
            case APPLICANT_ONE_DISCLOSURE -> "Applicant1 Disclosure %s %s";
            case APPLICANT_ONE_DISCLOSURE_LIST -> "Applicant1 Disclosure List %s %s";
            case APPLICANT_TWO_DISCLOSURE -> "Applicant2 Disclosure %s %s";
            case APPLICANT_TWO_DISCLOSURE_LIST -> "Applicant2 Disclosure List %s %s";
            case RESPONDENT_ONE_DISCLOSURE -> "Respondent1 Disclosure %s %s";
            case RESPONDENT_ONE_DISCLOSURE_LIST -> "Respondent1 Disclosure List %s %s";
            case RESPONDENT_TWO_DISCLOSURE -> "Respondent2 Disclosure %s %s";
            case RESPONDENT_TWO_DISCLOSURE_LIST -> "Respondent2 Disclosure List %s %s";
            case APPLICANT_ONE_UPLOADED_PRECEDENT_H -> "Applicant1 Uploaded Precedent H %s %s";
            case APPLICANT_ONE_PRECEDENT_AGREED -> "Applicant1 Precedent Agreed %s %s";
            case APPLICANT_ONE_ANY_PRECEDENT_H -> "Applicant1 Any Precedent H %s %s";
            case APPLICANT_TWO_UPLOADED_PRECEDENT_H -> "Applicant2 Uploaded Precedent H %s %s";
            case APPLICANT_TWO_PRECEDENT_AGREED -> "Applicant2 Precedent Agreed %s %s";
            case APPLICANT_TWO_ANY_PRECEDENT_H -> "Applicant2 Any Precedent H %s %s";
            case RESPONDENT_ONE_UPLOADED_PRECEDENT_H -> "Respondent1 Uploaded Precedent H %s %s";
            case RESPONDENT_ONE_PRECEDENT_AGREED -> "Respondent1 Precedent Agreed %s %s";
            case RESPONDENT_ONE_ANY_PRECEDENT_H -> "Respondent1 Any Precedent H %s %s";
            case RESPONDENT_TWO_UPLOADED_PRECEDENT_H -> "Respondent2 Uploaded Precedent H %s %s";
            case RESPONDENT_TWO_PRECEDENT_AGREED -> "Respondent2 Precedent Agreed %s %s";
            case RESPONDENT_TWO_ANY_PRECEDENT_H -> "Respondent2 Any Precedent H %s %s";
            case APPLICANT_ONE_TRIAL_SKELETON -> "Applicant1 Trial Skeleton %s %s";
            case APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE -> "Applicant1 Trial Document Correspondence %s %s";
            case APPLICANT_ONE_TRIAL_DOC_TIME_TABLE -> "Applicant1 Trial Document Timetable %s %s";
            case APPLICANT_TWO_TRIAL_SKELETON -> "Applicant2 Trial Skeleton %s %s";
            case APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE -> "Applicant2 Trial Document Correspondence %s %s";
            case APPLICANT_TWO_TRIAL_DOC_TIME_TABLE -> "Applicant2 Trial Document Timetable %s %s";
            case RESPONDENT_ONE_TRIAL_SKELETON -> "Respondent1 Trial Skeleton %s %s";
            case RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE -> "Respondent1 Trial Document Correspondence %s %s";
            case RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE -> "Respondent1 Trial Document Timetable %s %s";
            case RESPONDENT_TWO_TRIAL_SKELETON -> "Respondent2 Trial Skeleton %s %s";
            case RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE -> "Respondent2 Trial Document Correspondence %s %s";
            case RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE -> "Respondent2 Trial Document Timetable %s %s";
            default -> throw new IllegalStateException("Unexpected value: " + docCategory);
        };
    }
}
