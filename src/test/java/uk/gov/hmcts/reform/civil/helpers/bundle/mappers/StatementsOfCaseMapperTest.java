package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class StatementsOfCaseMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @Mock
    private SystemGeneratedDocMapper systemGeneratedDocMapper;

    @InjectMocks
    private StatementsOfCaseMapper mapper;

    @Test
    void testMapperWhenIncludesClaimFormParticularsResponsesAndDocEvidences() {
        CaseData caseData = getCaseData();

        when(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(any(), eq(BundleFileNameList.CLAIM_FORM.getDisplayName())))
            .thenReturn(java.util.Collections.singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        when(bundleDocumentsRetrieval.getParticularsOfClaimName(any(), any()))
            .thenReturn("name");

        when(bundleDocumentsRetrieval.getSortedDefendantDefenceAndClaimantReply(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(any(), eq(TypeOfDocDocumentaryEvidenceOfTrial.PART18.getDisplayNames()), eq(false)))
            .thenReturn(Collections.emptyList());
        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(any(), eq(TypeOfDocDocumentaryEvidenceOfTrial.SCHEDULE_OF_LOSS.getDisplayNames()), eq(false)))
            .thenReturn(Collections.emptyList());

        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(any(), any(), any(), any(PartyType.class)))
            .thenReturn(java.util.Collections.singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);

        assertEquals(16, result.size());
    }
}
