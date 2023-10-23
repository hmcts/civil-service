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
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.account.AccountType;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;

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
    @Autowired
    private SealedClaimLipResponseFormGenerator generator;
    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @Test
    void shouldGenerateDocumentSuccessfully() {
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
    void admitPayImmediate() {
        CaseData caseData = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
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
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(2000))
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim")
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void shouldNotBuildRepaymentPlan_whenRespondent1RepaymentPlanisNull() {
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
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
            .respondent2(company("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
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

}
