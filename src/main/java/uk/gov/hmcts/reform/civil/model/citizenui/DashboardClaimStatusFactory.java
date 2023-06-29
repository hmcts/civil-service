package uk.gov.hmcts.reform.civil.model.citizenui;

import org.springframework.stereotype.Service;

@Service
public class DashboardClaimStatusFactory {

    public DashboardClaimStatus getDashboardClaimStatus(Claim claim) {
        DashboardClaimStatus currentStatus = DashboardClaimStatus.NO_STATUS;
        DashboardClaimStatus [] statuses = DashboardClaimStatus.values();

        for(DashboardClaimStatus status : statuses){
            if(status.getClaimMatcher().test(claim)) {
                currentStatus = status;
            }
        }

        return currentStatus;
    }

}
