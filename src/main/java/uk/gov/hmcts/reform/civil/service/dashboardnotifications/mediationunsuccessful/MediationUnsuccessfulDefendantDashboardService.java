package uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationunsuccessful;

import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_MEDIATION_WHEN_CLAIMANT_NOT_CONTACTABLE;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MediationUnsuccessfulDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public MediationUnsuccessfulDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                          DashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService) {

        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyMediationUnsuccessful(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            if (isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(caseData)) {
                return SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_DEFENDANT_NONATTENDANCE.getScenario();
            } else if (isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData)) {
                return SCENARIO_AAA6_DEFENDANT_MEDIATION_WHEN_CLAIMANT_NOT_CONTACTABLE.getScenario();
            } else {
                return SCENARIO_AAA6_DEFENDANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario();
            }
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_DEFENDANT.getScenario();
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
    }
}
