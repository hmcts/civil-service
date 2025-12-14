package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF1;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.CLAIMANT1;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.DEFENDANT1;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.DEFENDANT2;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class DQMapper implements ManageDocMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final SystemGeneratedDocMapper systemGeneratedDocMapper;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DocCategory.APP1_DQ.getValue(), CLAIMANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DocCategory.DEF1_DEFENSE_DQ.getValue(), DEFENDANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DQ_DEF1.getValue(), DEFENDANT1));

        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(
            caseData, DocCategory.DEF2_DEFENSE_DQ.getValue(), DEFENDANT2));

        // No-category DQ
        List<uk.gov.hmcts.reform.civil.model.common.Element<uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument>> dqNoCategory =
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> DocumentType.DIRECTIONS_QUESTIONNAIRE
                    .equals(caseDocumentElement.getValue().getDocumentType())
                    && caseDocumentElement.getValue().getDocumentLink().getCategoryID() == null)
                .collect(Collectors.toCollection(ArrayList::new));
        bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
            dqNoCategory,
            uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList
                .DIRECTIONS_QUESTIONNAIRE_NO_CATEGORY_ID.getDisplayName()
        ));
        //ManageDocuments
        List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
        if (!manageDocuments.isEmpty()) {
            Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                                   addManageDocumentsByDocCategory(
                                                                       manageDocuments,
                                                                       partyType,
                                                                       bundlingRequestDocuments
                                                                   )
            );
        }
        return wrapElements(bundlingRequestDocuments);
    }
}
