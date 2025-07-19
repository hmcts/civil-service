package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;

@Component
public class AdmissionPaidAmountParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        getAlreadyPaidAmount(caseData).ifPresent(amount -> params.put("admissionPaidAmount", amount));
    }

    private Optional<String> getAlreadyPaidAmount(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData)).map(RespondToClaim::getHowMuchWasPaid).map(
                MonetaryConversions::penniesToPounds).map(
                BigDecimal::stripTrailingZeros)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

}
