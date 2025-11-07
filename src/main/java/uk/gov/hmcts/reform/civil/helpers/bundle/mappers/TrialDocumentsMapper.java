package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.CHRONOLOGY_FILE_DISPLAY_NAME;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.TRIAL_TIMETABLE_FILE_DISPLAY_NAME;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList.SKELETON_ARGUMENT;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class TrialDocumentsMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        Arrays.stream(PartyType.values()).collect(Collectors.toList()).forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.CASE_SUMMARY, caseData),
                    CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                    EvidenceUploadType.CASE_SUMMARY.name(),
                    partyType
                )));

        Arrays.stream(PartyType.values()).collect(Collectors.toList()).forEach(partyType ->
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

        Arrays.stream(PartyType.values()).collect(Collectors.toList()).forEach(partyType ->
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

        Arrays.stream(PartyType.values()).collect(Collectors.toList()).forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.SKELETON_ARGUMENT, caseData),
                    SKELETON_ARGUMENT.getDisplayName(),
                    EvidenceUploadType.SKELETON_ARGUMENT.name(),
                    partyType
                )));

        return wrapElements(bundlingRequestDocuments);
    }
}


