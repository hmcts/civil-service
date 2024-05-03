package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.*;

@Service
public class GenerateDashboardNotificationHwfHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1);
    public static final String TASK_ID = "GenerateDashboardNotificationClaimIssueHwfClaimant1";

    public GenerateDashboardNotificationHwfHandler(DashboardApiClient dashboardApiClient,
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
    public String getExtraScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_CLAIM_ISSUE_FAST_TRACK_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordExtraScenario(CaseData caseData) {
        AllocatedTrack allocatedTrack = AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null);

        return FAST_CLAIM.equals(allocatedTrack) && caseData.isApplicantNotRepresented();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CLAIM_ISSUE_HWF_REQUESTED.getScenario();
    }
}
