package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT;

@Service
public class OrderMadeClaimantNotificationHandler extends DashboardCallbackHandler {

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

    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        HashMap<String, Object> paramsMap = getMapWithDocumentInfo(caseData, caseEvent);

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String scenario = getScenario(caseData);
        if (!Strings.isNullOrEmpty(scenario) && shouldRecordScenario(caseData)) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                ScenarioRequestParams.builder().params(paramsMap).build()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }

    private HashMap<String, Object> getMapWithDocumentInfo(CaseData caseData, CaseEvent caseEvent) {
        HashMap<String, Object> params = new HashMap<>();

        switch (caseEvent) {
            case CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT -> {
                params.put("orderDocument", caseData.getFinalOrderDocumentCollection()
                    .get(0).getValue().getDocumentLink().getDocumentBinaryUrl());
                return params;
            }
            case CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT -> {
                params.put("orderDocument", caseData.getOrderSDODocumentDJCollection()
                    .get(0).getValue().getDocumentLink().getDocumentBinaryUrl());
                return params;
            }
            default -> {
                if (caseData.getSDODocument().isPresent()) {
                    params.put(
                        "orderDocument",
                        caseData.getSDODocument().get().getValue().getDocumentLink().getDocumentBinaryUrl()
                    );
                }
                return params;
            }
        }
    }
}
