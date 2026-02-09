package uk.gov.hmcts.reform.civil.service.dashboardnotifications.mediationunsuccessful;

import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_CLAIMANT_NONATTENDANCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_GENERIC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_MEDIATION_WHEN_DEFENDANT_NOT_CONTACTABLE;
import static uk.gov.hmcts.reform.civil.utils.MediationUtils.findMediationUnsuccessfulReason;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MediationUnsuccessfulClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public MediationUnsuccessfulClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                         DashboardNotificationsParamsMapper mapper,
                                                         FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyMediationUnsuccessful(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented();
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            if (isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(caseData)) {
                return SCENARIO_AAA6_CLAIMANT_MEDIATION_WHEN_DEFENDANT_NOT_CONTACTABLE.getScenario();
            } else if (isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData)) {
                return SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_CLAIMANT_NONATTENDANCE.getScenario();
            } else {
                return SCENARIO_AAA6_CLAIMANT_MEDIATION_UNSUCCESSFUL_GENERIC.getScenario();
            }
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_MEDIATION_UNSUCCESSFUL_CLAIMANT.getScenario();
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_DEFENDANT_ONE));
    }

    private boolean isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(CaseData caseData) {
        return findMediationUnsuccessfulReason(caseData, List.of(NOT_CONTACTABLE_CLAIMANT_ONE));
    }
}
