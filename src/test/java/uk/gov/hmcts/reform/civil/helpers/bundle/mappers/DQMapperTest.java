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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class DQMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private SystemGeneratedDocMapper systemGeneratedDocMapper;

    @InjectMocks
    private DQMapper mapper;

    @Test
    void testMapperWhenIncludesAllDQVariantsAndNoCategory() {
        CaseData caseData = getCaseData();

        BundlingRequestDocument bundlingRequestDocument = BundlingRequestDocument.builder().documentFileName("f").documentType("t").build();
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.APP1_DQ.getValue(), PartyType.CLAIMANT1))
            .thenReturn(singletonList(bundlingRequestDocument));
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.DEF1_DEFENSE_DQ.getValue(), PartyType.DEFENDANT1))
            .thenReturn(singletonList(bundlingRequestDocument));
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.DEF2_DEFENSE_DQ.getValue(), PartyType.DEFENDANT2))
            .thenReturn(singletonList(bundlingRequestDocument));

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
        when(bundleDocumentsRetrieval.getDqByCategoryId(caseData, DocCategory.DQ_DEF1.getValue(), PartyType.DEFENDANT1))
            .thenReturn(singletonList(bundlingRequestDocument));

        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), any()))
            .thenReturn(singletonList(bundlingRequestDocument));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);

        assertEquals(5, result.size());
    }
}
