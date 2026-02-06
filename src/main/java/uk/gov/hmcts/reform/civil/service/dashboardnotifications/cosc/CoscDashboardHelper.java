package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;

@Service
public class CoscDashboardHelper {

    public boolean isMarkedPaidInFull(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment()) && (data.getActiveJudgment().getFullyPaymentMadeDate() != null));
    }
}
