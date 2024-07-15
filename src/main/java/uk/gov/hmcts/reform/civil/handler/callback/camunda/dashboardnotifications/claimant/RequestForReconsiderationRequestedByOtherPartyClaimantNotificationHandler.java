package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CaseProgressionDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_CLAIMANT_RECIPIENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE;

/**
 * CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT is triggered when defendant has requested reconsideration
 * of the SDO; the claimant has to be notified.
 */
@Service
public class RequestForReconsiderationRequestedByOtherPartyClaimantNotificationHandler extends CaseProgressionDashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT);
    public static final String TASK_ID = "CreateNotificationRequestForReconsiderationClaimant";

    public RequestForReconsiderationRequestedByOtherPartyClaimantNotificationHandler(DashboardApiClient dashboardApiClient,
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
        if (caseData.isApplicant1NotRepresented()) {
            if (caseData.getOrderRequestedForReviewClaimant() == YesOrNo.YES) {
                return SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_CLAIMANT_RECIPIENT.getScenario();
            } else {
                return SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_CLAIMANT.getScenario();
            }
        } else {
            if (caseData.getOrderRequestedForReviewDefendant() == YesOrNo.YES
                && caseData.getOrderRequestedForReviewClaimant() == YesOrNo.YES) {
                return SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE.getScenario();
            } else {
                return SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE.getScenario();
            }
        }
    }
}
