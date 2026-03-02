package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RESPOND_FOR_INFORMATION;

@ExtendWith(MockitoExtension.class)
public class RespondForInformationGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @Mock
    private SecuredDocumentManagementService documentManagementService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @InjectMocks
    RespondForInformationGenerator respondForInformationGenerator;

    @Test
    void shouldGenerateRespondForInformationDocument() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .build().copy().generalAppAddlnInfoText("more info").build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any()))
                .thenReturn(new DocmosisDocument(RESPOND_FOR_INFORMATION.getDocumentTitle(), bytes));

        respondForInformationGenerator.generate(caseData, BEARER_TOKEN, DocUploadUtils.APPLICANT);

        verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.REQUEST_FOR_INFORMATION)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                eq(RESPOND_FOR_INFORMATION));
    }

    @Test
    void whenRespond_ShouldGetRespondForInformationData() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication()
                .build().copy().generalAppAddlnInfoText("more info").build();
        respondForInformationGenerator.setRole(DocUploadUtils.APPLICANT);
        var templateData =
                respondForInformationGenerator.getTemplateData(caseData, "auth");
        assertThatFieldsAreCorrect_RespondForInformation(templateData, caseData);
    }

    private void assertThatFieldsAreCorrect_RespondForInformation(JudgeDecisionPdfDocument templateData, GeneralApplicationCaseData caseData) {
        Assertions.assertAll(
                "Respond For Information Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getApplicantPartyName()),
                () -> assertEquals(templateData.getJudgeComments(), caseData.getGeneralAppAddlnInfoText())
        );
    }
}
