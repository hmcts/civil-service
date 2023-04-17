package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

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
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;

import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JudgeFinalOrderGenerator.class,
    JacksonAutoConfiguration.class
})
public class JudgeFinalOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileFreeForm = String.format(FREE_FORM_ORDER_PDF.getDocumentTitle(), LocalDate.now());
    private static final String assistedForm = String.format(ASSISTED_ORDER_PDF.getDocumentTitle(), LocalDate.now());

    private static final CaseDocument FREE_FROM_ORDER = CaseDocumentBuilder.builder()
        .documentName(fileFreeForm)
        .documentType(JUDGE_FINAL_ORDER)
        .build();
    private static final CaseDocument ASSISTED_FROM_ORDER = CaseDocumentBuilder.builder()
        .documentName(assistedForm)
        .documentType(JUDGE_FINAL_ORDER)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private JudgeFinalOrderGenerator generator;

    @Test
    void shouldGenerateFreeFormOrder_whenNoneSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenClaimantAndDefendantReferenceNotAddedToCase() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .solicitorReferences(null)
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderOnCourtInitiativeSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .orderOnCourtInitiative(FreeFormOrderValues.builder().onInitiativeSelectionTextArea("test").onInitiativeSelectionDate(
                LocalDate.now()).build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderWithoutNoticeIsSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .orderWithoutNotice(FreeFormOrderValues.builder().withoutNoticeSelectionTextArea("test without notice")
                                    .withoutNoticeSelectionDate(LocalDate.now()).build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedOrder_whenNoneSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenClaimantAndDefendantReferenceNotAddedToCase() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .solicitorReferences(null)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenRecitalsNotSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }
}
