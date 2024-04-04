package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT;

@Service
public class HearingFeeUnpaidClaimantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT);
    public static final String TASK_ID = "CreateHearingFeeUnpaidDashboardNotificationsForClaimant";

    public HearingFeeUnpaidClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        return YesOrNo.YES.equals(caseData.getTrialReadyApplicant())
            ? SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT.getScenario()
            : SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }
}
