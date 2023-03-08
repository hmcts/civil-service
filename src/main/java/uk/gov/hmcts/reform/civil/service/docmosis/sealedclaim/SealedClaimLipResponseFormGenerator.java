package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.LoanCardDebtLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.LipDefenceFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccountSimpleTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.DebtTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.ReasonMoneyTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;

@Service
@RequiredArgsConstructor
public class SealedClaimLipResponseFormGenerator implements TemplateDataGenerator<SealedClaimLipResponseForm> {

    private static final List<Pair<IncomeTypeLRspec, String>> INCOME_TYPE_ORDER = List.of(
        Pair.of(IncomeTypeLRspec.JOB, "Income from your job"),
        Pair.of(IncomeTypeLRspec.UNIVERSAL_CREDIT, "Universal Credit"),
        Pair.of(IncomeTypeLRspec.JOBSEEKER_ALLOWANCE_INCOME, "Jobseeker's Allowance (income based)"),
        Pair.of(IncomeTypeLRspec.JOBSEEKER_ALLOWANCE_CONTRIBUTION, "Jobseeker's Allowance (contribution based)"),
        Pair.of(IncomeTypeLRspec.INCOME_SUPPORT, "Income support"),
        Pair.of(IncomeTypeLRspec.WORKING_TAX_CREDIT, "Working Tax Credit"),
        Pair.of(IncomeTypeLRspec.CHILD_TAX, "Child Tax Credit"),
        Pair.of(IncomeTypeLRspec.CHILD_BENEFIT, "Child Benefit"),
        Pair.of(IncomeTypeLRspec.COUNCIL_TAX_SUPPORT, "Council Tax Support"),
        Pair.of(IncomeTypeLRspec.PENSION, "Pension"),
        Pair.of(IncomeTypeLRspec.OTHER, "Other: ")
    );
    private static final Comparator<RecurringIncomeLRspec> INCOME_COMPARATOR = Comparator
        .comparing(e1 -> {
            for (int i = 0; i < INCOME_TYPE_ORDER.size(); i++) {
                if (INCOME_TYPE_ORDER.get(i).getKey() == e1.getType()) {
                    return i;
                }
            }
            return -1;
        });
    private static final List<Pair<ExpenseTypeLRspec, String>> EXPENSE_TYPE_ORDER = List.of(
        Pair.of(ExpenseTypeLRspec.MORTGAGE, "Mortgage"),
        Pair.of(ExpenseTypeLRspec.RENT, "Rent"),
        Pair.of(ExpenseTypeLRspec.COUNCIL_TAX, "Council Tax"),
        Pair.of(ExpenseTypeLRspec.GAS, "Gas"),
        Pair.of(ExpenseTypeLRspec.ELECTRICITY, "Electric"),
        Pair.of(ExpenseTypeLRspec.WATER, "Water"),
        Pair.of(ExpenseTypeLRspec.TRAVEL, "Travel (work or school)"),
        Pair.of(ExpenseTypeLRspec.SCHOOL, "School costs"),
        Pair.of(ExpenseTypeLRspec.FOOD, "Food and housekeeping"),
        Pair.of(ExpenseTypeLRspec.TV, "TV and broadband"),
        Pair.of(ExpenseTypeLRspec.HIRE_PURCHASE, "Hire purchase"),
        Pair.of(ExpenseTypeLRspec.MOBILE_PHONE, "Mobile phone"),
        Pair.of(ExpenseTypeLRspec.MAINTENANCE, "Maintenance payments"),
        Pair.of(ExpenseTypeLRspec.OTHER, "Other")
    );
    private static final Comparator<RecurringExpenseLRspec> EXPENSE_COMPARATOR = Comparator
        .comparing(e1 -> {
            for (int i = 0; i < EXPENSE_TYPE_ORDER.size(); i++) {
                if (EXPENSE_TYPE_ORDER.get(i).getKey() == e1.getType()) {
                    return i;
                }
            }
            return -1;
        });

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;

