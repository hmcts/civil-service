package uk.gov.hmcts.reform.civil.model.citizenui;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DashboardClaimStatusFactory {

    public DashboardClaimStatus getDashboardClaimStatus(Claim claim) {
        DashboardClaimStatus dashboardClaimStatus = Arrays.stream(DashboardClaimStatus.values())
            .filter(status -> status.getClaimMatcher().test(claim))
            .findFirst()
            .orElse(DashboardClaimStatus.NO_STATUS);

        if (DashboardClaimStatus.STATUS_OVERRIDES.containsKey(dashboardClaimStatus)) {
            return Arrays.stream(DashboardClaimStatus.values())
                .filter(status -> status != dashboardClaimStatus)
                .filter(status -> status.getClaimMatcher().test(claim))
                .findFirst()
                .filter(status -> DashboardClaimStatus.STATUS_OVERRIDES.get(dashboardClaimStatus).contains(status))
                .orElse(dashboardClaimStatus);
        }
        return dashboardClaimStatus;
    }
}
