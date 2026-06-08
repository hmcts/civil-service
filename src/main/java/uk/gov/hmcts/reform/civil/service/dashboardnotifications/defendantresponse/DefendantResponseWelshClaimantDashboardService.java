package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;

@Service
public class DefendantResponseWelshClaimantDashboardService extends DashboardScenarioService {

    private static final String DJ_NOTIFICATION = "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant";

    private final FeatureToggleService featureToggleService;
    private final DashboardNotificationService dashboardNotificationService;

    public DefendantResponseWelshClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                          DashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService,
                                                          DashboardNotificationService dashboardNotificationService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
    }

    public void notifyDefendantResponse(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            if (featureToggleService.isWelshEnabledForMainCase()) {
                return SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT.getScenario();
            }
            return SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (featureToggleService.isWelshEnabledForMainCase()
            && caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now())
            && caseData.getCcdCaseReference() != null) {
            dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
                DJ_NOTIFICATION,
                caseData.getCcdCaseReference().toString(),
                CLAIMANT_ROLE
            );
        }
    }
}
