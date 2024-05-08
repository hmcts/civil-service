package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class MediationUnsuccessfulDashboardNotificationDefendantHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL);
    public static final String TASK_ID = "GenerateDashboardNotificationDefendantMediationUnsuccessful";

    public MediationUnsuccessfulDashboardNotificationDefendantHandler(DashboardApiClient dashboardApiClient,
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
        if (getFeatureToggleService().isCarmEnabledForCase(caseData)) {
            if (isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(caseData)) {
                return SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE.getScenario();
            } else {
                return SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario();
            }
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    private Boolean isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
    }
}
