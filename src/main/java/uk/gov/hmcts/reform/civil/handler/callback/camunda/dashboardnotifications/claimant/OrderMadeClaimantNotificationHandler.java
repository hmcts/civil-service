package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.apache.ibatis.annotations.Case;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.OrderCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class OrderMadeClaimantNotificationHandler extends OrderCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT);
    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderClaimant";

    public OrderMadeClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
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
    protected String getScenario(CaseData caseData, CallbackParams callbackParams) {
        if (isSDOEvent(callbackParams)
            && isEligibleForReconsideration(caseData)) {
            return SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT.getScenario();
        }
        if (isCarmApplicableCase(caseData)
            && isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData)
            && isSDOEvent(callbackParams)
            && hasTrackChanged(caseData)) {
            return SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM.getScenario();
        }
        return SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
    }

    private boolean isSDOEvent(CallbackParams callbackParams) {
        return CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT
            .equals(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }
}
