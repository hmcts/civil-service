package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DISCONTINUE_NOTICE_OF_DISCONTINUE_ISSUED_DEFENDANT;

@Service
public class DefendantNotifyDiscontinuanceDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE);
    public static final String TASK_ID = "CreateDefendantDashboardNotificationsForDiscontinuance";

    public DefendantNotifyDiscontinuanceDashboardNotificationHandler(DashboardApiClient dashboardApiClient,
                                                                     DashboardNotificationsParamsMapper mapper,
                                                                     FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            return SCENARIO_AAA6_DISCONTINUE_NOTICE_OF_DISCONTINUE_ISSUED_DEFENDANT.getScenario();
        }
        return null;
    }

}
