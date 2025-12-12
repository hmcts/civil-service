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
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_ONE_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.APPLICANT_TWO_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_ONE_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_HEARSAY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_OTHER_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_REFERRED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_STATEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.RESPONDENT_TWO_WITNESS_SUMMARY;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleTestUtil.getCaseData;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9A_PAPER_ADMISSION_FULL_OR_PART;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.N9B_PAPER_DEFENCE_OR_COUNTERCLAIM;

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
        when(bundleDocumentsRetrieval.getPartyByPartyType(eq(PartyType.CLAIMANT1), any()))
            .thenReturn(Party.builder().individualFirstName("cl1Fname").partyName("applicant1")
                            .type(Party.Type.INDIVIDUAL).build());

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

        CaseData caseData = getCaseData();
        caseData.setManageDocuments(getTestDocuments());
        List<Element<BundlingRequestDocument>> resultApp1 = mapper.map(caseData, PartyType.CLAIMANT1);
        assertEquals(11, resultApp1.size());
        List<Element<BundlingRequestDocument>> resultApp2 = mapper.map(caseData, PartyType.CLAIMANT2);
        assertEquals(5, resultApp2.size());
        List<Element<BundlingRequestDocument>> resultResp1 = mapper.map(caseData, PartyType.DEFENDANT1);
        assertEquals(5, resultResp1.size());
        List<Element<BundlingRequestDocument>> resultResp2 = mapper.map(caseData, PartyType.DEFENDANT2);
        assertEquals(5, resultResp2.size());
    }

    private List<Element<ManageDocument>> getTestDocuments() {
        Element<ManageDocument> documentA1 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, APPLICANT_ONE_WITNESS_STATEMENT);

        Element<ManageDocument> documentA2 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM, APPLICANT_ONE_WITNESS_OTHER_STATEMENT);

        Element<ManageDocument> documentA3 = getManageDocumentElement(
            MEDIATION_AGREEMENT, APPLICANT_ONE_WITNESS_HEARSAY);

        Element<ManageDocument> documentA4 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, APPLICANT_ONE_WITNESS_SUMMARY);
        Element<ManageDocument> documentA5 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM, APPLICANT_ONE_WITNESS_REFERRED);

        Element<ManageDocument> documentA6 = getManageDocumentElement(
            MEDIATION_AGREEMENT, APPLICANT_TWO_WITNESS_STATEMENT);

        Element<ManageDocument> documentA7 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, APPLICANT_TWO_WITNESS_OTHER_STATEMENT);

        Element<ManageDocument> documentA8 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM, APPLICANT_TWO_WITNESS_HEARSAY);

        Element<ManageDocument> documentA9 = getManageDocumentElement(
            MEDIATION_AGREEMENT, APPLICANT_TWO_WITNESS_SUMMARY);

        Element<ManageDocument> documentA10 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, APPLICANT_TWO_WITNESS_REFERRED);

        Element<ManageDocument> documentA11 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, RESPONDENT_ONE_WITNESS_STATEMENT);

        Element<ManageDocument> documentA21 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM, RESPONDENT_ONE_WITNESS_OTHER_STATEMENT);

        Element<ManageDocument> documentA31 = getManageDocumentElement(
            MEDIATION_AGREEMENT, RESPONDENT_ONE_WITNESS_HEARSAY);

        Element<ManageDocument> documentA41 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, RESPONDENT_ONE_WITNESS_SUMMARY);
        Element<ManageDocument> documentA51 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM, RESPONDENT_ONE_WITNESS_REFERRED);

        Element<ManageDocument> documentA61 = getManageDocumentElement(
            MEDIATION_AGREEMENT, RESPONDENT_TWO_WITNESS_STATEMENT);

        Element<ManageDocument> documentA71 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, RESPONDENT_TWO_WITNESS_OTHER_STATEMENT);

        Element<ManageDocument> documentA81 = getManageDocumentElement(
            N9B_PAPER_DEFENCE_OR_COUNTERCLAIM, RESPONDENT_TWO_WITNESS_HEARSAY);

        Element<ManageDocument> documentA91 = getManageDocumentElement(
            MEDIATION_AGREEMENT, RESPONDENT_TWO_WITNESS_SUMMARY);

        Element<ManageDocument> documentA17 = getManageDocumentElement(
            N9A_PAPER_ADMISSION_FULL_OR_PART, RESPONDENT_TWO_WITNESS_REFERRED);

        return List.of(documentA1, documentA2, documentA3, documentA4, documentA5, documentA6, documentA7, documentA8,
                       documentA9, documentA10, documentA11, documentA21, documentA31, documentA41, documentA51,
                       documentA61, documentA71, documentA81, documentA91, documentA17);
    }
}
