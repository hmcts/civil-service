package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;

@Service
@RequiredArgsConstructor
public class DefendantNocDashboardNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC);
    public static final String TASK_ID = "CreateClaimantDashboardNotificationDefendantNoc";
    private static final String CLAIMANT_ROLE = "CLAIMANT";

    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationService dashboardNotificationService;
    private final DashboardNotificationsParamsMapper mapper;
    private final FeatureToggleService featureToggleService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::configureScenarioForDefendantNoc);
    }

    private CallbackResponse configureScenarioForDefendantNoc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        ScenarioRequestParams params = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build();
        if (!featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            dashboardNotificationService.deleteByReferenceAndCitizenRole(
                caseData.getCcdCaseReference().toString(),
                CLAIMANT_ROLE
            );

            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        } else {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        }

        if (isNull(caseData.getTrialReadyApplicant()) && SdoHelper.isFastTrack(caseData)) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        }
        PaymentDetails hearingFeePaymentDetails = caseData.getHearingFeePaymentDetails();

        boolean isHearingFeeNotPaid = (isNull(hearingFeePaymentDetails) || hearingFeePaymentDetails.getStatus() != PaymentStatus.SUCCESS) && !caseData.isHWFTypeHearing();
        boolean isFeePaymentOutcomeNotDone = caseData.isHWFTypeHearing() && isNull(caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee())
            && (isNull(hearingFeePaymentDetails) || hearingFeePaymentDetails.getStatus() != PaymentStatus.SUCCESS);

        if (isHearingFeeNotPaid || isFeePaymentOutcomeNotDone) {
            dashboardScenariosService.recordScenarios(
                authToken,
                SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
                caseData.getCcdCaseReference().toString(),
                params
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
