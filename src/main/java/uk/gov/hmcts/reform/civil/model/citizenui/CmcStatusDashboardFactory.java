package uk.gov.hmcts.reform.civil.model.citizenui;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.util.function.Predicate;

@Service
public class CmcStatusDashboardFactory extends DashboardClaimStatusFactory<CmcClaim> {

    @Override
    protected Predicate<CmcClaim> getMatcher(DashboardClaimStatus status) {
        return status.getCmcClaimMatcher();
    }

}
