package uk.gov.hmcts.reform.civil.service.dashboardnotifications.uploadhearingdocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

@Service
public class UploadHearingDocumentsClaimantService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    protected UploadHearingDocumentsClaimantService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyUploadHearingDocuments(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantNotRepresented()
            && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && featureToggleService.isLipVLipEnabled()
            && (featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
            || featureToggleService.isWelshEnabledForMainCase());
    }
}