    private static final Set<Party.Type> INDIVIDUAL_TYPES = EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER);
    // TODO look for existing constant
    private static final long DAYS_TO_PAY_IMMEDIATELY = 5;

    @Override
    public SealedClaimLipResponseForm getTemplateData(CaseData caseData) {
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder = SealedClaimLipResponseForm.builder()
            .generationDate(LocalDate.now())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimant1(getPartyData(caseData.getApplicant1()))
            .defendant1(getPartyData(caseData.getRespondent1()))
            .defendant2(getPartyData(caseData.getRespondent2()))
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .partnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .selfEmployment(caseData.getSpecDefendant1SelfEmploymentDetails())
            .debtList(mapToDebtList(caseData.getSpecDefendant1Debts()));

        Optional.ofNullable(caseData.getSolicitorReferences())
            .ifPresent(references ->
                           builder.claimantReferenceNumber(references.getApplicantSolicitor1Reference())
                               .defendantReferenceNumber(references.getRespondentSolicitor1Reference()));

        Optional.ofNullable(caseData.getResponseClaimAdmitPartEmployer())
            .map(Respondent1EmployerDetailsLRspec::getEmployerDetails)
            .map(ElementUtils::unwrapElements)
            .ifPresent(builder::employerDetails);

        if (caseData.getRespondent1DQ() != null) {
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1BankAccountList())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream().map(AccountSimpleTemplateData::new).collect(Collectors.toList()))
                .ifPresent(builder::bankAccountList);
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1DQRecurringIncome())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .sorted(INCOME_COMPARATOR)
                    .map(item ->
                             ReasonMoneyTemplateData.builder()
                                 .type(item.getType() == IncomeTypeLRspec.OTHER
                                           ? "Other: " + item.getTypeOtherDetails()
                                           : INCOME_TYPE_ORDER.stream()
                                     .filter(p -> item.getType() == p.getKey())
                                     .findFirst().map(Pair::getValue).orElse("Other"))
                                 .amountPounds(item.getAmount())
                                 .build()
                    ).collect(Collectors.toList()))
                .ifPresent(builder::incomeList);
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1DQRecurringExpenses())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .sorted(EXPENSE_COMPARATOR)
                    .map(item ->
                             ReasonMoneyTemplateData.builder()
                                 .type(item.getType() == ExpenseTypeLRspec.OTHER
                                           ? "Other: " + item.getTypeOtherDetails()
                                           : EXPENSE_TYPE_ORDER.stream()
                                     .filter(p -> item.getType() == p.getKey())
                                     .findFirst().map(Pair::getValue).orElse("Other"))
                                 .amountPounds(item.getAmount())
                                 .build()).collect(Collectors.toList()))
                .ifPresent(builder::expenseList);
        }

        Optional.ofNullable(caseData.getRespondent1CourtOrderDetails())
            .map(ElementUtils::unwrapElements)
            .ifPresent(builder::courtOrderDetails);

        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
            Optional.ofNullable(caseData.getRespondent1DQ())
                .map(Respondent1DQ::getRespondent1DQHomeDetails)
                .map(HomeDetails::getType)
                .ifPresent(type -> {
                    switch (type) {
                        case OWNED_HOME:
                            builder.whereTheyLive("Home they own or pay a mortgage on");
                            break;
                        case PRIVATE_RENTAL:
                            builder.whereTheyLive("Private rental");
                            break;
                        case ASSOCIATION_HOME:
                            builder.whereTheyLive("Council or housing association home");
                            break;
                        case JOINTLY_OWNED_HOME:
                            builder.whereTheyLive("Jointly-owned home (or jointly mortgaged home)");
                            break;
                        case OTHER:
                            builder.whereTheyLive("Other");
                            break;
                    }
                });
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION:
                    if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                        == RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY) {
                        builder.payBy(LocalDate.now().plusDays(DAYS_TO_PAY_IMMEDIATELY))
                            .amountToPay(caseData.getTotalClaimAmount() + "");
                    } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                        == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
                        builder.repaymentPlan(caseData.getRespondent1RepaymentPlan())
                            .payBy(caseData.getRespondent1RepaymentPlan()
                                       .finalPaymentBy(caseData.getTotalClaimAmount()))
                            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
                    } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                        == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
                        builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
                            .amountToPay(caseData.getTotalClaimAmount() + "")
                            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
                    }
                    break;
                case PART_ADMISSION:
                    builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
                        .timelineEventList(caseData.getSpecResponseTimelineOfEvents().stream()
                                               .map(event ->
                                                        EventTemplateData.builder()
                                                            .date(event.getValue().getTimelineDate())
                                                            .explanation(event.getValue().getTimelineDescription())
                                                            .build()).collect(Collectors.toList()));
                    if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.YES) {
                        RespondToClaim respondToClaim = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                            .orElse(caseData.getRespondToClaim());
                        builder.whyReject("ALREADY_PAID")
                            .howMuchWasPaid(respondToClaim.getHowMuchWasPaid() + "")
                            .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
                            .paymentHow(respondToClaim.getHowWasThisAmountPaid() == PaymentMethod.OTHER
                                            ? respondToClaim.getHowWasThisAmountPaidOther()
                                            : respondToClaim.getHowWasThisAmountPaid()
                                .getHumanFriendly());
                    } else {
                        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                            == RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY) {
                            builder.payBy(LocalDate.now().plusDays(DAYS_TO_PAY_IMMEDIATELY))
                                .amountToPay(caseData.getRespondToAdmittedClaimOwingAmount() + "");
                        } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
                            builder.repaymentPlan(caseData.getRespondent1RepaymentPlan())
                                .payBy(caseData.getRespondent1RepaymentPlan()
                                           .finalPaymentBy(caseData.getRespondToAdmittedClaimOwingAmount()))
                                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
                        } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                            == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
                            builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
                                .amountToPay(caseData.getRespondToAdmittedClaimOwingAmount() + "")
                                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
                        }
                    }
                    break;
            }
        }

        return builder.build();
    }

    private List<DebtTemplateData> mapToDebtList(Respondent1DebtLRspec debtLRspec) {
        if (debtLRspec != null) {
            List<DebtTemplateData> debtList = new ArrayList<>();
            Optional.ofNullable(debtLRspec.getDebtDetails())
                .map(ElementUtils::unwrapElements)
                .ifPresent(list -> list.stream()
                    .map(this::mapGeneralDebt)
                    .forEach(debtList::add));

            Optional.ofNullable(debtLRspec.getLoanCardDebtDetails())
                .ifPresent(list -> list.stream().map(e -> mapLoanDebt(e.getValue()))
                    .forEach(debtList::add));
            return debtList;
        }
        return Collections.emptyList();
    }

    private DebtTemplateData mapLoanDebt(LoanCardDebtLRspec debt) {
        return DebtTemplateData.builder()
            .debtOwedTo(debt.getLoanCardDebtDetail())
            .paidPerMonth(debt.getMonthlyPayment())
            .poundsOwed(debt.getTotalOwed())
            .build();
    }

    private DebtTemplateData mapGeneralDebt(DebtLRspec debt) {
        DebtTemplateData.DebtTemplateDataBuilder builder = DebtTemplateData.builder()
            .debtOwedTo(debt.getDebtType().getLabel());
        switch (debt.getPaymentFrequency()) {
            case ONCE_ONE_MONTH:
            case ONCE_FOUR_WEEKS:
                builder.paidPerMonth(debt.getPaymentAmount());
                break;
            case ONCE_THREE_WEEKS:
                builder.paidPerMonth(debt.getPaymentAmount()
                                         .multiply(BigDecimal.valueOf(4))
                                         .divide(BigDecimal.valueOf(3), RoundingMode.CEILING));
                break;
            case ONCE_TWO_WEEKS:
                builder.paidPerMonth(debt.getPaymentAmount().multiply(BigDecimal.valueOf(2)));
                break;
            case ONCE_ONE_WEEK:
                builder.paidPerMonth(debt.getPaymentAmount().multiply(BigDecimal.valueOf(4)));
                break;
        }
        return builder.build();
    }

    private LipDefenceFormParty getPartyData(Party party) {
        if (party == null) {
            return null;
        }
        LipDefenceFormParty.LipDefenceFormPartyBuilder builder = LipDefenceFormParty.builder()
            .name(party.getPartyName())
            .phone(party.getPartyPhone())
            .email(party.getPartyEmail())
            .primaryAddress(party.getPrimaryAddress());
        if (INDIVIDUAL_TYPES.contains(party.getType())) {
            builder.isIndividual(true);
            Stream.of(party.getIndividualDateOfBirth(), party.getSoleTraderDateOfBirth())
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::dateOfBirth);
        } else {
            builder.isIndividual(false);
        }
        return builder.build();
    }

    public CaseDocument generate(CaseData caseData, String authorization) {
        SealedClaimLipResponseForm templateData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            DEFENDANT_RESPONSE_LIP_SPEC
        );
        String fileName = String.format(
            DEFENDANT_RESPONSE_LIP_SPEC.getDocumentTitle(),
            caseData.getLegacyCaseReference()
        );

        return documentManagementService.uploadDocument(
            authorization,
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }
}
