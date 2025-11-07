package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestDocsOrganizer;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getWitnessDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class WitnessStatementsMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;
    private final BundleRequestDocsOrganizer bundleRequestDocsOrganizer;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        Party party = bundleDocumentsRetrieval.getPartyByPartyType(partyType, caseData);
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementsMap =
            bundleRequestDocsOrganizer.groupWitnessStatementsByName(
                getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadType.WITNESS_STATEMENT, caseData));

        List<Element<UploadEvidenceWitness>> witnessStatementSelf =
            bundleDocumentsRetrieval.getSelfStatement(witnessStatementsMap, party);

        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            witnessStatementSelf,
            BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
            EvidenceUploadType.WITNESS_STATEMENT.name(),
            partyType,
            true
        ));

        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            witnessStatementsMap,
            BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
            EvidenceUploadType.WITNESS_STATEMENT.name(),
            party
        ));

        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadType.WITNESS_SUMMARY, caseData),
            BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
            EvidenceUploadType.WITNESS_SUMMARY.name(),
            partyType,
            false
        ));

        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.DOCUMENTS_REFERRED, caseData),
            BundleFileNameList.DOC_REFERRED_TO.getDisplayName(),
            EvidenceUploadType.DOCUMENTS_REFERRED.name(),
            partyType
        ));

        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadType.NOTICE_OF_INTENTION, caseData),
            BundleFileNameList.HEARSAY_NOTICE.getDisplayName(),
            EvidenceUploadType.NOTICE_OF_INTENTION.name(),
            partyType,
            false
        ));

        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial =
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.DOCUMENTARY, caseData);

        if (documentEvidenceForTrial != null) {
            bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                    documentEvidenceForTrial,
                    TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.getDisplayNames(),
                    false
                ),
                BundleFileNameList.NOTICE_TO_ADMIT_FACTS.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.name(),
                partyType
            ));
        }

        return wrapElements(bundlingRequestDocuments);
    }
}
