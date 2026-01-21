package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class CreateSdoDashboardTaskContributor extends DashboardTaskContributor {

    public CreateSdoDashboardTaskContributor(CreateSdoClaimantDashboardTask createSdoClaimantTask,
                                             CreateSdoDefendantDashboardTask createSdoDefendantTask,
                                             UploadHearingDocumentsClaimantDashboardTask uploadHearingDocumentsClaimantTask,
                                             UploadHearingDocumentsDefendantDashboardTask uploadHearingDocumentsDefendantTask) {
        super(
            DashboardTaskIds.CREATE_SDO,
            createSdoClaimantTask,
            createSdoDefendantTask,
            uploadHearingDocumentsClaimantTask,
            uploadHearingDocumentsDefendantTask
        );
    }
}
