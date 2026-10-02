package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class CancelUnissuedClaimSpecDateParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        LocalDate cancelledDate = caseData.getCancelUnissuedClaimSpecDate();
        if (nonNull(cancelledDate)) {
            params.put("cancelUnissuedClaimSpecDateEn", DateUtils.formatDate(cancelledDate));
            params.put("cancelUnissuedClaimSpecDateCy", DateUtils.formatDateInWelsh(cancelledDate, false));
        }
    }
}
