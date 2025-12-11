package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_APP1;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_APP2;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF1;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DQ_DEF2;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.CLAIMANT1;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.CLAIMANT2;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.DEFENDANT1;
import static uk.gov.hmcts.reform.civil.helpers.bundle.PartyType.DEFENDANT2;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class DQMapper {

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
            manageDocuments.forEach(manageDocument -> {
                extractedDocumentByCategoryAndPartyType(DQ_APP1, manageDocument, bundlingRequestDocuments, CLAIMANT1);
                extractedDocumentByCategoryAndPartyType(DQ_APP2, manageDocument, bundlingRequestDocuments, CLAIMANT2);
                extractedDocumentByCategoryAndPartyType(DQ_DEF1, manageDocument, bundlingRequestDocuments, DEFENDANT1);
                extractedDocumentByCategoryAndPartyType(DQ_DEF2, manageDocument, bundlingRequestDocuments, DEFENDANT2);
            });
        }
        return wrapElements(bundlingRequestDocuments);
    }

    private void extractedDocumentByCategoryAndPartyType(DocCategory docCategory,
                                                                Element<ManageDocument> md,
                                                                List<BundlingRequestDocument> bundlingRequestDocuments,
                                                                PartyType partyType) {
        if (docCategory.getValue().equals(md.getValue().getDocumentLink().getCategoryID())) {
            bundlingRequestDocuments.add(
                buildBundlingRequestDoc(
                    generateDocName(BundleFileNameList.DIRECTIONS_QUESTIONNAIRE.getDisplayName(),
                                    partyType.getDisplayName(), null,
                                    LocalDateTime.parse(md.getValue().getDocumentLink().getUploadTimestamp()).toLocalDate()
                    ),
                    md.getValue().getDocumentLink(),
                    md.getValue().getDocumentType().name()
                )
            );
        }
    }
}
