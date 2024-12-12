package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_NON_IMMEDIATE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentFormGenerator.class,
    NonImmediatePaymentTypeDefaultJudgmentFormBuilder.class,
    JudgmentAmountsCalculator.class,
    DefaultJudgmentFormBuilder.class,
    JacksonAutoConfiguration.class
})
class DefaultJudgmentFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = String.format(N121_SPEC.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(DEFAULT_JUDGMENT)
        .build();
    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private DefaultJudgmentFormGenerator generator;

    @MockBean
    private AssignCategoryId assignCategoryId;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private InterestCalculator interestCalculator;

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments).hasSize(1);
        verify(organisationService).findOrganisationById(any());
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenClaimIssueWithHWF() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().outstandingFeeInPounds(BigDecimal.valueOf(1)).build())
            .atStateNotificationAcknowledged()
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .totalClaimAmount(new BigDecimal(2000))
            .caseDataLip(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments).hasSize(1);
        verify(organisationService).findOrganisationById(any());
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));
    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenClaimIssueWithHWFAndFullRemissionGranted() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().outstandingFeeInPounds(BigDecimal.ZERO).build())
            .atStateNotificationAcknowledged()
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .totalClaimAmount(new BigDecimal(2000))
            .caseDataLip(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments).hasSize(1);
        verify(organisationService).findOrganisationById(any());
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenFixedAmountCostSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.NO).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments).hasSize(1);
        verify(organisationService).findOrganisationById(any());
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenFixedCostsAtClaimIssueOnly() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .totalClaimAmount(new BigDecimal(2000))
            .fixedCosts(FixedCosts.builder()
                .claimFixedCosts(YesOrNo.YES)
                .fixedCostAmount("10000")
                .build())
            .claimFixedCostsOnEntryDJ(YesOrNo.NO)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.NO).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));
    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenFixedCostsAtClaimIssueAndDJ() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .totalClaimAmount(new BigDecimal(2000))
            .fixedCosts(FixedCosts.builder()
                .claimFixedCosts(YesOrNo.YES)
                .fixedCostAmount("10000")
                .build())
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.NO).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT));
    }

    @Test
    void shouldDefaultJudgmentFormGeneratorTwoForms_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments).hasSize(2);
        verify(organisationService, times(2)).findOrganisationById(any());
    }

    @Test
    void shouldUSeNonImmediateDocmosisTemplate() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_NON_IMMEDIATE)))
            .thenReturn(new DocmosisDocument(N121_SPEC_NON_IMMEDIATE.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments).hasSize(2);
        verify(organisationService, times(2)).findOrganisationById(any());
    }
}
