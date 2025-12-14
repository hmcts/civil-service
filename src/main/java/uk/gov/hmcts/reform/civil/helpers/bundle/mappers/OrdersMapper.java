package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.APPLICATION_ORDERS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CASE_MAANGEMENT_ORDERS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.HEARING_NOTICES;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.JUDGEMENTS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.NOTICE_OF_DISCONTINUE;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Service
@RequiredArgsConstructor
public class OrdersMapper {

    private final SystemGeneratedDocMapper systemGeneratedDocMapper;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> DocumentType.DEFAULT_JUDGMENT_SDO_ORDER
                    .equals(caseDocumentElement.getValue().getDocumentType())).toList(),
            BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()
        ));
        bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> DocumentType.SDO_ORDER
                    .equals(caseDocumentElement.getValue().getDocumentType())).toList(),
            BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()
        ));
        if (caseData.getGeneralOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
                caseData.getGeneralOrderDocStaff(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        if (caseData.getDismissalOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
                caseData.getDismissalOrderDocStaff(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        if (caseData.getDirectionOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
                caseData.getDirectionOrderDocStaff(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        //ManageDocuments
        List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
        if (!manageDocuments.isEmpty()) {
            manageDocuments.forEach(md ->
                                        addDocumentByCategoryId(md, bundlingRequestDocuments, NOTICE_OF_DISCONTINUE));
            manageDocuments.forEach(md ->
                                        addDocumentByCategoryId(md, bundlingRequestDocuments, CASE_MAANGEMENT_ORDERS));
            manageDocuments.forEach(md ->
                                        addDocumentByCategoryId(md, bundlingRequestDocuments, HEARING_NOTICES));
            manageDocuments.forEach(md ->
                                        addDocumentByCategoryId(md, bundlingRequestDocuments, APPLICATION_ORDERS));
            manageDocuments.forEach(md ->
                                        addDocumentByCategoryId(md, bundlingRequestDocuments, JUDGEMENTS));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private void addDocumentByCategoryId(Element<ManageDocument> md,
                                                List<BundlingRequestDocument> bundlingRequestDocuments,
                                                DocCategory docCategory) {
        if (docCategory.getValue().equals(md.getValue().getDocumentLink().getCategoryID())) {
            bundlingRequestDocuments.add(
                buildBundlingRequestDoc(
                    generateDocName(BundleFileNameList.ORDER.getDisplayName(), md.getValue().getDocumentName(), null,
                                    LocalDateTime.parse(md.getValue().getDocumentLink().getUploadTimestamp()).toLocalDate()),
                    md.getValue().getDocumentLink(),
                    md.getValue().getDocumentType().name()
                )
            );
        }
    }
}
