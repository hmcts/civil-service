package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.LipDefenceFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccountSimpleTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.DebtTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.ReasonMoneyTemplateData;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimLipResponseForm implements MappableObject {

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

    private final String claimReferenceNumber;
    private final String claimantReferenceNumber;
    private final String defendantReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate generationDate;
    private final String amountToPay;
    private final String howMuchWasPaid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate paymentDate;
    private final String paymentHow;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec howToPay;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate payBy;
    private final String whyNotPayImmediately;
    private final RepaymentPlanLRspec repaymentPlan;

    private final RespondentResponseTypeSpec responseType;
    // TODO enum, ALREADY_PAID, DISPUTE, COUNTER_CLAIM
    private final String whyReject;
    private final String freeTextWhyReject;
    private final LipDefenceFormParty claimant1;
    private final LipDefenceFormParty defendant1;
    private final LipDefenceFormParty defendant2;
    private final List<EventTemplateData> timelineEventList;
    private final String timelineComments;
    private final List<EvidenceTemplateData> evidenceList;
    private final String evidenceComments;
    private final boolean mediation;
    private final String whereTheyLive;
    private final PartnerAndDependentsLRspec partnerAndDependent;
    private final List<EmployerDetailsLRspec> employerDetails;
    private final Respondent1SelfEmploymentLRspec selfEmployment;
    private List<AccountSimpleTemplateData> bankAccountList;
    private final List<Respondent1CourtOrderDetails> courtOrderDetails;
    private final List<DebtTemplateData> debtList;
    private final List<ReasonMoneyTemplateData> incomeList;
    private final List<ReasonMoneyTemplateData> expenseList;
    private final int childrenMaintenance;

    public String getResponseTypeDisplay() {
        // TODO localization?
        return responseType.getDisplayedValue();
    }

    public boolean isCurrentlyWorking() {
        return (employerDetails != null && !employerDetails.isEmpty())
            || selfEmployment != null && selfEmployment.getAnnualTurnover() != null;
    }

    @JsonIgnore
    public static SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder buildGeneralInformation (final CaseData caseData) {
        return  SealedClaimLipResponseForm.builder()
            .generationDate(LocalDate.now())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimant1(LipDefenceFormParty.from(caseData.getApplicant1()))
            .defendant1(LipDefenceFormParty.from(caseData.getRespondent1()))
            .defendant2(LipDefenceFormParty.from(caseData.getRespondent2()))
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .partnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .selfEmployment(caseData.getSpecDefendant1SelfEmploymentDetails())
            .debtList(mapToDebtList(caseData.getSpecDefendant1Debts()));

    }

    public static SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder addSolicitorDetails (final CaseData caseData, final SealedClaimLipResponseForm form) {
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder = form.toBuilder();
        Optional.ofNullable(caseData.getSolicitorReferences())
            .ifPresent(references ->
                           builder.claimantReferenceNumber(references.getApplicantSolicitor1Reference())
                               .defendantReferenceNumber(references.getRespondentSolicitor1Reference()));
        return builder;
    }

    @JsonIgnore
    private static List<DebtTemplateData> mapToDebtList(Respondent1DebtLRspec debtLRspec) {
        if (debtLRspec != null) {
            List<DebtTemplateData> debtList = new ArrayList<>();
            Optional.ofNullable(debtLRspec.getDebtDetails())
                .map(ElementUtils::unwrapElements)
                .ifPresent(list -> list.stream()
                    .map(SealedClaimLipResponseForm::mapGeneralDebt)
                    .forEach(debtList::add));

            Optional.ofNullable(debtLRspec.getLoanCardDebtDetails())
                .ifPresent(list -> list.stream().map(e -> DebtTemplateData.loanDebtFrom(e.getValue()))
                    .forEach(debtList::add));
            return debtList;
        }
        return Collections.emptyList();
    }

    @JsonIgnore
    private static DebtTemplateData mapGeneralDebt(DebtLRspec debt) {
        return DebtTemplateData.generalDebtFrom(debt);
    }



}
