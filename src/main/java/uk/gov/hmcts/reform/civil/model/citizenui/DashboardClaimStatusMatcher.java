package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DashboardClaimStatusMatcher {
    private DashboardClaimStatus status;
    private boolean matched;

    public boolean isMatched(){
        return this.matched;
    }
}
