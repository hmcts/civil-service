package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RESPOND_FOR_WRITTEN_REPRESENTATION;

@ExtendWith(MockitoExtension.class)
class RespondToWrittenRepresentationGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @Mock
    private SecuredDocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @InjectMocks
    RespondToWrittenRepresentationGenerator respondToWrittenRepresentationGenerator;

    @Test
    void shouldGenerateRespondToWrittenRepresentationDocument() {
        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
            .build().toBuilder().generalAppWrittenRepText("writtenRep text").build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
            .thenReturn(new DocmosisDocument(RESPOND_FOR_WRITTEN_REPRESENTATION.getDocumentTitle(), bytes));

        respondToWrittenRepresentationGenerator.generate(caseData, BEARER_TOKEN, DocUploadUtils.APPLICANT);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(RESPOND_FOR_WRITTEN_REPRESENTATION));
    }

    @Test
    void whenRespond_ShouldGetRespondToWrittenRepresentationData() {
        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
            .build().toBuilder().generalAppWrittenRepText("writtenRep Text").build();
        respondToWrittenRepresentationGenerator.setRole(DocUploadUtils.APPLICANT);
        var templateData =
            respondToWrittenRepresentationGenerator.getTemplateData(caseData, "auth");
        assertThatFieldsAreCorrect_RespondToWrittenRepresentation(templateData, caseData);
    }

    @Test
    void whenRespond_ShouldGetRespondToWrittenRepresentationDataForRespondent() {
        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
            .build().toBuilder().parentClaimantIsApplicant(YesOrNo.YES).generalAppWrittenRepText("writtenRep Text")
            .build();
        respondToWrittenRepresentationGenerator.setRole(DocUploadUtils.RESPONDENT_ONE);
        var templateData =
            respondToWrittenRepresentationGenerator.getTemplateData(caseData, "auth");
        assertEquals(templateData.getJudgeNameTitle(), caseData.getDefendant1PartyName());
    }

    @Test
    void whenRespond_ShouldGetRespondToWrittenRepresentationDataForRespondentHasClaimant() {
        CaseData caseData = CaseDataBuilder.builder().requestForInformationApplication()
            .build().toBuilder().parentClaimantIsApplicant(YesOrNo.NO).generalAppWrittenRepText("writtenRep Text")
            .build();
        respondToWrittenRepresentationGenerator.setRole(DocUploadUtils.RESPONDENT_ONE);
        var templateData =
            respondToWrittenRepresentationGenerator.getTemplateData(caseData, "auth");
        assertEquals(templateData.getJudgeNameTitle(), caseData.getClaimant1PartyName());
    }

    private void assertThatFieldsAreCorrect_RespondToWrittenRepresentation(JudgeDecisionPdfDocument templateData, CaseData caseData) {
        Assertions.assertAll(
            "Respond To Written Representation Document data should be as expected",
            () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
            () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
            () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
            () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getApplicantPartyName()),
            () -> assertEquals(templateData.getJudgeComments(), caseData.getGeneralAppWrittenRepText())
        );
    }
}
