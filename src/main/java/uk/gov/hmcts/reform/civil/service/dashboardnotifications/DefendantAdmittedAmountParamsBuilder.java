package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;

import java.util.HashMap;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;

@Component
@AllArgsConstructor
public class DefendantAdmittedAmountParamsBuilder extends DashboardNotificationsParamsBuilder {

    private final ClaimantResponseUtils claimantResponseUtils;

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(claimantResponseUtils.getDefendantAdmittedAmount(caseData))) {
            params.put(
                "defendantAdmittedAmount",
                "£" + this.removeDoubleZeros(formatAmount(claimantResponseUtils.getDefendantAdmittedAmount(caseData)))
            );
        }
    }
}
