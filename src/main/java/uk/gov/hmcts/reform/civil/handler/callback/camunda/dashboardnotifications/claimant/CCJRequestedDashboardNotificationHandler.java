package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@Service
public class CCJRequestedDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1);
    public static final String TASK_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForApplicant1";

    public CCJRequestedDashboardNotificationHandler(DashboardApiClient dashboardApiClient,
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

        if (claimantSubmitsEitherCCJorDJWithoutSA(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario();
    }

    private boolean claimantSubmitsEitherCCJorDJWithoutSA(CaseData caseData) {
        LocalDate whenWillThisAmountBePaid =
            Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(
                null);
        return (nonNull(whenWillThisAmountBePaid)
            && whenWillThisAmountBePaid.isBefore(LocalDate.now())
            && caseData.isFullAdmitPayImmediatelyClaimSpec())
            || (caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now())
            && CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT == caseData.getCcdState());
    }
}
