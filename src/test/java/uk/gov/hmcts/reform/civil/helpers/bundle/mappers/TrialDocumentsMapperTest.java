package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
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
class TrialDocumentsMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @InjectMocks
    private TrialDocumentsMapper mapper;

    @Test
    void testMapperWhenReturnsDocsForAllPartiesAndTypes() {
        CaseData caseData = getCaseData();

        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(any(), eq(TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames()), eq(false)))
            .thenReturn(Collections.emptyList());
        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(any(), eq(TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames()), eq(false)))
            .thenReturn(Collections.emptyList());

        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(any(), any(), any(), any()))
            .thenReturn(java.util.Collections.singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);

        assertEquals(16, result.size());
    }
}


