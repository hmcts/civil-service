package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.CHRONOLOGY_FILE_DISPLAY_NAME;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.SKELETON_ARGUMENT;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.TRIAL_TIMETABLE_FILE_DISPLAY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class TrialDocumentsMapper implements ManageDocMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.CASE_SUMMARY, caseData),
                    CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                    EvidenceUploadType.CASE_SUMMARY.name(),
                    partyType
                )));

        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                        getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.DOCUMENTARY, caseData),
                        TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames(),
                        false
                    ),
                    CHRONOLOGY_FILE_DISPLAY_NAME.getDisplayName(),
                    TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.name(),
                    partyType
                )));

        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                        getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.DOCUMENTARY, caseData),
                        TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames(),
                        false
                    ),
                    TRIAL_TIMETABLE_FILE_DISPLAY_NAME.getDisplayName(),
                    TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.name(),
                    partyType
                )));

        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.SKELETON_ARGUMENT, caseData),
                    SKELETON_ARGUMENT.getDisplayName(),
                    EvidenceUploadType.SKELETON_ARGUMENT.name(),
                    partyType
                )));

        List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
        if (!manageDocuments.isEmpty()) {
            Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                addManageDocuments(manageDocuments, partyType, bundlingRequestDocuments)
            );
        }
        return wrapElements(bundlingRequestDocuments);
    }

    private void addManageDocuments(List<Element<ManageDocument>> manageDocuments,
                                    PartyType partyType,
                                    List<BundlingRequestDocument> bundlingRequestDocuments) {
        List<DocumentCategory> documentCategories = switch (partyType) {
            case CLAIMANT1 -> List.of(
                APPLICANT_ONE_TRIAL_SKELETON,
                APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE,
                APPLICANT_ONE_TRIAL_DOC_TIME_TABLE
            );
            case CLAIMANT2 -> List.of(
                APPLICANT_TWO_TRIAL_SKELETON,
                APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE,
                APPLICANT_TWO_TRIAL_DOC_TIME_TABLE
            );
            case DEFENDANT1 -> List.of(
                RESPONDENT_ONE_TRIAL_SKELETON,
                RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE,
                RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE
            );
            case DEFENDANT2 -> List.of(
                RESPONDENT_TWO_TRIAL_SKELETON,
                RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE,
                RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE
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
