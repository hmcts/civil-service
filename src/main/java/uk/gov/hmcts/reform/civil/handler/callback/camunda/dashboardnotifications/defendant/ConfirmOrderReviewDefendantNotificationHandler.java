package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ConfirmOrderReviewNotificationHandler;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT;

@Service
public class ConfirmOrderReviewDefendantNotificationHandler extends ConfirmOrderReviewNotificationHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT);
    private static final String TASK_ID = "UpdateTaskListConfirmOrderReviewDefendant";
    private static final String ROLE = "DEFENDANT";

    public ConfirmOrderReviewDefendantNotificationHandler(DashboardApiClient dashboardApiClient,
                                                          DashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService,
                                                          ObjectMapper objectMapper) {
        super(dashboardApiClient, mapper, featureToggleService, objectMapper, ROLE, TASK_ID, EVENTS);
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented()
            && YesOrNo.YES.equals(caseData.getIsFinalOrder());
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (isOrderMadeFastTrackTrialNotResponded(caseData)) {
            return SCENARIO_AAA6_UPDATE_TASK_LIST_TRIAL_READY_FINALS_ORDERS_DEFENDANT.getScenario();
        }
        return SCENARIO_AAA6_UPDATE_DASHBOARD_DEFENDANT_TASK_LIST_UPLOAD_DOCUMENTS_FINAL_ORDERS.getScenario();
    }

    private boolean isOrderMadeFastTrackTrialNotResponded(CaseData caseData) {
        return SdoHelper.isFastTrack(caseData) && isNull(caseData.getTrialReadyRespondent1());
    }
}
