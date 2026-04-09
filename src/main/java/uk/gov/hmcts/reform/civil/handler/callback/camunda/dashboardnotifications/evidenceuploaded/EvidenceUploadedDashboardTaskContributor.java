package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.evidenceuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class EvidenceUploadedDashboardTaskContributor extends DashboardTaskContributor {

    public EvidenceUploadedDashboardTaskContributor(EvidenceUploadedClaimantDashboardTask claimantTask,
                                                    EvidenceUploadedDefendantDashboardTask defendantTask) {
        super(
            DashboardTaskIds.EVIDENCE_UPLOADED,
            claimantTask,
            defendantTask
        );
    }
}
