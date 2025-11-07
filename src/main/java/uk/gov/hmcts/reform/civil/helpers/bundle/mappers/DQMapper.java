package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class DQMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final FeatureToggleService featureToggleService;
    private final SystemGeneratedDocMapper systemGeneratedDocMapper;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DocCategory.APP1_DQ.getValue(), PartyType.CLAIMANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DocCategory.DEF1_DEFENSE_DQ.getValue(), PartyType.DEFENDANT1));

        if (featureToggleService.isCaseProgressionEnabled()) {
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
                caseData, DocCategory.DQ_DEF1.getValue(), PartyType.DEFENDANT1));
        }

        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DocCategory.DEF2_DEFENSE_DQ.getValue(), PartyType.DEFENDANT2));

        // No-category DQ
        List<uk.gov.hmcts.reform.civil.model.common.Element<uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument>> dqNoCategory =
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                    .equals(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                    && caseDocumentElement.getValue().getDocumentLink().getCategoryID() == null)
                .collect(Collectors.toList());
        bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
            dqNoCategory,
            uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList
                .DIRECTIONS_QUESTIONNAIRE_NO_CATEGORY_ID.getDisplayName()
        ));

        return wrapElements(bundlingRequestDocuments);
    }
}


