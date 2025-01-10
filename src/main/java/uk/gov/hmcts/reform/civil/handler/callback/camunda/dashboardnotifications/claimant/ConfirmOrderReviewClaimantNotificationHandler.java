package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseEventsDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT;

@Service
public class ConfirmOrderReviewClaimantNotificationHandler extends CaseEventsDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT);
    public static final String TASK_ID = "UpdateTaskListConfirmOrderReviewClaimant";
    public static final String GA = "Applications";

    public ConfirmOrderReviewClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                         DashboardNotificationsParamsMapper mapper,
                                                         FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public String getScenario(CaseData caseData) {
        return null;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented())
            && YesOrNo.YES.equals(caseData.getIsFinalOrder());
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public CallbackResponse configureDashboardScenario(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        if (shouldRecordScenario(caseData)) {
            dashboardApiClient.makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                "CLAIMANT",
                GA,
                authToken
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
