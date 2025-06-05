package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

@Component
public class BundleRestitchedDateParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        Optional<LocalDateTime> latestBundleCreatedOn = getLatestBundleCreatedOn(caseData);
        latestBundleCreatedOn.ifPresent(date -> {
            params.put("bundleRestitchedDateEn", DateUtils.formatDate(date));
            params.put("bundleRestitchedDateCy", DateUtils.formatDateInWelsh(date.toLocalDate(), false));
        });
    }

    private Optional<LocalDateTime> getLatestBundleCreatedOn(CaseData caseData) {
        return Optional.ofNullable(caseData.getCaseBundles())
            .map(bundles -> bundles.stream()
                .map(IdValue::getValue)
                .map(Bundle::getCreatedOn)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder()))
            .orElse(Optional.empty());
    }
}
