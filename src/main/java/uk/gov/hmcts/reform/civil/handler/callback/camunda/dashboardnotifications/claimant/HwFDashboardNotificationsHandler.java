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
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT1_HWF_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GRANTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_FULL_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_INFO_REQUIRED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_INVALID_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_NO_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_PART_REMISSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_HWF_UPDATED;

@Service
public class HwFDashboardNotificationsHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CLAIMANT1_HWF_DASHBOARD_NOTIFICATION);
    public static final String TASK_ID = "Claimant1HwFDashboardNotification";

    public final Map<CaseEvent, String> dashboardScenariosClaimIssue = Map.of(
        NO_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_NO_REMISSION.getScenario(),
        INVALID_HWF_REFERENCE, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INVALID_REF.getScenario(),
        MORE_INFORMATION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_INFO_REQUIRED.getScenario(),
        UPDATE_HELP_WITH_FEE_NUMBER, SCENARIO_AAA6_CLAIM_ISSUE_HWF_UPDATED.getScenario(),
        PARTIAL_REMISSION_HWF_GRANTED, SCENARIO_AAA6_CLAIM_ISSUE_HWF_PART_REMISSION.getScenario(),
        FULL_REMISSION_HWF, SCENARIO_AAA6_CLAIM_ISSUE_HWF_FULL_REMISSION.getScenario()
    );
    private final Map<CaseEvent, String> dashboardScenariosHearingFee = Map.of(
        NO_REMISSION_HWF, SCENARIO_AAA6_HEARING_FEE_HWF_NO_REMISSION.getScenario(),
        INVALID_HWF_REFERENCE, SCENARIO_AAA6_HEARING_FEE_HWF_INVALID_REF.getScenario(),
        MORE_INFORMATION_HWF, SCENARIO_AAA6_HEARING_FEE_HWF_INFO_REQUIRED.getScenario(),
        UPDATE_HELP_WITH_FEE_NUMBER, SCENARIO_AAA6_HEARING_FEE_HWF_UPDATED.getScenario(),
        PARTIAL_REMISSION_HWF_GRANTED, SCENARIO_AAA6_HEARING_FEE_HWF_PART_REMISSION.getScenario(),
        FULL_REMISSION_HWF, SCENARIO_AAA6_HEARING_FEE_HWF_FULL_REMISSION.getScenario()
    );

    public HwFDashboardNotificationsHandler(DashboardApiClient dashboardApiClient,
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
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.getHwFEvent() != null) {
            if (caseData.isHWFTypeClaimIssued()) {
                return dashboardScenariosClaimIssue.get(caseData.getHwFEvent());
            } else if (caseData.isHWFTypeHearing() && featureToggleService.isCaseProgressionEnabled()) {
                return dashboardScenariosHearingFee.get(caseData.getHwFEvent());
            }
        }
        return null;
    }
}
