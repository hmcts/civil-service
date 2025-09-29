package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.MarkPaidConsentList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class SettleClaimPaidInFullDateParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getMarkPaidConsent()) && MarkPaidConsentList.YES == caseData.getMarkPaidConsent()) {
            params.put("settleClaimPaidInFullDateEn", DateUtils.formatDate(LocalDate.now()));
            params.put("settleClaimPaidInFullDateCy", DateUtils.formatDateInWelsh(LocalDate.now(), false));
        }
    }
}
