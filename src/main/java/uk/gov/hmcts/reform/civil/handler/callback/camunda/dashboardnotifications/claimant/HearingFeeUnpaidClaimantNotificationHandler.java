package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT;

@Service
public class HearingFeeUnpaidClaimantNotificationHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID_FOR_CLAIMANT1);
    public static final String TASK_ID = "CreateHearingFeeUnpaidDashboardNotificationsForClaimant";
    private final DashboardNotificationService dashboardNotificationService;

    public HearingFeeUnpaidClaimantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                       DashboardNotificationsParamsMapper mapper,
                                                       FeatureToggleService featureToggleService,
                                                       DashboardNotificationService dashboardNotificationService) {
        super(dashboardScenariosService, mapper, featureToggleService);
        this.dashboardNotificationService = dashboardNotificationService;
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
        return isNull(caseData.getTrialReadyApplicant()) && SdoHelper.isFastTrack(caseData)
            ? SCENARIO_AAA6_HEARING_FEE_UNPAID_CLAIMANT.getScenario()
            : SCENARIO_AAA6_HEARING_FEE_UNPAID_TRIAL_READY_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        dashboardNotificationService.deleteByReferenceAndCitizenRole(
            String.valueOf(caseData.getCcdCaseReference()),
            "CLAIMANT"
        );
    }
}
