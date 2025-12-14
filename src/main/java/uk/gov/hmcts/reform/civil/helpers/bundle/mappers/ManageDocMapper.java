package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REPLIES_TO_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REPLY;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REQUEST_FOR_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REQUEST_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REPLIES_TO_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REPLY;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REQUEST_FOR_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REQUEST_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT1_DETAILS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT2_DETAILS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF1_DEFENSE_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF1_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF2_DEFENSE_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF2_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_APP1;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_APP2;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF1;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF2;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_ANSWERS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_JOINT_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_QUESTIONS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

public interface ManageDocMapper {

    default void addManageDocumentsByDocCategory(List<Element<ManageDocument>> manageDocuments,
                                                 PartyType partyType,
                                                 List<BundlingRequestDocument> bundlingRequestDocuments) {
        List<DocCategory> documentCategories = switch (partyType) {
            case CLAIMANT1 -> List.of(
                APP1_DQ,
                APP1_REPLIES_TO_FURTHER_INFORMATION,
                APP1_REQUEST_FOR_FURTHER_INFORMATION,
                APP1_REQUEST_SCHEDULE_OF_LOSS,
                APP1_REPLY,
                CLAIMANT1_DETAILS_OF_CLAIM,
                PARTICULARS_OF_CLAIM,
                DQ_APP1
            );
            case CLAIMANT2 -> List.of(
                APP2_DQ,
                APP2_REPLIES_TO_FURTHER_INFORMATION,
                APP2_REQUEST_FOR_FURTHER_INFORMATION,
                APP2_REQUEST_SCHEDULE_OF_LOSS,
                APP2_REPLY,
                CLAIMANT2_DETAILS_OF_CLAIM,
                APP2_PARTICULARS_OF_CLAIM,
                DQ_APP2
            );
            case DEFENDANT1 -> List.of(
                DEF1_DEFENSE_DQ,
                DEF1_SCHEDULE_OF_LOSS,
                DQ_DEF1
            );
            case DEFENDANT2 -> List.of(
                DEF2_DEFENSE_DQ,
                DEF2_SCHEDULE_OF_LOSS,
                DQ_DEF2
            );
        };

        documentCategories.forEach(category ->
                                       manageDocuments.forEach(md -> addDocumentByCategoryId(
                                           md,
                                           bundlingRequestDocuments,
                                           category
                                       )));

    }

    default void addManageDocuments(List<Element<ManageDocument>> manageDocuments,
                                    PartyType partyType,
                                    List<BundlingRequestDocument> bundlingRequestDocuments) {
        List<DocumentCategory> documentCategories = switch (partyType) {
            case CLAIMANT1 -> List.of(
                APPLICANT_ONE_TRIAL_SKELETON,
                APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE,
                APPLICANT_ONE_TRIAL_DOC_TIME_TABLE,
                APPLICANT_ONE_WITNESS_STATEMENT,
                APPLICANT_ONE_WITNESS_OTHER_STATEMENT,
                APPLICANT_ONE_WITNESS_HEARSAY,
                APPLICANT_ONE_WITNESS_SUMMARY,
                APPLICANT_ONE_WITNESS_REFERRED,
                APPLICANT_ONE_EXPERT_REPORT,
                APPLICANT_ONE_EXPERT_QUESTIONS,
                APPLICANT_ONE_EXPERT_ANSWERS,
                APPLICANT_ONE_EXPERT_JOINT_STATEMENT,
                APPLICANT_ONE_DISCLOSURE,
                APPLICANT_ONE_DISCLOSURE_LIST,
                APPLICANT_ONE_UPLOADED_PRECEDENT_H,
                APPLICANT_ONE_PRECEDENT_AGREED,
                APPLICANT_ONE_ANY_PRECEDENT_H
            );
            case CLAIMANT2 -> List.of(
                APPLICANT_TWO_TRIAL_SKELETON,
                APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE,
                APPLICANT_TWO_TRIAL_DOC_TIME_TABLE,
                APPLICANT_TWO_WITNESS_STATEMENT,
                APPLICANT_TWO_WITNESS_OTHER_STATEMENT,
                APPLICANT_TWO_WITNESS_HEARSAY,
                APPLICANT_TWO_WITNESS_SUMMARY,
                APPLICANT_TWO_WITNESS_REFERRED,
                APPLICANT_TWO_EXPERT_REPORT,
                APPLICANT_TWO_EXPERT_QUESTIONS,
                APPLICANT_TWO_EXPERT_ANSWERS,
                APPLICANT_TWO_EXPERT_JOINT_STATEMENT,
                APPLICANT_TWO_DISCLOSURE,
                APPLICANT_TWO_DISCLOSURE_LIST,
                APPLICANT_TWO_UPLOADED_PRECEDENT_H,
                APPLICANT_TWO_PRECEDENT_AGREED,
                APPLICANT_TWO_ANY_PRECEDENT_H
            );
            case DEFENDANT1 -> List.of(
                RESPONDENT_ONE_TRIAL_SKELETON,
                RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE,
                RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE,
                RESPONDENT_ONE_WITNESS_STATEMENT,
                RESPONDENT_ONE_WITNESS_OTHER_STATEMENT,
                RESPONDENT_ONE_WITNESS_HEARSAY,
                RESPONDENT_ONE_WITNESS_SUMMARY,
                RESPONDENT_ONE_WITNESS_REFERRED,
                RESPONDENT_ONE_EXPERT_REPORT,
                RESPONDENT_ONE_EXPERT_QUESTIONS,
                RESPONDENT_ONE_EXPERT_ANSWERS,
                RESPONDENT_ONE_EXPERT_JOINT_STATEMENT,
                RESPONDENT_ONE_DISCLOSURE,
                RESPONDENT_ONE_DISCLOSURE_LIST,
                RESPONDENT_ONE_UPLOADED_PRECEDENT_H,
                RESPONDENT_ONE_PRECEDENT_AGREED,
                RESPONDENT_ONE_ANY_PRECEDENT_H
            );
            case DEFENDANT2 -> List.of(
                RESPONDENT_TWO_TRIAL_SKELETON,
                RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE,
                RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE,
                RESPONDENT_TWO_WITNESS_STATEMENT,
                RESPONDENT_TWO_WITNESS_OTHER_STATEMENT,
                RESPONDENT_TWO_WITNESS_HEARSAY,
                RESPONDENT_TWO_WITNESS_SUMMARY,
                RESPONDENT_TWO_WITNESS_REFERRED,
                RESPONDENT_TWO_EXPERT_REPORT,
                RESPONDENT_TWO_EXPERT_QUESTIONS,
                RESPONDENT_TWO_EXPERT_ANSWERS,
                RESPONDENT_TWO_EXPERT_JOINT_STATEMENT,
                RESPONDENT_TWO_DISCLOSURE,
                RESPONDENT_TWO_DISCLOSURE_LIST,
                RESPONDENT_TWO_UPLOADED_PRECEDENT_H,
                RESPONDENT_TWO_PRECEDENT_AGREED,
                RESPONDENT_TWO_ANY_PRECEDENT_H
            );
        };

        documentCategories.forEach(category ->
                                       manageDocuments.forEach(md -> addDocumentByCategoryId(
                                           md,
                                           bundlingRequestDocuments,
                                           category
                                       )));
    }

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
            case DQ_APP1 -> "Directions Questionnaire %s %s";
            case DQ_APP2 -> "Directions Questionnaire %s %s";
            case DQ_DEF1 -> "Directions Questionnaire %s %s";
            case DQ_DEF2 -> "Directions Questionnaire %s %s";
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
