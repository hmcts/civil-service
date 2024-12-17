package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_TRIAL_READY_CHECK_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT;

public class TrialReadyCheckDefendantNotificationHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_TRIAL_READY_CHECK_DEFENDANT1);
    public static final String TASK_ID = "TrialReadyCheckDashboardNotificationsForDefendant1";

    public TrialReadyCheckDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        return SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_CHECK_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented())
            && isNull(caseData.getTrialReadyRespondent1())
            && AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack());
    }
}
