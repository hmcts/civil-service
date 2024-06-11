package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DELETE_CLAIMANT;

@Service
public class RequestForReconsiderationDeletionClaimantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(DELETE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT);
    public static final String TASK_ID = "DeleteNotificationRequestForReconsiderationClaimant";

    public RequestForReconsiderationDeletionClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        return SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DELETE_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented() && (nonNull(caseData.getOrderRequestedForReviewClaimant()));
    }
}
