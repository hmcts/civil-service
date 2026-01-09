package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;

@ExtendWith(MockitoExtension.class)
class DQMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Mock
    private SystemGeneratedDocMapper systemGeneratedDocMapper;

    @InjectMocks
    private DQMapper mapper;

    @Test
    void testMapperWhenIncludesAllDQVariantsAndNoCategory() {
        CaseData caseData = getCaseData();

        BundlingRequestDocument bundlingRequestDocument = new BundlingRequestDocument()
            .setDocumentFileName("f")
            .setDocumentType("t");
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.APP1_DQ.getValue(), PartyType.CLAIMANT1))
            .thenReturn(singletonList(bundlingRequestDocument));
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.DEF1_DEFENSE_DQ.getValue(), PartyType.DEFENDANT1))
            .thenReturn(singletonList(bundlingRequestDocument));
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.DEF2_DEFENSE_DQ.getValue(), PartyType.DEFENDANT2))
            .thenReturn(singletonList(bundlingRequestDocument));
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.DQ_DEF1.getValue(), PartyType.DEFENDANT1))
            .thenReturn(singletonList(bundlingRequestDocument));
        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), any()))
            .thenReturn(singletonList(bundlingRequestDocument));

        caseData.setManageDocuments(getTestDocuments());
        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);
        assertEquals(9, result.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA5 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocCategory.DQ_APP1
        );

        Element<ManageDocument> documentA6 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            DocCategory.DQ_APP2
        );

        Element<ManageDocument> documentA7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DocCategory.DQ_DEF1
        );

        Element<ManageDocument> documentA8 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocCategory.DQ_DEF2
        );
        return List.of(
            documentA5, documentA6, documentA7, documentA8
        );
    }
}
