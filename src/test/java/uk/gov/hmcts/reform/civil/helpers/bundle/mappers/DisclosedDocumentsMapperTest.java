package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
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

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE_LIST;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9_REQUEST_MORE_TIME;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.OTHER;

@ExtendWith(MockitoExtension.class)
class DisclosedDocumentsMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @InjectMocks
    private DisclosedDocumentsMapper mapper;

    @Test
    void testMapperWhenDocumentsForDisclosureAndDocumentary() {
        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            any(),
            any(),
            eq(EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE.name()),
            eq(PartyType.CLAIMANT1)
        ))
            .thenReturn(singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
            any(),
            eq(TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames()),
            eq(true)
        ))
            .thenReturn(Collections.emptyList());

        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            any(),
            any(),
            eq(EvidenceUploadType.DOCUMENTARY.name()),
            eq(PartyType.CLAIMANT1)
        ))
            .thenReturn(singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());

        List<Element<BundlingRequestDocument>> resultA1 = mapper.map(caseData, PartyType.CLAIMANT1);
        assertEquals(4, resultA1.size());
        List<Element<BundlingRequestDocument>> resultA2 = mapper.map(caseData, PartyType.CLAIMANT2);
        assertEquals(2, resultA2.size());
        List<Element<BundlingRequestDocument>> resultR1 = mapper.map(caseData, PartyType.DEFENDANT1);
        assertEquals(2, resultR1.size());
        List<Element<BundlingRequestDocument>> resultR2 = mapper.map(caseData, PartyType.DEFENDANT2);
        assertEquals(4, resultR2.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA1 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            RESPONDENT_TWO_DISCLOSURE_LIST
        );
        Element<ManageDocument> documentA9 = getManageDocumentElement(
            OTHER,
            RESPONDENT_TWO_DISCLOSURE_LIST
        );

        Element<ManageDocument> documentA10 = getManageDocumentElement(
            N9_REQUEST_MORE_TIME,
            RESPONDENT_TWO_DISCLOSURE_LIST
        );

        Element<ManageDocument> documentA2 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APPLICANT_ONE_DISCLOSURE
        );

        Element<ManageDocument> documentA3 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            APPLICANT_ONE_DISCLOSURE_LIST
        );

        Element<ManageDocument> documentA4 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APPLICANT_TWO_DISCLOSURE
        );
        Element<ManageDocument> documentA5 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APPLICANT_TWO_DISCLOSURE_LIST
        );

        Element<ManageDocument> documentA6 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            RESPONDENT_ONE_DISCLOSURE
        );

        Element<ManageDocument> documentA7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            RESPONDENT_ONE_DISCLOSURE_LIST
        );

        Element<ManageDocument> documentA8 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_TWO_DISCLOSURE
        );
        return List.of(
            documentA1, documentA2, documentA3, documentA4, documentA5, documentA6,
            documentA7, documentA8, documentA9, documentA10
        );
    }
}
