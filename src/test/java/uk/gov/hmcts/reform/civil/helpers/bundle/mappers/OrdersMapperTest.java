package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
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
        CaseData caseData = getCaseData();

        BundlingRequestDocument doc = BundlingRequestDocument.builder().documentFileName("f").documentType("t").build();
        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), eq(BundleFileNameList.DIRECTIONS_ORDER.getDisplayName())))
            .thenReturn(singletonList(doc));
        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), eq(BundleFileNameList.ORDER.getDisplayName())))
            .thenReturn(singletonList(doc));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);

        assertEquals(4, result.size());
    }
}
