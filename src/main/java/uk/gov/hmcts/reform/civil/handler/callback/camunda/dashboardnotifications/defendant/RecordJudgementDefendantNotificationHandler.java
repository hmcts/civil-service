package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardJudgementOnlineCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_RECORD_JUDGMENT_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGMENT_ONLINE_RECORD_JUDGMENT_DETERMINATION_ISSUED_DEFENDANT;

@Service
public class RecordJudgementDefendantNotificationHandler extends DashboardJudgementOnlineCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_RECORD_JUDGMENT_DEFENDANT);
    public static final String TASK_ID = "GenerateDashboardNotificationRecordJudgmentDefendant";

    public RecordJudgementDefendantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                       DashboardNotificationsParamsMapper mapper,
                                                       FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
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
        return SCENARIO_AAA6_JUDGMENT_ONLINE_RECORD_JUDGMENT_DETERMINATION_ISSUED_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1LiP() && caseData.getJoPaymentPlan().getType().equals(PaymentPlanSelection.PAY_IN_INSTALMENTS);
    }
}
