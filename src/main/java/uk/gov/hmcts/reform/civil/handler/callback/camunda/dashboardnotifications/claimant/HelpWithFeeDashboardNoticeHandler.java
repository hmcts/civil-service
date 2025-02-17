package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_HELP_WITH_FEE_APPLIED_CLAIMANT;

@Service
public class HelpWithFeeDashboardNoticeHandler  extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections
        .singletonList(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HELP_FEE_IN_REVIEW_CLAIMANT);

    private static final String TASK_ID = "CreateHelpWithFeeInReviewNotificationForClaimant";

    public HelpWithFeeDashboardNoticeHandler(DashboardScenariosService dashboardScenariosService,
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
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_HEARING_FEE_HELP_WITH_FEE_APPLIED_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }
}
