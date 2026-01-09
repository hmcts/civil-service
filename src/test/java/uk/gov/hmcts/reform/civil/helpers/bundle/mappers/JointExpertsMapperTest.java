package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class JointExpertsMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @InjectMocks
    private JointExpertsMapper mapper;

    @Test
    void testMapperForAllPartiesAndJointExperts() {
        CaseData caseData = getCaseData();

        when(bundleDocumentsRetrieval.getAllExpertsNames(any(), eq(EvidenceUploadType.JOINT_STATEMENT), any()))
            .thenReturn(Set.of("j1"));
        BundlingRequestDocument doc = new BundlingRequestDocument()
            .setDocumentFileName("f")
            .setDocumentType("t");
        when(bundleDocumentsRetrieval.getAllExpertReports(any(), eq(EvidenceUploadType.JOINT_STATEMENT), any(), eq(BundleFileNameList.JOINT_STATEMENTS_OF_EXPERTS), anySet()))
            .thenReturn(singletonList(doc));
        when(bundleDocumentsRetrieval.getAllOtherPartyQuestions(any(), any(), anySet()))
            .thenReturn(singletonList(doc));
        when(bundleDocumentsRetrieval.getAllExpertReports(any(), eq(EvidenceUploadType.ANSWERS_FOR_EXPERTS), any(), eq(BundleFileNameList.REPLIES_FROM), anySet()))
            .thenReturn(singletonList(doc));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);

        assertEquals(12, result.size());
    }
}
