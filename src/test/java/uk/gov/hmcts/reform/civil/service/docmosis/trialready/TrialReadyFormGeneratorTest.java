package uk.gov.hmcts.reform.civil.service.docmosis.trialready;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.TRIAL_READY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.TRIAL_READY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    TrialReadyFormGenerator.class,
    JacksonAutoConfiguration.class
})

public class TrialReadyFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName_application = String.format(
        TRIAL_READY.getDocumentTitle(), "Rambo", formatLocalDate(LocalDate.now(), DATE));
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName_application)
        .documentType(TRIAL_READY_DOCUMENT)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private TrialReadyFormGenerator generator;

    @Test
    void shouldTrialReadyFormGeneratorOneForm_whenValidDataIsProvided() {
        // Given
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(TRIAL_READY)))
            .thenReturn(new DocmosisDocument(TRIAL_READY.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, TRIAL_READY_DOCUMENT)))
            .thenReturn(CASE_DOCUMENT);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder().trialReadyApplicant(YesOrNo.YES)
            .applicantRevisedHearingRequirements(
                RevisedHearingRequirements.builder()
                    .revisedHearingRequirements(YesOrNo.YES)
                    .revisedHearingComments("Revised Hearing Comments").build()).build();
        // When
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN, "GenerateTrialReadyFormApplicant");
        // Then
        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, TRIAL_READY_DOCUMENT));
    }

    @Test
    void shouldTrialReadyFormGeneratorOneForm_whenRespondent1GenerateDocs() {
        // Given
        String fileName = String.format(
            TRIAL_READY.getDocumentTitle(), "Trader", formatLocalDate(LocalDate.now(), DATE));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(TRIAL_READY)))
            .thenReturn(new DocmosisDocument(TRIAL_READY.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, TRIAL_READY_DOCUMENT)))
            .thenReturn(CASE_DOCUMENT);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build().toBuilder().trialReadyRespondent1(YesOrNo.YES)
            .respondent1RevisedHearingRequirements(
                RevisedHearingRequirements.builder()
                    .revisedHearingRequirements(YesOrNo.YES)
                    .revisedHearingComments("Revised Hearing Comments").build()).build();
        // When
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN, "GenerateTrialReadyFormRespondent1");
        // Then
        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, TRIAL_READY_DOCUMENT));
    }

    @Test
    void shouldTrialReadyFormGeneratorOneForm_whenRespondent2GenerateDocs() {
        // Given
        String fileName = String.format(
            TRIAL_READY.getDocumentTitle(), "Company", formatLocalDate(LocalDate.now(), DATE));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(TRIAL_READY)))
            .thenReturn(new DocmosisDocument(TRIAL_READY.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, TRIAL_READY_DOCUMENT)))
            .thenReturn(CASE_DOCUMENT);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Company").build())
            .build().toBuilder().trialReadyRespondent2(YesOrNo.NO)
            .respondent2RevisedHearingRequirements(
                RevisedHearingRequirements.builder()
                    .revisedHearingRequirements(YesOrNo.YES)
                    .revisedHearingComments("Revised Hearing Comments").build()).build();
        // When
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN, "GenerateTrialReadyFormRespondent2");
        // Then
        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, TRIAL_READY_DOCUMENT));
    }

    @Test
    void shouldTrialReadyFormGeneratorOneForm_whenRespondent2OrganisationGenerateDocs() {
        // Given
        String fileName = String.format(
            TRIAL_READY.getDocumentTitle(), "Organisation", formatLocalDate(LocalDate.now(), DATE));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(TRIAL_READY)))
            .thenReturn(new DocmosisDocument(TRIAL_READY.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, TRIAL_READY_DOCUMENT)))
            .thenReturn(CASE_DOCUMENT);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .respondent2(Party.builder().type(Party.Type.ORGANISATION).organisationName("Organisation").build())
            .build().toBuilder().trialReadyRespondent2(YesOrNo.YES)
            .respondent2RevisedHearingRequirements(
                RevisedHearingRequirements.builder()
                    .revisedHearingRequirements(YesOrNo.NO)
                    .revisedHearingComments("Revised Hearing Comments").build()).build();
        // When
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN, "GenerateTrialReadyFormRespondent2");
        // Then
        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, TRIAL_READY_DOCUMENT));
    }
}
