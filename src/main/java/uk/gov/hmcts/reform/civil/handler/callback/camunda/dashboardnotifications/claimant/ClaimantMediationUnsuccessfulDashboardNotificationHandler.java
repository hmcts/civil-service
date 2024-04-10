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

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL_APPLICANT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_GENERIC;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class ClaimantMediationUnsuccessfulDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_UNSUCCESSFUL_APPLICANT);
    public static final String TASK_ID = "GenerateDashboardNotificationClaimantMediationUnsuccessful";

    public ClaimantMediationUnsuccessfulDashboardNotificationHandler(DashboardApiClient dashboardApiClient,
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
        return getFeatureToggleService().isCarmEnabledForCase(caseData)
            && !isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData);
    }

    @Override
    public String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario();
    }

    private Boolean isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
    }
}
