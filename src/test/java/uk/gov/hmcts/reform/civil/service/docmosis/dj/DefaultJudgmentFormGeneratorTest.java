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
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
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
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_DEFENDANT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentFormGeneratorTest {

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
    private FeesService feesService;

    @MockBean
    private InterestCalculator interestCalculator;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldDefaultJudgmentFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC)))
            .thenReturn(new DocmosisDocument(N121_SPEC.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

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

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().outstandingFeeInPounds(BigDecimal.valueOf(1)).build())
            .atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .caseDataLip(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

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

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().outstandingFeeInPounds(BigDecimal.ZERO).build())
            .atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .caseDataLip(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

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

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.NO).build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(1);

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

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .totalClaimAmount(new BigDecimal(2000))
            .fixedCosts(FixedCosts.builder()
                            .claimFixedCosts(YesOrNo.YES)
                            .fixedCostAmount("10000")
                            .build())
            .claimFixedCostsOnEntryDJ(YesOrNo.NO)
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

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .totalClaimAmount(new BigDecimal(2000))
            .fixedCosts(FixedCosts.builder()
                            .claimFixedCosts(YesOrNo.YES)
                            .fixedCostAmount("10000")
                            .build())
            .claimFixedCostsOnEntryDJ(YesOrNo.YES)
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

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN, GENERATE_DJ_FORM_SPEC.name());

        assertThat(caseDocuments.size()).isEqualTo(2);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergent_whenValidDataIsProvided() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_CLAIMANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT1)))
            .thenReturn(CASE_DOCUMENT);
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT2)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .addApplicant2(YesOrNo.YES)
            .applicant2(PartyBuilder.builder().individual().build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());

        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(2);
    }

    @Test
    void shouldGenerateDefendantDocsNonDivergent_whenValidDataIsProvided() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_DEFENDANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT1)))
            .thenReturn(CASE_DOCUMENT);
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT2)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(2);
    }

    @Test
    void shouldGenerateDefendantDocNonDivergent_whenValidDataIsProvidedLip() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_DEFENDANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT1)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .paymentSetDate(LocalDate.now().plusDays(5))
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(1);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergent_whenValidDataIsProvidedLip() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_CLAIMANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT1)))
            .thenReturn(CASE_DOCUMENT);
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT2)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(1);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergent1v2_whenValidDataIsProvidedLip() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_DEFENDANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT1)))
            .thenReturn(CASE_DOCUMENT);
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT2)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        when(feesService.getFeeDataByTotalClaimAmount(new BigDecimal(2000)))
            .thenReturn(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build());

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v2Respondent2LiP()
            .respondent2(PartyBuilder.builder().company().build())
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        List<CaseDocument> caseDocuments = generator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(2);
    }
}
