package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.OrderCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

@Service
public class OrderMadeClaimantNotificationHandler extends OrderCallbackHandler {

    private final ObjectMapper objectMapper;

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT,
                                                          CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT);
    public static final String TASK_ID = "GenerateDashboardNotificationFinalOrderClaimant";

    public OrderMadeClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                DashboardNotificationsParamsMapper mapper,
                                                FeatureToggleService featureToggleService, ObjectMapper objectMapper) {
        super(dashboardApiClient, mapper, featureToggleService);
        this.objectMapper = objectMapper;
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
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        HashMap<String, Object> paramsMap = (HashMap<String, Object>) mapper.mapCaseDataToParams(caseData, caseEvent);

        if (isNull(caseData.getRequestForReconsiderationDeadline())
            && isSDOEvent(callbackParams)
            && isEligibleForReconsideration(caseData)
            && featureToggleService.isDashboardServiceEnabled()) {
            caseDataBuilder.requestForReconsiderationDeadline(LocalDate.now().plusDays(7).atTime(16, 0));
        }

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData, callbackParams);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                ScenarioRequestParams.builder().params(paramsMap).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
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
        if (isSDODrawnPreCPRelease()) {
            return SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario();
        }
        return SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }

    private boolean isSDODrawnPreCPRelease() {
        return !getFeatureToggleService().isCaseProgressionEnabled();
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
    }

    private boolean isSDOEvent(CallbackParams callbackParams) {
        return CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT
            .equals(CaseEvent.valueOf(callbackParams.getRequest().getEventId()));
    }
}
