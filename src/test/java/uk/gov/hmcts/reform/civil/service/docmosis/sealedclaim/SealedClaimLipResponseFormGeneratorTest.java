package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DebtTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChildrenByAgeGroupLRspec;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.account.AccountType;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SealedClaimLipResponseFormGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class SealedClaimLipResponseFormGeneratorTest {

    private static final String AUTHORIZATION = "authorization";
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private DeadlineExtensionCalculatorService deadlineCalculatorService;
    @Autowired
    private SealedClaimLipResponseFormGenerator generator;

    @MockBean
    private FeatureToggleService featureToggleService;
    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @Test
    void shouldGenerateDocumentSuccessfully() {

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        //Given
        CaseData caseData = commonData().build();

        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = CaseDocument.builder().documentName(fileName).build();
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any())).willReturn(
            docmosisDocument);
        given(documentManagementService.uploadDocument(anyString(), any(PDF.class))).willReturn(caseDocument);
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        //When
        CaseDocument result = generator.generate(caseData, AUTHORIZATION);
        //Then
        assertThat(result).isEqualTo(caseDocument);
        verify(documentGeneratorService).generateDocmosisDocument(templateData, DEFENDANT_RESPONSE_LIP_SPEC);
        verify(documentManagementService).uploadDocument(
            eq(AUTHORIZATION),
            uploadDocumentArgumentCaptor.capture()
        );
        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DEFENDANT_DEFENCE);
    }

    @Test
    void shouldGenerateDocumentSuccessfully_AfterCarmEnabled() {

        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        //Given
        CaseData caseData = commonData().build();
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );
        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = CaseDocument.builder().documentName(fileName).build();
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any())).willReturn(
            docmosisDocument);
        given(documentManagementService.uploadDocument(anyString(), any(PDF.class))).willReturn(caseDocument);
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        //When
        CaseDocument result = generator.generate(builder.build(), AUTHORIZATION);
        //Then
        assertThat(result).isEqualTo(caseDocument);
        verify(documentGeneratorService).generateDocmosisDocument(templateData, DEFENDANT_RESPONSE_LIP_SPEC);
        verify(documentManagementService).uploadDocument(
            eq(AUTHORIZATION),
            uploadDocumentArgumentCaptor.capture()
        );
        PDF document = uploadDocumentArgumentCaptor.getValue();
        assertThat(document.getDocumentType()).isEqualTo(DEFENDANT_DEFENCE);
    }

    @Test
    void admitPayImmediate() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void admitPayInstalments() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(company("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                          .firstRepaymentDate(LocalDate.now().plusDays(5))
                                          .paymentAmount(BigDecimal.valueOf(200))
                                          .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                          .build())
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = financialDetails(builder).build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void admitPayByDate() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                               .whenWillThisAmountBePaid(LocalDate.now().plusMonths(3))
                                               .build())
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = financialDetails(builder).build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void partAdmitPayImmediate() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ResponseDate(now())
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(2000))
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim")
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void shouldNotBuildRepaymentPlan_whenRespondent1RepaymentPlanisNull() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent1ResponseDate(now())
            .respondent2(company("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(2000))
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim")
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");
        CaseData caseData = timeline(financialDetails(builder))
            .build();
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        assertThat(templateData.getCommonDetails().getRepaymentPlan()).isNull();
    }

    @Test
    void partAdmitPayInstalments() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent1ResponseDate(now())
            .respondent2(company("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent1ResponseDate(LocalDateTime.now())
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(2000))
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim")
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                          .firstRepaymentDate(LocalDate.now().plusDays(5))
                                          .paymentAmount(BigDecimal.valueOf(200))
                                          .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                          .build())
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
        assertThat(templateData.getCommonDetails().getRepaymentPlan()).isNotNull();
    }

    @Test
    public void partAdmitPayByDate() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent1ResponseDate(now())
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(2000))
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim")
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                               .whenWillThisAmountBePaid(LocalDate.now().plusMonths(3))
                                               .build())
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void partAdmitAlreadyPaid() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.YES)
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(BigDecimal.valueOf(10_000))
                                .howWasThisAmountPaid(PaymentMethod.CHEQUE)
                                .whenWasThisAmountPaid(LocalDate.now().minusMonths(1))
                                .build())
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim");

        CaseData caseData = timeline(builder)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void fullDefenseAlreadyPaid() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(BigDecimal.valueOf(10_000))
                                .howWasThisAmountPaid(PaymentMethod.CHEQUE)
                                .whenWasThisAmountPaid(LocalDate.now().minusMonths(1))
                                .build())
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim");

        CaseData caseData = timeline(builder)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void fullDefenseDispute() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM)
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim");

        CaseData caseData = timeline(builder)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void counterClaim() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);

        CaseData caseData = timeline(builder)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void shouldGenerateDocumentSuccessfullyForFullAdmit() {
        //Given
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            )
            .build();
        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = CaseDocument.builder().documentName(fileName).build();
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any())).willReturn(
            docmosisDocument);
        given(documentManagementService.uploadDocument(anyString(), any(PDF.class))).willReturn(caseDocument);
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        //When
        CaseDocument result = generator.generate(caseData, AUTHORIZATION);
        //Then
        assertThat(result).isEqualTo(caseDocument);
    }

    @Test
    void shouldGenerateDocumentSuccessfullyForPartAdmit() {
        //Given
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = commonData()
            .respondent1(company("B"))
            .respondent1ResponseDate(now())
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(2000))
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim")
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            ).build();
        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = CaseDocument.builder().documentName(fileName).build();
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any())).willReturn(
            docmosisDocument);
        given(documentManagementService.uploadDocument(anyString(), any(PDF.class))).willReturn(caseDocument);
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        //When
        CaseDocument result = generator.generate(caseData, AUTHORIZATION);
        //Then
        assertThat(result).isEqualTo(caseDocument);
    }

    private static AccountSimple account(@NotNull AccountType type, @NotNull YesOrNo joint, @NotNull BigDecimal balance) {
        AccountSimple a = new AccountSimple();
        a.setAccountType(type);
        a.setJointAccount(joint);
        a.setBalance(balance);
        return a;
    }

    private static CaseData.CaseDataBuilder<?, ?> timeline(CaseData.CaseDataBuilder<?, ?> builder) {
        return builder.specResponseTimelineOfEvents(List.of(
            TimelineOfEvents.builder()
                .value(TimelineOfEventDetails.builder()
                           .timelineDate(LocalDate.now().minusMonths(7))
                           .timelineDescription("Event 1")
                           .build())
                .build(),
            TimelineOfEvents.builder()
                .value(TimelineOfEventDetails.builder()
                           .timelineDate(LocalDate.now().minusMonths(5))
                           .timelineDescription("Event 2")
                           .build())
                .build()
        ));
    }

    private static CaseData.CaseDataBuilder<?, ?> financialDetails(CaseData.CaseDataBuilder<?, ?> builder) {
        return builder.respondent1DQ(Respondent1DQ.builder()
                                         .respondent1DQHomeDetails(
                                             HomeDetails.builder().type(HomeTypeOptionLRspec.OWNED_HOME).build())
                                         .respondent1BankAccountList(ElementUtils.wrapElements(
                                             account(AccountType.CURRENT, YesOrNo.YES, BigDecimal.valueOf(2000)),
                                             account(AccountType.ISA, YesOrNo.NO, BigDecimal.valueOf(500))
                                         ))
                                         .respondent1DQRecurringIncome(ElementUtils.wrapElements(
                                             new RecurringIncomeLRspec(
                                                 IncomeTypeLRspec.INCOME_SUPPORT,
                                                 null,
                                                 BigDecimal.valueOf(100),
                                                 PaymentFrequencyLRspec.ONCE_ONE_MONTH
                                             ),
                                             new RecurringIncomeLRspec(
                                                 IncomeTypeLRspec.OTHER,
                                                 "Details of other income",
                                                 BigDecimal.valueOf(400),
                                                 PaymentFrequencyLRspec.ONCE_ONE_MONTH
                                             )
                                         ))
                                         .respondent1DQRecurringExpenses(ElementUtils.wrapElements(
                                             new RecurringExpenseLRspec(
                                                 ExpenseTypeLRspec.COUNCIL_TAX,
                                                 null,
                                                 BigDecimal.valueOf(300),
                                                 PaymentFrequencyLRspec.ONCE_ONE_MONTH
                                             ),
                                             new RecurringExpenseLRspec(
                                                 ExpenseTypeLRspec.OTHER,
                                                 "Details of other expenses",
                                                 BigDecimal.valueOf(400),
                                                 PaymentFrequencyLRspec.ONCE_ONE_MONTH
                                             )
                                         ))
                                         .build())
            .specDefendant1Debts(Respondent1DebtLRspec.builder()
                                     .hasLoanCardDebt(YesOrNo.YES)
                                     .loanCardDebtDetails(ElementUtils.wrapElements(
                                         LoanCardDebtLRspec.builder()
                                             .loanCardDebtDetail("Card 1")
                                             .totalOwed(BigDecimal.valueOf(500))
                                             .monthlyPayment(BigDecimal.valueOf(50))
                                             .build(),
                                         LoanCardDebtLRspec.builder()
                                             .loanCardDebtDetail("Card 2")
                                             .totalOwed(BigDecimal.valueOf(1500))
                                             .monthlyPayment(BigDecimal.valueOf(200))
                                             .build()
                                     ))
                                     .debtDetails(ElementUtils.wrapElements(
                                         DebtLRspec.builder()
                                             .debtType(DebtTypeLRspec.GAS)
                                             .paymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                             .paymentAmount(BigDecimal.valueOf(30))
                                             .build(),
                                         DebtLRspec.builder()
                                             .debtType(DebtTypeLRspec.MAINTENANCE_PAYMENTS)
                                             .paymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                             .paymentAmount(BigDecimal.valueOf(60))
                                             .build()
                                     ))
                                     .build())
            .respondent1PartnerAndDependent(PartnerAndDependentsLRspec.builder()
                                                .haveAnyChildrenRequired(YesOrNo.YES)
                                                .howManyChildrenByAgeGroup(ChildrenByAgeGroupLRspec.builder()
                                                                               .numberOfUnderEleven("1")
                                                                               .numberOfElevenToFifteen("2")
                                                                               .numberOfSixteenToNineteen("0")
                                                                               .build())
                                                .supportedAnyoneFinancialRequired(YesOrNo.YES)
                                                .supportPeopleNumber("3")
                                                .supportPeopleDetails("Supported people details")
                                                .build())

            .specDefendant1SelfEmploymentDetails(Respondent1SelfEmploymentLRspec.builder()
                                                     .jobTitle("job title")
                                                     .annualTurnover(BigDecimal.valueOf(30000))
                                                     .isBehindOnTaxPayment(YesOrNo.YES)
                                                     .amountOwed(BigDecimal.valueOf(10000))
                                                     .reason("reason to owe tax")
                                                     .build())
            .responseClaimAdmitPartEmployer(Respondent1EmployerDetailsLRspec.builder()
                                                .employerDetails(ElementUtils.wrapElements(
                                                    EmployerDetailsLRspec.builder()
                                                        .employerName("Employer 1")
                                                        .jobTitle("Job title 1")
                                                        .build(),
                                                    EmployerDetailsLRspec.builder()
                                                        .employerName("Employer 2")
                                                        .jobTitle("Job title 2")
                                                        .build()
                                                ))
                                                .build());
    }

    private CaseData.CaseDataBuilder<?, ?> commonData() {
        return CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234567890123456L)
            .solicitorReferences(SolicitorReferences.builder()
                                     .applicantSolicitor1Reference("claimant reference")
                                     .respondentSolicitor1Reference("defendant reference")
                                     .build())
            .applicant1(company("A"))
            .totalClaimAmount(BigDecimal.valueOf(10_000));
    }

    private Party company(String suffix) {
        return Party.builder()
            .type(Party.Type.COMPANY)
            .companyName("company " + suffix)
            .partyPhone("phone " + suffix)
            .partyEmail("email " + suffix)
            .partyName("company " + suffix)
            .primaryAddress(Address.builder()
                                .postCode("postCode " + suffix)
                                .addressLine1("line 1 " + suffix)
                                .addressLine2("line 2 " + suffix)
                                .addressLine3("line 3 " + suffix)
                                .postTown("town " + suffix)
                                .county("county " + suffix)
                                .country("country " + suffix)
                                .build())
            .build();
    }

    private Party individual(String suffix) {
        return Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("Name " + suffix)
            .individualLastName("Surname " + suffix)
            .individualDateOfBirth(LocalDate.of(1956, 10, 2))
            .partyPhone("phone " + suffix)
            .partyName("Name Surname" + suffix)
            .partyEmail("email " + suffix)
            .primaryAddress(Address.builder()
                                .postCode("postCode " + suffix)
                                .addressLine1("line 1 " + suffix)
                                .addressLine2("line 2 " + suffix)
                                .addressLine3("line 3 " + suffix)
                                .postTown("town " + suffix)
                                .county("county " + suffix)
                                .country("country " + suffix)
                                .build())
            .build();
    }

    @Test
    void checkMediationDefaultFields() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationContactNameCorrect(YesOrNo.YES)
                                                                      .isMediationEmailCorrect(YesOrNo.YES)
                                                                      .isMediationPhoneCorrect(YesOrNo.YES)
                                                                      .hasUnavailabilityNextThreeMonths(YesOrNo.NO)
                                                                      .build()).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationAlternativeFields() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationContactNameCorrect(YesOrNo.NO)
                                                                      .alternativeMediationContactPerson("Jake")
                                                                      .isMediationEmailCorrect(YesOrNo.NO)
                                                                      .alternativeMediationEmail("test@gmail.com")
                                                                      .isMediationPhoneCorrect(YesOrNo.NO)
                                                                      .alternativeMediationTelephone("23454656")
                                                                      .hasUnavailabilityNextThreeMonths(YesOrNo.NO)
                                                                      .build()).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("Jake", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("test@gmail.com", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("23454656", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationUnAvailabilityDateRangeFields() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        List<Element<UnavailableDate>> def1UnavailabilityDates = new ArrayList<>();
        def1UnavailabilityDates.add(element(UnavailableDate.builder()
                                                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                                                 .toDate(LocalDate.now().plusDays(5))
                                                 .fromDate(LocalDate.now()).build()));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationContactNameCorrect(YesOrNo.NO)
                                                                      .alternativeMediationContactPerson("Jake")
                                                                      .isMediationEmailCorrect(YesOrNo.NO)
                                                                      .alternativeMediationEmail("test@gmail.com")
                                                                      .isMediationPhoneCorrect(YesOrNo.NO)
                                                                      .alternativeMediationTelephone("23454656")
                                                                      .hasUnavailabilityNextThreeMonths(YesOrNo.YES)
                                                                      .unavailableDatesForMediation(def1UnavailabilityDates)
                                                                      .build()).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("Jake", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("test@gmail.com", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("23454656", templateData.getDefendant1MediationContactNumber());
        Assertions.assertEquals(LocalDate.now(), templateData.getDefendant1UnavailableDatesList().get(0).getValue().getFromDate());
        Assertions.assertEquals(LocalDate.now().plusDays(5), templateData.getDefendant1UnavailableDatesList().get(0).getValue().getToDate());
    }

    @Test
    void checkMediationUnAvailabilitySingleDateFields() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        List<Element<UnavailableDate>> def1UnavailabilityDates = new ArrayList<>();
        def1UnavailabilityDates.add(element(UnavailableDate.builder()
                                                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                .date(LocalDate.now())
                                                .fromDate(LocalDate.now()).build()));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationContactNameCorrect(YesOrNo.NO)
                                                                      .alternativeMediationContactPerson("Jake")
                                                                      .isMediationEmailCorrect(YesOrNo.NO)
                                                                      .alternativeMediationEmail("test@gmail.com")
                                                                      .isMediationPhoneCorrect(YesOrNo.NO)
                                                                      .alternativeMediationTelephone("23454656")
                                                                      .hasUnavailabilityNextThreeMonths(YesOrNo.YES)
                                                                      .unavailableDatesForMediation(def1UnavailabilityDates)
                                                                      .build()).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("Jake", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("test@gmail.com", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("23454656", templateData.getDefendant1MediationContactNumber());
        Assertions.assertEquals(LocalDate.now(), templateData.getDefendant1UnavailableDatesList().get(0).getValue().getFromDate());
        Assertions.assertEquals(LocalDate.now(), templateData.getDefendant1UnavailableDatesList().get(0).getValue().getDate());
    }

    @Test
    void checkMediationRespondent1LipResponseFieldsAreNull() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder()
                             .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationCaseDataLipResponsesAreNull() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationNullFieldsOfRespondent1MediationLipResponseFields() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder().respondent1MediationLiPResponseCarm(MediationLiPCarm.builder().build())
                             .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationIndividualNameField() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        List<Element<UnavailableDate>> def1UnavailabilityDates = new ArrayList<>();
        def1UnavailabilityDates.add(element(UnavailableDate.builder()
                                                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                                .date(LocalDate.now())
                                                .fromDate(LocalDate.now()).build()));
        def1UnavailabilityDates.add(element(UnavailableDate.builder()
                                                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                                                .toDate(LocalDate.now().plusDays(5))
                                                .date(LocalDate.now())
                                                .fromDate(LocalDate.now()).build()));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationEmailCorrect(YesOrNo.NO)
                                                                      .alternativeMediationEmail("test@gmail.com")
                                                                      .isMediationPhoneCorrect(YesOrNo.NO)
                                                                      .alternativeMediationTelephone("23454656")
                                                                      .hasUnavailabilityNextThreeMonths(YesOrNo.YES)
                                                                      .unavailableDatesForMediation(def1UnavailabilityDates)
                                                                      .build()).build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build()
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("Name B Surname B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("test@gmail.com", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("23454656", templateData.getDefendant1MediationContactNumber());
        Assertions.assertEquals(2, templateData.getDefendant1UnavailableDatesList().size());
    }


    @Test
    void shouldGenerateDocumentWithStatementOfTruthSuccessfully_DefendantLipTypeCompanyOROrg() {
        //Given
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        //When
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
            .applicant1(company("A"))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(CaseDataLiP.builder().respondent1MediationLiPResponseCarm(MediationLiPCarm.builder().build())
                             .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                RespondToClaimAdmitPartLRspec.builder()
                    .whenWillThisAmountBePaid(whenWillPay)
                    .build())
            .totalClaimAmount(BigDecimal.valueOf(10_000))
            .uiStatementOfTruth(StatementOfTruth.builder().name("Test").role("Test").build());

        //Then
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        assertThat(templateData.getUiStatementOfTruth().getName()).isEqualTo("Test");
        assertThat(templateData.getUiStatementOfTruth().getRole()).isEqualTo("Test");
    }
}
