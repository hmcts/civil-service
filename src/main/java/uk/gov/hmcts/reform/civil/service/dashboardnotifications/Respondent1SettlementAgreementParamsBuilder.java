package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;

@Component
public class Respondent1SettlementAgreementParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        getRespondToSettlementAgreementDeadline(caseData).ifPresent(date -> {
            params.put("respondent1SettlementAgreementDeadline", date.atTime(END_OF_DAY));
            params.put("respondent1SettlementAgreementDeadlineEn", DateUtils.formatDate(date));
            params.put("respondent1SettlementAgreementDeadlineCy", DateUtils.formatDateInWelsh(date, false));
            params.put("claimantSettlementAgreementEn", getClaimantRepaymentPlanDecision(caseData));
            params.put("claimantSettlementAgreementCy", getClaimantRepaymentPlanDecisionCy(caseData));
        });
    }

    private Optional<LocalDate> getRespondToSettlementAgreementDeadline(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RespondToSettlementAgreementDeadline())
            .map(LocalDateTime::toLocalDate);
    }
}
