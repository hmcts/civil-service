package uk.gov.hmcts.reform.civil.model.citizenui;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

@Service
public class CcdClaimStatusDashboardFactory extends DashboardClaimStatusFactory<CaseData> {

    @Override
    protected Predicate<CaseData> getMatcher(DashboardClaimStatus status) {
        return status.getCcdClaimMatcher();
    }

}
