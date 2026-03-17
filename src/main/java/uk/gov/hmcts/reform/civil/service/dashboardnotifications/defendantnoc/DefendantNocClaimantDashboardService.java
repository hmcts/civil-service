package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_JBA_CLAIM_MOVES_OFFLINE_CLAIMANT;

@Service
public class DefendantNocClaimantDashboardService extends DashboardScenarioService {

    private final DashboardNotificationService dashboardNotificationService;
    private final FeatureToggleService featureToggleService;
    private final SdoCaseClassificationService sdoCaseClassificationService;

    public DefendantNocClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                DashboardNotificationsParamsMapper mapper,
                                                DashboardNotificationService dashboardNotificationService,
                                                FeatureToggleService featureToggleService,
                                                SdoCaseClassificationService sdoCaseClassificationService) {
        super(dashboardScenariosService, mapper);
        this.dashboardNotificationService = dashboardNotificationService;
        this.featureToggleService = featureToggleService;
        this.sdoCaseClassificationService = sdoCaseClassificationService;
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (!featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            return SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT.getScenario();
        }
        if (featureToggleService.isJudgmentOnlineLive()
            && CaseState.All_FINAL_ORDERS_ISSUED.equals(caseData.getPreviousCCDState())
            && nonNull(caseData.getActiveJudgment())) {
            return SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_JBA_CLAIM_MOVES_OFFLINE_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled() && isProceedingOffline(caseData);
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (!featureToggleService.isLipVLipEnabled() || !isProceedingOffline(caseData)) {
            return Map.of();
        }

        boolean shouldRecordTrialArrangements = isNull(caseData.getTrialReadyApplicant())
            && sdoCaseClassificationService.isFastTrack(caseData);

        PaymentDetails hearingFeePaymentDetails = caseData.getHearingFeePaymentDetails();
        boolean isHearingFeeNotPaid = (isNull(hearingFeePaymentDetails)
            || hearingFeePaymentDetails.getStatus() != PaymentStatus.SUCCESS)
            && !caseData.isHWFTypeHearing();
        boolean isFeePaymentOutcomeNotDone = caseData.isHWFTypeHearing()
            && isNull(caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee())
            && (isNull(hearingFeePaymentDetails) || hearingFeePaymentDetails.getStatus() != PaymentStatus.SUCCESS);

        return Map.of(
            SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_TRIAL_ARRANGEMENTS_TASK_LIST.getScenario(),
            shouldRecordTrialArrangements,
            SCENARIO_AAA6_DEFENDANT_NOC_CLAIMANT_HEARING_FEE_TASK_LIST.getScenario(),
            isHearingFeeNotPaid || isFeePaymentOutcomeNotDone
        );
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (!featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            String caseId = String.valueOf(caseData.getCcdCaseReference());
            dashboardNotificationService.deleteByReferenceAndCitizenRole(caseId, CLAIMANT_ROLE);
        }
    }

    private boolean isProceedingOffline(CaseData caseData) {
        return CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.equals(caseData.getCcdState());
    }
}
