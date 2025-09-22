package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class CertofScParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getCertOfSC())) {
            params.put("coscFullPaymentDateEn", DateUtils.formatDate(caseData.getCertOfSC().getDefendantFinalPaymentDate()));
            params.put("coscFullPaymentDateCy", DateUtils.formatDateInWelsh(caseData.getCertOfSC().getDefendantFinalPaymentDate(), false));
            params.put("coscNotificationDateEn", DateUtils.formatDate(LocalDate.now()));
            params.put("coscNotificationDateCy", DateUtils.formatDateInWelsh(LocalDate.now(), false));
        }
    }
}
