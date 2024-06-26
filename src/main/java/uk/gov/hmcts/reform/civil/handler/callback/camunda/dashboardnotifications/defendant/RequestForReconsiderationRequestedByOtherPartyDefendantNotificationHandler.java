package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE;

@Service
public class RequestForReconsiderationRequestedByOtherPartyDefendantNotificationHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_DEFENDANT);
    public static final String TASK_ID = "CreateNotificationRequestForReconsiderationDefendant";

    public RequestForReconsiderationRequestedByOtherPartyDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        return caseData.isRespondent1NotRepresented()
            ? SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT.getScenario()
            : SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE.getScenario();
    }
}
