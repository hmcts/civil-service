package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class CostsBudgetsMapper implements ManageDocMapper {

    private static final String DOC_FILE_NAME_WITH_DATE = "DOC_FILE_NAME %s";

    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(
                partyType,
                EvidenceUploadType.COSTS,
                caseData
            ),
            DOC_FILE_NAME_WITH_DATE,
            EvidenceUploadType.COSTS.name(),
            partyType
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
                APPLICANT_ONE_UPLOADED_PRECEDENT_H,
                APPLICANT_ONE_PRECEDENT_AGREED,
                APPLICANT_ONE_ANY_PRECEDENT_H
            );
            case CLAIMANT2 -> List.of(
                APPLICANT_TWO_UPLOADED_PRECEDENT_H,
                APPLICANT_TWO_PRECEDENT_AGREED,
                APPLICANT_TWO_ANY_PRECEDENT_H
            );
            case DEFENDANT1 -> List.of(
                RESPONDENT_ONE_UPLOADED_PRECEDENT_H,
                RESPONDENT_ONE_PRECEDENT_AGREED,
                RESPONDENT_ONE_ANY_PRECEDENT_H
            );
            case DEFENDANT2 -> List.of(
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
}
