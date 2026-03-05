package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Objects;

@Service
@AllArgsConstructor
public class CoscDashboardHelper {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public boolean isMarkedPaidInFull(CaseData data) {
        return (Objects.nonNull(data.getActiveJudgment()) && (data.getActiveJudgment().getFullyPaymentMadeDate() != null));
    }

    public CaseData getParentCaseData(GeneralApplicationCaseData caseData) {
        return caseDetailsConverter.toCaseData(coreCaseDataService.getCase(Long.valueOf(caseData.getParentCaseReference())));
    }
}
