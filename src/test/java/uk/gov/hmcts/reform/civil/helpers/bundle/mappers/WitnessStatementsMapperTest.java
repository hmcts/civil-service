package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleRequestDocsOrganizer;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;

@ExtendWith(MockitoExtension.class)
class WitnessStatementsMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @Mock
    private BundleRequestDocsOrganizer bundleRequestDocsOrganizer;

    @InjectMocks
    private WitnessStatementsMapper mapper;

    @Test
    void testMapperWhenIncludesAllWitnessRelatedDocs() {
        CaseData caseData = getCaseData();

        when(bundleDocumentsRetrieval.getPartyByPartyType(eq(PartyType.CLAIMANT1), any()))
            .thenReturn(Party.builder().individualFirstName("cl1Fname").partyName("applicant1").type(Party.Type.INDIVIDUAL).build());

        when(bundleRequestDocsOrganizer.groupWitnessStatementsByName(any()))
            .thenReturn(Collections.emptyMap());

        when(bundleDocumentsRetrieval.getSelfStatement(anyMap(), any(Party.class)))
            .thenReturn(Collections.emptyList());

        BundlingRequestDocument doc = BundlingRequestDocument.builder().documentFileName("f").documentType("t").build();
        when(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            any(),
            eq(BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName()),
            eq(EvidenceUploadType.WITNESS_STATEMENT.name()),
            eq(PartyType.CLAIMANT1),
            eq(true)
        ))
            .thenReturn(singletonList(doc));
        when(conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            any(),
            eq(BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName()),
            eq(EvidenceUploadType.WITNESS_STATEMENT.name()), any(Party.class)
        ))
            .thenReturn(singletonList(doc));
        when(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            any(),
            eq(BundleFileNameList.WITNESS_SUMMARY.getDisplayName()),
            eq(EvidenceUploadType.WITNESS_SUMMARY.name()),
            eq(PartyType.CLAIMANT1),
            eq(false)
        ))
            .thenReturn(singletonList(doc));
        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            any(),
            eq(BundleFileNameList.DOC_REFERRED_TO.getDisplayName()),
            eq(EvidenceUploadType.DOCUMENTS_REFERRED.name()),
            eq(PartyType.CLAIMANT1)
        ))
            .thenReturn(singletonList(doc));
        when(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            any(),
            eq(BundleFileNameList.HEARSAY_NOTICE.getDisplayName()),
            eq(EvidenceUploadType.NOTICE_OF_INTENTION.name()),
            eq(PartyType.CLAIMANT1),
            eq(false)
        ))
            .thenReturn(singletonList(doc));

        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
            any(),
            eq(TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.getDisplayNames()),
            eq(false)
        ))
            .thenReturn(Collections.emptyList());
        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            any(),
            eq(BundleFileNameList.NOTICE_TO_ADMIT_FACTS.getDisplayName()),
            eq(TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.name()),
            eq(PartyType.CLAIMANT1)
        ))
            .thenReturn(singletonList(doc));

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData, PartyType.CLAIMANT1);

        assertEquals(6, result.size());
    }
}
