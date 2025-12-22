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
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_DISCLOSURE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_TRIAL_SKELETON;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9_REQUEST_MORE_TIME;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.OTHER;

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
        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(any(), eq(TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames()), eq(false)))
            .thenReturn(Collections.emptyList());
        when(bundleDocumentsRetrieval.getDocumentaryEvidenceByType(any(), eq(TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames()), eq(false)))
            .thenReturn(Collections.emptyList());

        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(any(), any(), any(), any()))
            .thenReturn(java.util.Collections.singletonList(
                BundlingRequestDocument.builder().documentFileName("f").documentType("t").build()
            ));

        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());
        List<Element<BundlingRequestDocument>> result = mapper.map(caseData);
        assertEquals(28, result.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA1 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APPLICANT_ONE_TRIAL_SKELETON
        );
        Element<ManageDocument> documentA9 = getManageDocumentElement(
            OTHER,
            APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE
        );

        Element<ManageDocument> documentA10 = getManageDocumentElement(
            N9_REQUEST_MORE_TIME,
            APPLICANT_ONE_TRIAL_DOC_TIME_TABLE
        );

        Element<ManageDocument> documentA2 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APPLICANT_TWO_TRIAL_SKELETON
        );

        Element<ManageDocument> documentA3 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE
        );

        Element<ManageDocument> documentA4 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APPLICANT_TWO_TRIAL_DOC_TIME_TABLE
        );
        Element<ManageDocument> documentA5 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_ONE_TRIAL_SKELETON
        );

        Element<ManageDocument> documentA6 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE
        );

        Element<ManageDocument> documentA7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            RESPONDENT_TWO_TRIAL_SKELETON
        );

        Element<ManageDocument> documentA8 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_TWO_DISCLOSURE
        );
        Element<ManageDocument> documentA82 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE
        );
        Element<ManageDocument> documentA83 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE
        );
        Element<ManageDocument> documentA84 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE
        );
        return List.of(documentA1, documentA2, documentA3, documentA4, documentA5,
                       documentA6, documentA7, documentA8, documentA9, documentA10,
                       documentA82, documentA83, documentA84);
    }
}
