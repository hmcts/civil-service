package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class ExpertEvidenceMapper {

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
        return wrapElements(bundlingRequestDocuments);
    }
}
