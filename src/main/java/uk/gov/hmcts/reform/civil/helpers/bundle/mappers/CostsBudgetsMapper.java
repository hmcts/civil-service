package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

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
        List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
        if (!manageDocuments.isEmpty()) {
            addManageDocuments(manageDocuments, partyType, bundlingRequestDocuments);
        }
        return wrapElements(bundlingRequestDocuments);
    }
}
