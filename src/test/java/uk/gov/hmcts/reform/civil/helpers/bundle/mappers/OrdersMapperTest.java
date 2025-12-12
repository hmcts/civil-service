package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class OrdersMapperTest {

    @Mock
    private SystemGeneratedDocMapper systemGeneratedDocMapper;

    @InjectMocks
    private OrdersMapper mapper;

    @Test
    void testMapperWhenIncludesDefaultJudgmentSdoAndGeneralDismissalOrders() {
        BundlingRequestDocument doc = BundlingRequestDocument.builder().documentFileName("f").documentType("t").build();
        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), eq(BundleFileNameList.DIRECTIONS_ORDER.getDisplayName())))
            .thenReturn(singletonList(doc));
        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), eq(BundleFileNameList.ORDER.getDisplayName())))
            .thenReturn(singletonList(doc));

        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);
        assertEquals(9, result.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA1 = MockManageDocument.getManageDocumentElement(
            ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART,
            DocCategory.NOTICE_OF_DISCONTINUE
        );
        Element<ManageDocument> documentA9 = MockManageDocument.getManageDocumentElement(
            ManageDocumentType.OTHER,
            DocCategory.CASE_MAANGEMENT_ORDERS
        );

        Element<ManageDocument> documentA10 = MockManageDocument.getManageDocumentElement(
            ManageDocumentType.N9_REQUEST_MORE_TIME,
            DocCategory.HEARING_NOTICES
        );

        Element<ManageDocument> documentA2 = MockManageDocument.getManageDocumentElement(
            ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocCategory.APPLICATION_ORDERS
        );

        Element<ManageDocument> documentA3 = MockManageDocument.getManageDocumentElement(
            ManageDocumentType.MEDIATION_AGREEMENT,
            DocCategory.JUDGEMENTS
        );

        return List.of(
            documentA1, documentA2, documentA3, documentA9, documentA10
        );
    }
}
