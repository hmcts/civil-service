package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.translateddocument;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardCaseType;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContributor;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskIds;

@Component
public class TranslatedDocumentDashboardTaskContributor extends DashboardTaskContributor {

    public TranslatedDocumentDashboardTaskContributor(TranslatedDocumentApplicantDashboardTask applicantTask,
                                                      TranslatedDocumentRespondentDashboardTask respondentTask) {
        super(
            DashboardCaseType.GENERAL_APPLICATION,
            DashboardTaskIds.GA_TRANSLATED_DOCUMENT,
            applicantTask,
            respondentTask
        );
    }
}
