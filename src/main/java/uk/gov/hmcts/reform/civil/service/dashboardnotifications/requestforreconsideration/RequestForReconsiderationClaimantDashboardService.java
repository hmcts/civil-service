package uk.gov.hmcts.reform.civil.service.dashboardnotifications.requestforreconsideration;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_CLAIMANT_RECIPIENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE;

@Service
public class RequestForReconsiderationClaimantDashboardService extends DashboardScenarioService {

    public RequestForReconsiderationClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                               DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyRequestForReconsideration(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
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
