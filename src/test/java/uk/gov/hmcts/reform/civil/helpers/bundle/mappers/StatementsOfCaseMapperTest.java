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
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REPLIES_TO_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REPLY;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REQUEST_FOR_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REQUEST_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REPLIES_TO_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REPLY;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REQUEST_FOR_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REQUEST_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT1_DETAILS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT2_DETAILS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF1_DEFENSE_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF1_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF2_DEFENSE_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF2_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9_REQUEST_MORE_TIME;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.OTHER;

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

        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());

        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);

        assertEquals(34, result.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> testDocument1 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APP1_DQ
        );
        Element<ManageDocument> testDocument2 = getManageDocumentElement(
            N9_REQUEST_MORE_TIME,
            APP1_REPLIES_TO_FURTHER_INFORMATION
        );
        Element<ManageDocument> testDocument3 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            APP1_REQUEST_FOR_FURTHER_INFORMATION
        );
        Element<ManageDocument> testDocument4 = getManageDocumentElement(
            OTHER,
            APP1_REQUEST_SCHEDULE_OF_LOSS
        );
        Element<ManageDocument> testDocument5 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APP1_REPLY
        );
        Element<ManageDocument> testDocument6 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            CLAIMANT1_DETAILS_OF_CLAIM
        );
        Element<ManageDocument> testDocument7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            PARTICULARS_OF_CLAIM
        );
        Element<ManageDocument> testDocument11 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APP2_DQ
        );
        Element<ManageDocument> testDocument21 = getManageDocumentElement(
            N9_REQUEST_MORE_TIME,
            APP2_REPLIES_TO_FURTHER_INFORMATION
        );
        Element<ManageDocument> testDocument31 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            APP2_REQUEST_FOR_FURTHER_INFORMATION
        );
        Element<ManageDocument> testDocument41 = getManageDocumentElement(
            OTHER,
            APP2_REQUEST_SCHEDULE_OF_LOSS
        );
        Element<ManageDocument> testDocument51 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APP2_REPLY
        );
        Element<ManageDocument> testDocument61 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            CLAIMANT2_DETAILS_OF_CLAIM
        );
        Element<ManageDocument> testDocument71 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APP2_PARTICULARS_OF_CLAIM
        );

        Element<ManageDocument> testDocument81 = getManageDocumentElement(
            OTHER,
            DEF1_DEFENSE_DQ
        );
        Element<ManageDocument> testDocument91 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DEF1_SCHEDULE_OF_LOSS
        );
        Element<ManageDocument> testDocument611 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DEF2_DEFENSE_DQ
        );
        Element<ManageDocument> testDocument711 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DEF2_SCHEDULE_OF_LOSS
        );
        return List.of(testDocument1, testDocument2, testDocument3, testDocument4,
                       testDocument5, testDocument6, testDocument7, testDocument11, testDocument21,
                       testDocument31, testDocument41, testDocument51, testDocument61, testDocument71,
                       testDocument81, testDocument91, testDocument611, testDocument711);
    }
}
