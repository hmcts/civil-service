package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class ExpertEvidenceMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @InjectMocks
    private ExpertEvidenceMapper mapper;

    @Test
    void testMapperWhenReturnsAggregatedExpertDocs() {
        CaseData caseData = getCaseData();

        when(bundleDocumentsRetrieval.getAllExpertsNames(
            eq(PartyType.CLAIMANT1),
            eq(EvidenceUploadType.EXPERT_REPORT),
            any()
        ))
            .thenReturn(Set.of("e1"));
        when(bundleDocumentsRetrieval.getAllExpertsNames(
            eq(PartyType.CLAIMANT1),
            eq(EvidenceUploadType.JOINT_STATEMENT),
            any()
        ))
            .thenReturn(Set.of("j1"));

        BundlingRequestDocument doc = BundlingRequestDocument.builder().documentFileName("f").documentType("t").build();
        when(bundleDocumentsRetrieval.getAllExpertReports(
            eq(PartyType.CLAIMANT1),
            eq(EvidenceUploadType.EXPERT_REPORT),
            any(),
            eq(BundleFileNameList.EXPERT_EVIDENCE),
            any()
        ))
            .thenReturn(singletonList(doc));
        when(bundleDocumentsRetrieval.getAllOtherPartyQuestions(eq(PartyType.CLAIMANT1), any(), any()))
            .thenReturn(singletonList(doc));
        when(bundleDocumentsRetrieval.getAllExpertReports(
            eq(PartyType.CLAIMANT1),
            eq(EvidenceUploadType.ANSWERS_FOR_EXPERTS),
            any(),
            eq(BundleFileNameList.REPLIES_FROM),
            any()
        ))
            .thenReturn(singletonList(doc));
        when(bundleDocumentsRetrieval.getAllRemainingExpertQuestions(
            eq(PartyType.CLAIMANT1),
            eq(EvidenceUploadType.QUESTIONS_FOR_EXPERTS),
            any()
        ))
            .thenReturn(singletonList(doc));
        when(bundleDocumentsRetrieval.getAllRemainingExpertReports(
            eq(PartyType.CLAIMANT1),
            eq(EvidenceUploadType.ANSWERS_FOR_EXPERTS),
            any(),
            eq(BundleFileNameList.REPLIES_FROM),
            any(),
            any()
        ))
            .thenReturn(singletonList(doc));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData, PartyType.CLAIMANT1);

        assertEquals(5, result.size());
    }
}
