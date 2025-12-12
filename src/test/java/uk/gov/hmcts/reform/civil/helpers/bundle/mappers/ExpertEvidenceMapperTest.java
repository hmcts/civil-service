package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9_REQUEST_MORE_TIME;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.OTHER;

@ExtendWith(MockitoExtension.class)
class ExpertEvidenceMapperTest {

    @Mock
    private BundleDocumentsRetrieval bundleDocumentsRetrieval;

    @InjectMocks
    private ExpertEvidenceMapper mapper;

    @Test
    void testMapperWhenReturnsAggregatedExpertDocs() {
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

        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());

        List<Element<BundlingRequestDocument>> resultApp1 = mapper.map(caseData, PartyType.CLAIMANT1);
        assertEquals(9, resultApp1.size());
        List<Element<BundlingRequestDocument>> resultApp2 = mapper.map(caseData, PartyType.CLAIMANT2);
        assertEquals(4, resultApp2.size());
        List<Element<BundlingRequestDocument>> resultResp1 = mapper.map(caseData, PartyType.DEFENDANT1);
        assertEquals(4, resultResp1.size());
        List<Element<BundlingRequestDocument>> resultResp2 = mapper.map(caseData, PartyType.DEFENDANT2);
        assertEquals(5, resultResp2.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA1 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DocumentCategory.APPLICANT_ONE_UPLOADED_PRECEDENT_H
        );

        Element<ManageDocument> documentA2 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocumentCategory.APPLICANT_ONE_EXPERT_REPORT
        );
        Element<ManageDocument> documentA3 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            DocumentCategory.APPLICANT_ONE_EXPERT_QUESTIONS
        );
        Element<ManageDocument> documentA4 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DocumentCategory.APPLICANT_ONE_EXPERT_ANSWERS
        );
        Element<ManageDocument> documentA5 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocumentCategory.APPLICANT_ONE_EXPERT_JOINT_STATEMENT
        );
        Element<ManageDocument> documentA6 = getManageDocumentElement(
            MEDIATION_AGREEMENT,

            DocumentCategory.APPLICANT_TWO_EXPERT_REPORT
        );
        Element<ManageDocument> documentA7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DocumentCategory.APPLICANT_TWO_EXPERT_QUESTIONS
        );
        Element<ManageDocument> documentA8 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocumentCategory.APPLICANT_TWO_EXPERT_ANSWERS
        );
        Element<ManageDocument> documentA9 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            DocumentCategory.APPLICANT_TWO_EXPERT_JOINT_STATEMENT
        );

        Element<ManageDocument> documentA10 = getManageDocumentElement(
            N9_REQUEST_MORE_TIME,
            DocumentCategory.RESPONDENT_ONE_EXPERT_REPORT
        );
        Element<ManageDocument> documentA11 = getManageDocumentElement(
            OTHER,
            DocumentCategory.RESPONDENT_ONE_EXPERT_QUESTIONS
        );
        Element<ManageDocument> documentA12 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            DocumentCategory.RESPONDENT_ONE_EXPERT_ANSWERS
        );
        Element<ManageDocument> documentA13 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            DocumentCategory.RESPONDENT_ONE_EXPERT_JOINT_STATEMENT
        );

        Element<ManageDocument> documentA18 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            DocumentCategory.RESPONDENT_TWO_EXPERT_REPORT
        );
        Element<ManageDocument> documentA19 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            DocumentCategory.RESPONDENT_TWO_EXPERT_QUESTIONS
        );
        Element<ManageDocument> documentA20 = getManageDocumentElement(
            N9_REQUEST_MORE_TIME,
            DocumentCategory.RESPONDENT_TWO_EXPERT_ANSWERS
        );
        Element<ManageDocument> documentA21 = getManageDocumentElement(
            OTHER,
            DocumentCategory.RESPONDENT_TWO_EXPERT_JOINT_STATEMENT
        );
        Element<ManageDocument> documentA22 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            DocumentCategory.RESPONDENT_TWO_EXPERT_JOINT_STATEMENT
        );

        return List.of(documentA1, documentA2, documentA3, documentA4, documentA5, documentA6,
                       documentA7, documentA8, documentA9, documentA10, documentA11, documentA12,
                       documentA13, documentA18, documentA19, documentA20, documentA21, documentA22);
    }
}
