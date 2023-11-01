package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.COUNCIL_TAX;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.ELECTRICITY;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.FOOD;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.GAS;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.HIRE_PURCHASE;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.MAINTENANCE;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.MOBILE_PHONE;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.MORTGAGE;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.RENT;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.SCHOOL;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.TRAVEL;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.TV;
import static uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec.WATER;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.CHILD_BENEFIT;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.CHILD_TAX;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.COUNCIL_TAX_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.INCOME_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.JOB;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.JOBSEEKER_ALLOWANCE_CONTRIBUTION;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.JOBSEEKER_ALLOWANCE_INCOME;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.PENSION;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.UNIVERSAL_CREDIT;
import static uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec.WORKING_TAX_CREDIT;

@Builder
@Data
public class ReasonMoneyTemplateData {

    @JsonIgnore
    private static final Map<IncomeTypeLRspec, String> INCOME_TYPE_LIP_RESPONSE = Map.of(
        JOB, "Income from your job",
        UNIVERSAL_CREDIT, "Universal Credit",
        JOBSEEKER_ALLOWANCE_INCOME, "Jobseeker's Allowance (income based)",
        JOBSEEKER_ALLOWANCE_CONTRIBUTION, "Jobseeker's Allowance (contribution based)",
        INCOME_SUPPORT, "Income support",
        WORKING_TAX_CREDIT, "Working Tax Credit",
        CHILD_TAX, "Child Tax Credit",
        CHILD_BENEFIT, "Child Benefit",
        COUNCIL_TAX_SUPPORT, "Council Tax Support",
        PENSION, "Pension"
    );

    @JsonIgnore
    private static final Map<ExpenseTypeLRspec, String> EXPENSE_TYPE_LIP_RESPONSE = Map.ofEntries(
        entry(MORTGAGE, "Mortgage"),
        entry(RENT, "Rent"), entry(COUNCIL_TAX, "Council Tax"),
        entry(GAS, "Gas"),
        entry(ELECTRICITY, "Electric"),
        entry(WATER, "Water"),
        entry(TRAVEL, "Travel (work or school)"),
        entry(SCHOOL, "School costs"),
        entry(FOOD, "Food and housekeeping"),
        entry(TV, "TV and broadband"),
        entry(HIRE_PURCHASE, "Hire purchase"),
        entry(MOBILE_PHONE, "Mobile phone"),
        entry(MAINTENANCE, "Maintenance payments")
    );

    private String type;
    private BigDecimal amountPounds;

    @JsonIgnore
    public static ReasonMoneyTemplateData toReasonMoneyTemplateData(RecurringIncomeLRspec item) {
        return ReasonMoneyTemplateData.builder()
            .type(item.getType() == IncomeTypeLRspec.OTHER
                      ? "Other: " + item.getTypeOtherDetails()
                      : INCOME_TYPE_LIP_RESPONSE.get(item.getType()))
            .amountPounds(MonetaryConversions.penniesToPounds(item.getAmount()).setScale(2))
            .build();
    }

    @JsonIgnore
    public static ReasonMoneyTemplateData toReasonMoneyTemplateData(RecurringExpenseLRspec item) {
        return ReasonMoneyTemplateData.builder()
            .type(item.getType() == ExpenseTypeLRspec.OTHER
                      ? "Other: " + item.getTypeOtherDetails()
                      : EXPENSE_TYPE_LIP_RESPONSE.get(item.getType()))
            .amountPounds(MonetaryConversions.penniesToPounds(item.getAmount()).setScale(2))
            .build();
    }
}
