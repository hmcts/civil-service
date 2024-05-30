package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.OrderCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_SDO_DRAWN_PRE_CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class OrderMadeDefendantNotificationHandler extends OrderCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT);
    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderDefendant";

    public OrderMadeDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                 DashboardNotificationsParamsMapper mapper,
                                                 FeatureToggleService featureToggleService,
                                                 ObjectMapper objectMapper) {
        super(dashboardApiClient, mapper, featureToggleService, objectMapper);
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
    protected String getScenario(CaseData caseData, CallbackParams callbackParams) {
        if (isSDOEvent(callbackParams)
            && isEligibleForReconsideration(caseData)) {
            return SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DEFENDANT.getScenario();
        }
        if (isCarmApplicableCase(caseData)
            && isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(caseData)
            && isSDOEvent(callbackParams)
            && hasTrackChanged(caseData)) {
            return SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_CARM.getScenario();
        }
        if (isSDODrawnPreCPRelease()) {
            return SCENARIO_AAA6_DEFENDANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario();
        }
        return SCENARIO_AAA6_CP_ORDER_MADE_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
    }

    private boolean isSDOEvent(CallbackParams callbackParams) {
        return CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT
            .equals(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }

    private boolean isSDODrawnPreCPRelease() {
        return !getFeatureToggleService().isCaseProgressionEnabled();
    }
}
