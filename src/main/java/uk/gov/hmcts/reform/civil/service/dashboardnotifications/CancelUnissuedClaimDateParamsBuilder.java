package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class CancelUnissuedClaimDateParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        LocalDate cancelledDate = caseData.getCancelUnissuedClaimDate();
        if (nonNull(cancelledDate)) {
            params.put("cancelUnissuedClaimDateEn", DateUtils.formatDate(cancelledDate));
            params.put("cancelUnissuedClaimDateCy", DateUtils.formatDateInWelsh(cancelledDate, false));
        }
    }
}
