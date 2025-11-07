package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class CostsBudgetsMapperTest {

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @InjectMocks
    private CostsBudgetsMapper mapper;

    @Test
    void testCostBudgetMapper() {
        CaseData caseData = getCaseData();

        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(any(), any(), eq(EvidenceUploadType.COSTS.name()), eq(PartyType.CLAIMANT1)))
            .thenReturn(java.util.Collections.singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData, PartyType.CLAIMANT1);
        assertEquals(1, result.size());
    }
}


