package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
public class Respondent1RepaymentPlanParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {

        if (nonNull(caseData.getRespondent1RepaymentPlan())) {
            getInstalmentAmount(caseData).ifPresent(amount -> params.put("instalmentAmount", amount));
            getInstalmentStartDate(caseData).ifPresent(date -> {
                params.put("instalmentStartDateEn", DateUtils.formatDate(date));
                params.put("instalmentStartDateCy", DateUtils.formatDateInWelsh(date, false));
            });

            params.put("installmentAmount", "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                caseData.getRespondent1RepaymentPlan().getPaymentAmount()).toPlainString()));

            params.put(
                "paymentFrequency",
                caseData.getRespondent1RepaymentPlan().getRepaymentFrequency().getDashboardLabel()
            );
            params.put(
                "paymentFrequencyWelsh",
                caseData.getRespondent1RepaymentPlan().getRepaymentFrequency().getDashboardLabelWelsh()
            );
            getFirstRepaymentDate(caseData).ifPresent(date -> {
                params.put("firstRepaymentDateEn", DateUtils.formatDate(date));
                params.put("firstRepaymentDateCy", DateUtils.formatDateInWelsh(date, false));
            });
        }
    }

    private Optional<LocalDate> getInstalmentStartDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate());
    }

    private Optional<String> getInstalmentAmount(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan())
            .map(RepaymentPlanLRspec::getPaymentAmount)
            .map(MonetaryConversions::penniesToPounds)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

    private Optional<LocalDate> getFirstRepaymentDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan())
            .map(RepaymentPlanLRspec::getFirstRepaymentDate);
    }

}
