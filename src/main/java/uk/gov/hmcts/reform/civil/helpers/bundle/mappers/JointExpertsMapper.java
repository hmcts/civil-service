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
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JointExpertsMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            Set<String> allJointExpertsNames = bundleDocumentsRetrieval.getAllExpertsNames(
                partyType,
                EvidenceUploadType.JOINT_STATEMENT,
                caseData
            );
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
                partyType,
                EvidenceUploadType.JOINT_STATEMENT,
                caseData,
                BundleFileNameList.JOINT_STATEMENTS_OF_EXPERTS,
                allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllOtherPartyQuestions(partyType, caseData,
                allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
                partyType,
                EvidenceUploadType.ANSWERS_FOR_EXPERTS,
                caseData,
                BundleFileNameList.REPLIES_FROM,
                allJointExpertsNames
            ));
        });

        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }
}


