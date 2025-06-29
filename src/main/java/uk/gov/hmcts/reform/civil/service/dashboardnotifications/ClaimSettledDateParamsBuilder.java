package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

@Component
public class ClaimSettledDateParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        getClaimSettleDate(caseData).ifPresent(date -> {
            params.put("claimSettledObjectionsDeadline",
                date.plusDays(CLAIM_SETTLED_OBJECTION_DEADLINE_DAYS).atTime(END_OF_DAY));
            params.put("claimSettledDateEn", DateUtils.formatDate(date));
            params.put("claimSettledDateCy", DateUtils.formatDateInWelsh(date, false));
        });
    }

    private Optional<LocalDate> getClaimSettleDate(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData))
            .map(RespondToClaim::getWhenWasThisAmountPaid);
    }
}
