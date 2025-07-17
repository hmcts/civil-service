package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardJudgementOnlineCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT;

@Service
public class JudgementByAdmissionIssuedClaimantDashboardNotificationHandler extends DashboardJudgementOnlineCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT);
    public static final String TASK_ID = "GenerateDashboardNotificationJudgementByAdmissionClaimant";

    public JudgementByAdmissionIssuedClaimantDashboardNotificationHandler(DashboardScenariosService dashboardScenariosService,
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
        if (isJudgmentOrderIssued(caseData)) {
            return
                SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT.getScenario();
        }
        return null;
    }

    private boolean isJudgmentOrderIssued(CaseData caseData) {
        return caseData.isApplicantLiP()
            && isActiveJudgmentExist(caseData)
            && (isIndividualOrSoleTraderWithJoIssued(caseData)
            || isCompanyOrOrganisationWithRepaymentPlanAccepted(caseData));
    }

    private boolean isActiveJudgmentExist(CaseData caseData) {
        return (featureToggleService.isJudgmentOnlineLive()
            && caseData.getActiveJudgment() != null
            && JudgmentState.ISSUED.equals(caseData.getActiveJudgment().getState())
            && JudgmentType.JUDGMENT_BY_ADMISSION.equals(caseData.getActiveJudgment().getType()));
    }

    private boolean isIndividualOrSoleTraderWithJoIssued(CaseData caseData) {
        return caseData.getRespondent1().isIndividualORSoleTrader();
    }

    private boolean isCompanyOrOrganisationWithRepaymentPlanAccepted(CaseData caseData) {
        return caseData.getRespondent1().isCompanyOROrganisation()
            && caseData.hasApplicantAcceptedRepaymentPlan();
    }
}
