package uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAmountsCalculator;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT2;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT_WELSH;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_DEFENDANT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    NonDivergentSpecDefaultJudgmentFormGenerator.class,
    NonDivergentSpecDefaultJudgementFormBuilder.class,
    JudgmentAmountsCalculator.class,
    DefaultJudgmentFormBuilder.class,
    JacksonAutoConfiguration.class
})
class NonDivergentSpecDefaultJudgmentFormGeneratorTest {

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
    private NonDivergentSpecDefaultJudgmentFormGenerator nonDivergentSpecDefaultJudgmentFormGenerator;

    @MockBean
    private AssignCategoryId assignCategoryId;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private InterestCalculator interestCalculator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CivilStitchService civilStitchService;

    @Test
    void shouldGenerateClaimantDocsNonDivergent_whenValidDataIsProvided() {
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

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .addApplicant2(YesOrNo.YES)
            .applicant2(PartyBuilder.builder().individual().build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        List<CaseDocument> caseDocuments = nonDivergentSpecDefaultJudgmentFormGenerator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());
        verify(organisationService, times(6)).findOrganisationById(any());
        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(2);
    }

    @Test
    void shouldGenerateDefendantDocsNonDivergent_whenValidDataIsProvided() {
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

        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .build();
        List<CaseDocument> caseDocuments = nonDivergentSpecDefaultJudgmentFormGenerator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());
        verify(organisationService, times(6)).findOrganisationById(any());
        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(2);
    }

    @Test
    void shouldGenerateDefendantDocNonDivergent_whenValidDataIsProvidedLip() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_DEFENDANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_DEFENDANT1)))
            .thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .paymentSetDate(LocalDate.now().plusDays(5))
            .build();
        List<CaseDocument> caseDocuments = nonDivergentSpecDefaultJudgmentFormGenerator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());
        verify(organisationService, times(2)).findOrganisationById(any());
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(1);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergent_whenValidDataIsProvidedLip() {
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

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        List<CaseDocument> caseDocuments = nonDivergentSpecDefaultJudgmentFormGenerator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        verify(organisationService, times(3)).findOrganisationById(any());
        assertThat(caseDocuments).hasSize(1);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergent1v2_whenValidDataIsProvidedLip() {
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

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v2Respondent2LiP()
            .respondent2(PartyBuilder.builder().company().build())
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        List<CaseDocument> caseDocuments = nonDivergentSpecDefaultJudgmentFormGenerator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name());

        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        verify(organisationService, times(5)).findOrganisationById(any());
        assertThat(caseDocuments).hasSize(2);
    }

    @Test
    void shouldGenerateClaimantDocsNonDivergentWelsh_whenValidDataIsProvidedLip() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_CLAIMANT)))
            .thenReturn(new DocmosisDocument(N121_SPEC_CLAIMANT.getDocumentTitle(), bytes));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N121_SPEC_CLAIMANT_WELSH)))
            .thenReturn(new DocmosisDocument(N121_SPEC_CLAIMANT_WELSH.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DEFAULT_JUDGMENT_CLAIMANT1)))
            .thenReturn(CASE_DOCUMENT);

        when(civilStitchService.generateStitchedCaseDocument(anyList(), any(), anyLong(), eq(DocumentType.DEFAULT_JUDGMENT_CLAIMANT1),
                                                             anyString())).thenReturn(CASE_DOCUMENT);

        when(interestCalculator.calculateInterest(any(CaseData.class)))
            .thenReturn(new BigDecimal(10));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
            .totalClaimAmount(new BigDecimal(2000))
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .build();
        List<CaseDocument> caseDocuments = nonDivergentSpecDefaultJudgmentFormGenerator.generateNonDivergentDocs(caseData, BEARER_TOKEN,
                                                                              GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT, "judgments");
        assertThat(caseDocuments).hasSize(1);
    }

}
