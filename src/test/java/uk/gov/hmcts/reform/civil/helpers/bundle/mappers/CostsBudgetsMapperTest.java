package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_ANY_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_PRECEDENT_AGREED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_UPLOADED_PRECEDENT_H;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;

@ExtendWith(MockitoExtension.class)
class CostsBudgetsMapperTest {

    @Mock
    private ConversionToBundleRequestDocs conversionToBundleRequestDocs;

    @InjectMocks
    private CostsBudgetsMapper mapper;

    @Test
    void testCostBudgetMapper() {
        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());

        when(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(any(), any(), eq(EvidenceUploadType.COSTS.name()), eq(PartyType.CLAIMANT1)))
            .thenReturn(java.util.Collections.singletonList(
                new BundlingRequestDocument().setDocumentFileName("f").setDocumentType("t")
            ));

        List<Element<BundlingRequestDocument>> resultApplicant1 = mapper.map(caseData, PartyType.CLAIMANT1);
        assertEquals(4, resultApplicant1.size());
        List<Element<BundlingRequestDocument>> resultApplicant2 = mapper.map(caseData, PartyType.CLAIMANT2);
        assertEquals(3, resultApplicant2.size());
        List<Element<BundlingRequestDocument>> resultRespondent1 = mapper.map(caseData, PartyType.DEFENDANT1);
        assertEquals(3, resultRespondent1.size());
        List<Element<BundlingRequestDocument>> resultRespondent2 = mapper.map(caseData, PartyType.DEFENDANT2);
        assertEquals(3, resultRespondent2.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA1 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APPLICANT_ONE_UPLOADED_PRECEDENT_H);

        Element<ManageDocument> documentA2 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APPLICANT_ONE_PRECEDENT_AGREED);

        Element<ManageDocument> documentA3 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            APPLICANT_ONE_ANY_PRECEDENT_H);

        Element<ManageDocument> documentA4 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            APPLICANT_TWO_UPLOADED_PRECEDENT_H);
        Element<ManageDocument> documentA5 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            APPLICANT_TWO_PRECEDENT_AGREED);

        Element<ManageDocument> documentA6 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            APPLICANT_TWO_ANY_PRECEDENT_H);

        Element<ManageDocument> documentA7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            RESPONDENT_ONE_UPLOADED_PRECEDENT_H);

        Element<ManageDocument> documentA8 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_ONE_PRECEDENT_AGREED);

        Element<ManageDocument> documentA9 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            RESPONDENT_ONE_ANY_PRECEDENT_H);

        Element<ManageDocument> documentA10 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART,
            RESPONDENT_TWO_UPLOADED_PRECEDENT_H);

        Element<ManageDocument> documentA11 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM,
            RESPONDENT_TWO_PRECEDENT_AGREED);
        Element<ManageDocument> documentA12 = getManageDocumentElement(
            MEDIATION_AGREEMENT,
            RESPONDENT_TWO_ANY_PRECEDENT_H);
        return List.of(documentA1, documentA2, documentA3, documentA4, documentA5, documentA6,
                       documentA7, documentA8, documentA9, documentA10, documentA11, documentA12);
    }
}
