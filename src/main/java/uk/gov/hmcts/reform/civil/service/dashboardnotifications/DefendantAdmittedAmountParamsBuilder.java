package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;

import java.math.BigDecimal;
import java.util.HashMap;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;

@Component
@AllArgsConstructor
public class DefendantAdmittedAmountParamsBuilder extends DashboardNotificationsParamsBuilder {

    private final ClaimantResponseUtils claimantResponseUtils;

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        BigDecimal defendantAdmittedAmount = claimantResponseUtils.getDefendantAdmittedAmount(caseData, true);
        if (nonNull(defendantAdmittedAmount)) {
            params.put(
                "defendantAdmittedAmount",
                "£" + this.removeDoubleZeros(formatAmount(defendantAdmittedAmount)));
        }
    }
}
