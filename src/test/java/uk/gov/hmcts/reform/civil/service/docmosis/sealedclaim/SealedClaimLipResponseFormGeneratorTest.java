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
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import jakarta.validation.constraints.NotNull;
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
    @MockBean
    private InterestCalculator interestCalculator;
    @Captor
    ArgumentCaptor<PDF> uploadDocumentArgumentCaptor;

    @Test
    void shouldGenerateDocumentSuccessfully() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        CaseData caseData = commonData().build();

        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = new CaseDocument().setDocumentName(fileName);
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        //Given
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );
        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = new CaseDocument().setDocumentName(fileName);
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void admitPayInstalments() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(company("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setFirstRepaymentDate(LocalDate.now().plusDays(5))
                                          .setPaymentAmount(BigDecimal.valueOf(200))
                                          .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                          )
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = financialDetails(builder).build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void admitPayByDate() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.now().plusMonths(3))
                                               )
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = financialDetails(builder).build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void admitPayByDate_Lip() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(
                RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = financialDetails(builder).build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void partAdmitPayImmediate() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void shouldNotBuildRepaymentPlan_whenRespondent1RepaymentPlanisNull() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
        assertThat(templateData.getCommonDetails().repaymentPlan()).isNull();
    }

    @Test
    void partAdmitPayInstalments() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
            .respondent1RepaymentPlan(new RepaymentPlanLRspec()
                                          .setFirstRepaymentDate(LocalDate.now().plusDays(5))
                                          .setPaymentAmount(BigDecimal.valueOf(200))
                                          .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                          )
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
        assertThat(templateData.getCommonDetails().repaymentPlan()).isNotNull();
    }

    @Test
    void partAdmitPayByDate() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                               .setWhenWillThisAmountBePaid(LocalDate.now().plusMonths(3))
                                               )
            .responseToClaimAdmitPartWhyNotPayLRspec("Reason not to pay immediately");

        CaseData caseData = timeline(financialDetails(builder))
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void partAdmitAlreadyPaid() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .specDefenceAdmittedRequired(YesOrNo.YES)
            .respondToClaim(new RespondToClaim()
                                .setHowMuchWasPaid(BigDecimal.valueOf(10_000))
                                .setHowWasThisAmountPaid(PaymentMethod.CHEQUE)
                                .setWhenWasThisAmountPaid(LocalDate.now().minusMonths(1))
                                )
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim");

        CaseData caseData = timeline(builder)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void fullDefenseAlreadyPaid() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(individual("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .respondToClaim(new RespondToClaim()
                                .setHowMuchWasPaid(BigDecimal.valueOf(10_000))
                                .setHowWasThisAmountPaid(PaymentMethod.CHEQUE)
                                .setWhenWasThisAmountPaid(LocalDate.now().minusMonths(1))
                                )
            .detailsOfWhyDoesYouDisputeTheClaim("Reason to dispute the claim");

        CaseData caseData = timeline(builder)
            .build();

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(caseData);
        Assertions.assertEquals(LocalDate.now(), templateData.getGenerationDate());
    }

    @Test
    void fullDefenseDispute() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = new CaseDocument().setDocumentName(fileName);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any())).willReturn(
            docmosisDocument);
        given(documentManagementService.uploadDocument(anyString(), any(PDF.class))).willReturn(caseDocument);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = commonData()
            .respondent1(company("B"))
            .respondent2(individual("C"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            )
            .build();
        generator.getTemplateData(caseData);
        //When
        CaseDocument result = generator.generate(caseData, AUTHORIZATION);
        //Then
        assertThat(result).isEqualTo(caseDocument);
    }

    @Test
    void shouldGenerateDocumentSuccessfullyForPartAdmit() {
        //Given
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        String fileName = "someName";
        DocmosisDocument docmosisDocument = mock(DocmosisDocument.class);
        byte[] bytes = {};
        given(docmosisDocument.getBytes()).willReturn(bytes);
        CaseDocument caseDocument = new CaseDocument().setDocumentName(fileName);
        given(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), any())).willReturn(
            docmosisDocument);
        given(documentManagementService.uploadDocument(anyString(), any(PDF.class))).willReturn(caseDocument);
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
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            ).build();
        generator.getTemplateData(caseData);
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
            new TimelineOfEvents(new TimelineOfEventDetails(LocalDate.now().minusMonths(7), "Event 1"), null),
            new TimelineOfEvents(new TimelineOfEventDetails(LocalDate.now().minusMonths(5), "Event 2"), null)
        ));
    }

    private static CaseData.CaseDataBuilder<?, ?> financialDetails(CaseData.CaseDataBuilder<?, ?> builder) {
        return builder.respondent1DQ(new Respondent1DQ()
                                         .setRespondent1DQHomeDetails(
                                             new HomeDetails(HomeTypeOptionLRspec.OWNED_HOME, null))
                                         .setRespondent1BankAccountList(ElementUtils.wrapElements(
                                             account(AccountType.CURRENT, YesOrNo.YES, BigDecimal.valueOf(2000)),
                                             account(AccountType.ISA, YesOrNo.NO, BigDecimal.valueOf(500))
                                         ))
                                         .setRespondent1DQRecurringIncome(ElementUtils.wrapElements(
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
                                         .setRespondent1DQRecurringExpenses(ElementUtils.wrapElements(
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
                                         )))
            .specDefendant1Debts(new Respondent1DebtLRspec()
                                     .setHasLoanCardDebt(YesOrNo.YES)
                                     .setLoanCardDebtDetails(ElementUtils.wrapElements(
                                         new LoanCardDebtLRspec().setLoanCardDebtDetail("Card 1")
                                             .setTotalOwed(BigDecimal.valueOf(500))
                                             .setMonthlyPayment(BigDecimal.valueOf(50)),
                                         new LoanCardDebtLRspec().setLoanCardDebtDetail("Card 2")
                                             .setTotalOwed(BigDecimal.valueOf(1500))
                                             .setMonthlyPayment(BigDecimal.valueOf(200))
                                     
                                     ))
                                     .setDebtDetails(ElementUtils.wrapElements(
                                         new DebtLRspec()
                                             .setDebtType(DebtTypeLRspec.GAS)
                                             .setPaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                             .setPaymentAmount(BigDecimal.valueOf(30)),
                                         new DebtLRspec()
                                             .setDebtType(DebtTypeLRspec.MAINTENANCE_PAYMENTS)
                                             .setPaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                             .setPaymentAmount(BigDecimal.valueOf(60))
                                     ))
                                     )
            .respondent1PartnerAndDependent(new PartnerAndDependentsLRspec()
                                                .setHaveAnyChildrenRequired(YesOrNo.YES)
                                                .setHowManyChildrenByAgeGroup(new ChildrenByAgeGroupLRspec()
                                                                               .setNumberOfUnderEleven("1")
                                                                               .setNumberOfElevenToFifteen("2")
                                                                               .setNumberOfSixteenToNineteen("0"))
                                                .setSupportedAnyoneFinancialRequired(YesOrNo.YES)
                                                .setSupportPeopleNumber("3")
                                                .setSupportPeopleDetails("Supported people details")
                                                )

            .specDefendant1SelfEmploymentDetails(new Respondent1SelfEmploymentLRspec()
                                                     .setJobTitle("job title")
                                                     .setAnnualTurnover(BigDecimal.valueOf(30000))
                                                     .setIsBehindOnTaxPayment(YesOrNo.YES)
                                                     .setAmountOwed(BigDecimal.valueOf(10000))
                                                     .setReason("reason to owe tax")
                                                     )
            .responseClaimAdmitPartEmployer(new Respondent1EmployerDetailsLRspec(ElementUtils.wrapElements(
                                                    new EmployerDetailsLRspec()
                                                        .setEmployerName("Employer 1")
                                                        .setJobTitle("Job title 1"),
                                                    new EmployerDetailsLRspec()
                                                        .setEmployerName("Employer 2")
                                                        .setJobTitle("Job title 2")
                                                )));
    }

    private CaseData.CaseDataBuilder<?, ?> commonData() {
        return CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234567890123456L)
            .solicitorReferences(new SolicitorReferences()
                                     .setApplicantSolicitor1Reference("claimant reference")
                                     .setRespondentSolicitor1Reference("defendant reference")
                                     )
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
            .primaryAddress(address(suffix))
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
            .primaryAddress(address(suffix))
            .build();
    }

    private Address address(String suffix) {
        Address address = new Address();
        address.setPostCode("postCode " + suffix);
        address.setAddressLine1("line 1 " + suffix);
        address.setAddressLine2("line 2 " + suffix);
        address.setAddressLine3("line 3 " + suffix);
        address.setPostTown("town " + suffix);
        address.setCounty("county " + suffix);
        address.setCountry("country " + suffix);
        return address;
    }

    @Test
    void checkMediationDefaultFields() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YesOrNo.YES)
                                                                      .setIsMediationEmailCorrect(YesOrNo.YES)
                                                                      .setIsMediationPhoneCorrect(YesOrNo.YES)
                                                                      .setHasUnavailabilityNextThreeMonths(YesOrNo.NO)))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationAlternativeFields() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationContactPerson("Jake")
                                                                      .setIsMediationEmailCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationEmail("test@gmail.com")
                                                                      .setIsMediationPhoneCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationTelephone("23454656")
                                                                      .setHasUnavailabilityNextThreeMonths(YesOrNo.NO)))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("Jake", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("test@gmail.com", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("23454656", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationUnAvailabilityDateRangeFields() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationContactPerson("Jake")
                                                                      .setIsMediationEmailCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationEmail("test@gmail.com")
                                                                      .setIsMediationPhoneCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationTelephone("23454656")
                                                                      .setHasUnavailabilityNextThreeMonths(YesOrNo.YES)
                                                                      .setUnavailableDatesForMediation(def1UnavailabilityDates)))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationContactPerson("Jake")
                                                                      .setIsMediationEmailCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationEmail("test@gmail.com")
                                                                      .setIsMediationPhoneCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationTelephone("23454656")
                                                                      .setHasUnavailabilityNextThreeMonths(YesOrNo.YES)
                                                                      .setUnavailableDatesForMediation(def1UnavailabilityDates)))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(new CaseDataLiP())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationCaseDataLipResponsesAreNull() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationNullFieldsOfRespondent1MediationLipResponseFields() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData.CaseDataBuilder<?, ?> builder = commonData()
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(new CaseDataLiP().setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
            );

        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        Assertions.assertEquals("company B", templateData.getDefendant1MediationCompanyName());
        Assertions.assertEquals("email B", templateData.getDefendant1MediationEmail());
        Assertions.assertEquals("phone B", templateData.getDefendant1MediationContactNumber());
    }

    @Test
    void checkMediationIndividualNameField() {
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
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
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationEmailCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationEmail("test@gmail.com")
                                                                      .setIsMediationPhoneCorrect(YesOrNo.NO)
                                                                      .setAlternativeMediationTelephone("23454656")
                                                                      .setHasUnavailabilityNextThreeMonths(YesOrNo.YES)
                                                                      .setUnavailableDatesForMediation(def1UnavailabilityDates)))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    
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
        when(interestCalculator.claimAmountPlusInterestToDate(any())).thenReturn(new BigDecimal(2000));
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        //When
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
            .applicant1(company("A"))
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .respondent1(company("B"))
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .caseDataLiP(new CaseDataLiP().setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()))
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1LiPStatementOfTruth(new StatementOfTruth().setName("Test").setRole("Test"))
            .respondToClaimAdmitPartLRspec(
                new RespondToClaimAdmitPartLRspec()
                    .setWhenWillThisAmountBePaid(whenWillPay)
                    )
            .totalClaimAmount(BigDecimal.valueOf(10_000))
            .uiStatementOfTruth(new StatementOfTruth().setName("Test").setRole("Test"));

        //Then
        SealedClaimLipResponseForm templateData = generator
            .getTemplateData(builder.build());
        assertThat(templateData.getUiStatementOfTruth().getName()).isEqualTo("Test");
        assertThat(templateData.getUiStatementOfTruth().getRole()).isEqualTo("Test");
    }
}
