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
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class DisclosedDocumentsMapper implements ManageDocMapper {

    private static final String DOC_FILE_NAME = "DOC_FILE_NAME";

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(
                partyType,
                EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE, caseData
            ),
            DOC_FILE_NAME,
            EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE.name(),
            partyType
        ));

        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialList =
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.DOCUMENTARY, caseData);

        if (documentEvidenceForTrialList != null) {
            bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                    documentEvidenceForTrialList,
                    TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(),
                    true
                ),
                DOC_FILE_NAME,
                EvidenceUploadType.DOCUMENTARY.name(),
                partyType
            ));
        }
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
                APPLICANT_ONE_DISCLOSURE,
                APPLICANT_ONE_DISCLOSURE_LIST
            );
            case CLAIMANT2 -> List.of(
                APPLICANT_TWO_DISCLOSURE,
                APPLICANT_TWO_DISCLOSURE_LIST
            );
            case DEFENDANT1 -> List.of(
                RESPONDENT_ONE_DISCLOSURE,
                RESPONDENT_ONE_DISCLOSURE_LIST
            );
            case DEFENDANT2 -> List.of(
                RESPONDENT_TWO_DISCLOSURE,
                RESPONDENT_TWO_DISCLOSURE_LIST
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
