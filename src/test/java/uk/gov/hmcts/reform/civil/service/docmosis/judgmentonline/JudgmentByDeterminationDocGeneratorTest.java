package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_DETERMINATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_BY_DATE;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IN_INSTALMENTS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_DETERMINATION_DEFENDANT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JudgmentByDeterminationDocGenerator.class,
    JacksonAutoConfiguration.class
})
class JudgmentByDeterminationDocGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final CaseDocument CASE_DOCUMENT_CLAIMANT = CaseDocumentBuilder.builder()
        .documentName("Judgment_by_determination_claimant.pdf")
        .documentType(JUDGMENT_BY_DETERMINATION_CLAIMANT)
        .build();
    private static final CaseDocument CASE_DOCUMENT_DEFENDANT = CaseDocumentBuilder.builder()
        .documentName("Judgment_by_determination_defendant.pdf")
        .documentType(DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT)
        .build();
    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private JudgmentByDeterminationDocGenerator generator;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private AssignCategoryId assignCategoryId;

    @Test
    void shouldDefaultJudgmentFormGeneratorOneFormClaimant() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(new DocmosisDocument(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes,
                                                       JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(CASE_DOCUMENT_CLAIMANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .activeJudgment(JudgmentDetails.builder()
                                .state(ISSUED)
                                .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IN_INSTALMENTS).build())
                                .orderedAmount("150001")
                                .instalmentDetails(JudgmentInstalmentDetails.builder()
                                                       .amount("20001")
                                                       .paymentFrequency(PaymentFrequency.MONTHLY)
                                                       .startDate(LocalDate.now())
                                                       .build())
                                .build())
            .build();
        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN, GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());

        assertThat(caseDocuments).hasSize(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes, JUDGMENT_BY_DETERMINATION_CLAIMANT));
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT_CLAIMANT, "judgments");
    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneFormClaimant_sceanrio2() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(new DocmosisDocument(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes,
                                                       JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(CASE_DOCUMENT_CLAIMANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .activeJudgment(JudgmentDetails.builder()
                                .state(ISSUED)
                                .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_IN_INSTALMENTS).build())
                                .orderedAmount("150001")
                                .instalmentDetails(JudgmentInstalmentDetails.builder()
                                                       .amount("20001")
                                                       .paymentFrequency(PaymentFrequency.MONTHLY)
                                                       .startDate(LocalDate.now())
                                                       .build())
                                .build())
            .build();
        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN, GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());

        assertThat(caseDocuments).hasSize(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes, JUDGMENT_BY_DETERMINATION_CLAIMANT));
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT_CLAIMANT, "judgments");
    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneFormClaimant_sceanrio3() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(new DocmosisDocument(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes,
                                                       JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(CASE_DOCUMENT_CLAIMANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .activeJudgment(JudgmentDetails.builder()
                                .state(ISSUED)
                                .paymentPlan(JudgmentPaymentPlan.builder().type(PAY_BY_DATE).paymentDeadlineDate(LocalDate.now()).build())
                                .orderedAmount("150001")
                                .build())
            .build();
        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN, GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());

        assertThat(caseDocuments).hasSize(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes, JUDGMENT_BY_DETERMINATION_CLAIMANT));
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT_CLAIMANT, "judgments");

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorOneFormClaimantOrg() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(new DocmosisDocument(DocmosisTemplates.JUDGMENT_BY_DETERMINATION_CLAIMANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes,
                                                       JUDGMENT_BY_DETERMINATION_CLAIMANT)))
            .thenReturn(CASE_DOCUMENT_CLAIMANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                        .organisationID("ORG_NAME").build()).build())
            .build();
        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN, GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());

        assertThat(caseDocuments).hasSize(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_claimant.pdf", bytes, JUDGMENT_BY_DETERMINATION_CLAIMANT));
        verify(assignCategoryId)
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT_CLAIMANT, "judgments");

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorDefendant() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(JUDGMENT_BY_DETERMINATION_DEFENDANT)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_DETERMINATION_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_defendant.pdf", bytes,
                                                       DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT)))
            .thenReturn(CASE_DOCUMENT_DEFENDANT);

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .build();

        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN,
                                                                  GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT.name());

        assertThat(caseDocuments).hasSize(2);

        verify(documentManagementService, times(2))
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_defendant.pdf", bytes,
                                                  DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT));
        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT_DEFENDANT, "judgments");

    }

    @Test
    void shouldDefaultJudgmentFormGeneratorDefendantOrg() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(JUDGMENT_BY_DETERMINATION_DEFENDANT)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_DETERMINATION_DEFENDANT.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_defendant.pdf", bytes,
                                                       DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT)))
            .thenReturn(CASE_DOCUMENT_DEFENDANT);

        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("test solicitor")
                                        .contactInformation(List.of(ContactInformation.builder().addressLine1("Test").country(
                                            "Test").build()))
                                        .build()));

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment()
            .toBuilder().applicant1(PartyBuilder.builder().soleTrader().build())
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .respondent2(PartyBuilder.builder().soleTrader().build())
            .respondent1Represented(YesOrNo.NO)
            .respondent1OrganisationPolicy(OrganisationPolicy.builder().organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                                        .organisationID("ORG_NAME").build()).build())
            .build();

        List<CaseDocument> caseDocuments = generator.generateDocs(caseData, BEARER_TOKEN,
                                                                  GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT.name());

        assertThat(caseDocuments).hasSize(2);

        verify(documentManagementService, times(2))
            .uploadDocument(BEARER_TOKEN, new PDF("Judgment_by_determination_defendant.pdf", bytes,
                                                  DocumentType.JUDGMENT_BY_DETERMINATION_DEFENDANT));
        verify(assignCategoryId, times(2))
            .assignCategoryIdToCaseDocument(CASE_DOCUMENT_DEFENDANT, "judgments");

    }
}
