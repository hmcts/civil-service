package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT;

@Service
public class CCJRequestedDashboardNotificationDefendantHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1);
    public static final String TASK_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForRespondent1";

    public CCJRequestedDashboardNotificationDefendantHandler(DashboardScenariosService dashboardScenariosService,
                                                             DashboardNotificationsParamsMapper mapper,
                                                             FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
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

        /* Assumption has been made that claimant will raise CCJ only if settlement agreement is broken
         * 1. Defendant fails to respond to the SA by deadline
         * 2. Defendant rejects the SA
         * 3. Defendant accepts the SA and then breaks the terms of the agreement
         *
         */
        if (respondentRejectedSettlementAgreementOrNotRespondedByDeadline(caseData)) {
            return SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_NO_DEF_RESPONSE_DEFENDANT
                .getScenario();
        } else if (respondentAcceptedSettlementAgreementButMissedPayment(caseData)) {
            return SCENARIO_AAA6_CCJ_CLAIMANT_ACCEPT_OR_REJECT_PLAN_SETTLEMENT_REQUESTED_DEF_PAYMENT_MISSED_DEFENDANT.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_DEFENDANT.getScenario();
    }

    private boolean respondentRejectedSettlementAgreementOrNotRespondedByDeadline(CaseData caseData) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && (caseData.isRespondentRejectedSettlementAgreement()
            || (!caseData.isRespondentRespondedToSettlementAgreement() && caseData.isSettlementAgreementDeadlineExpired()));
    }

    private boolean respondentAcceptedSettlementAgreementButMissedPayment(CaseData caseData) {
        return caseData.hasApplicant1SignedSettlementAgreement()
            && caseData.isRespondentSignedSettlementAgreement();
    }
}
