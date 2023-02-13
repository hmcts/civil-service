package uk.gov.hmcts.reform.civil.model.citizenui;

import java.util.Arrays;
import java.util.function.Predicate;

public abstract class DashboardClaimStatusFactory<T> {


    public DashboardClaimStatus getDashboardClaimStatus(T claim) {
        return Arrays.stream(DashboardClaimStatus.values())
            .filter(s -> getMatcher(s).test(claim))
            .findFirst()
            .orElse(DashboardClaimStatus.NO_STATUS);
    }

    protected abstract Predicate<T> getMatcher(DashboardClaimStatus status);

}
