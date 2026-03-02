package uk.gov.hmcts.reform.civil.service.docmosis.manualdetermination;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.LIP_MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CLAIMANT_LIP_MANUAL_DETERMINATION_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    ClaimantLipManualDeterminationFormGenerator.class,
    JacksonAutoConfiguration.class
})
class ClaimantLipManualDeterminationFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String REFERENCE_NUMBER = "000MC014";
    private static final String fileName_application = String.format(
            CLAIMANT_LIP_MANUAL_DETERMINATION_PDF.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
            .documentName(fileName_application)
            .documentType(LIP_MANUAL_DETERMINATION)
            .build();
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @Autowired
    private ClaimantLipManualDeterminationFormGenerator generator;
    @MockBean
    private ClaimantResponseUtils claimantResponseUtils;

    @BeforeEach
    void setup() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(CLAIMANT_LIP_MANUAL_DETERMINATION_PDF)))
                .thenReturn(new DocmosisDocument(CLAIMANT_LIP_MANUAL_DETERMINATION_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, LIP_MANUAL_DETERMINATION)))
                .thenReturn(CASE_DOCUMENT);
    }

    @Test
    void shouldGenerateClaimantManualDeterminationDoc_whenValidDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder()
                .legacyCaseReference(REFERENCE_NUMBER)
                .issueDate(LocalDate.now())
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
                .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, LIP_MANUAL_DETERMINATION));
    }

    @Test
    void shouldGenerateClaimantManualDeterminationDoc_whenPayBySetDate() {
        CaseData caseData = CaseDataBuilder.builder()
                .legacyCaseReference(REFERENCE_NUMBER)
                .issueDate(LocalDate.now())
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.SET_DATE)
                .applicant1RequestedPaymentDateForDefendantSpec(new PaymentBySetDate().setPaymentSetDate(LocalDate.now()))
                .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, LIP_MANUAL_DETERMINATION));
    }

    @Test
    void shouldGenerateClaimantManualDeterminationDoc_whenPayByInstallment() {
        CaseData caseData = CaseDataBuilder.builder()
                .legacyCaseReference(REFERENCE_NUMBER)
                .issueDate(LocalDate.now())
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
                .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal.valueOf(100))
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(PaymentFrequencyClaimantResponseLRspec.ONCE_ONE_MONTH)
                .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate.now())
                .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();

        verify(documentManagementService)
                .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, LIP_MANUAL_DETERMINATION));
    }
}
