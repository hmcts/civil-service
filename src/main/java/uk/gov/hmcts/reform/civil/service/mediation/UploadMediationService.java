package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CHANGE_VIEW_MEDIATION_AVAILABLE_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CHANGE_VIEW_MEDIATION_AVAILABLE_CARM;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;

@Slf4j
@Service
@AllArgsConstructor
public class UploadMediationService {

    private final DashboardNotificationsParamsMapper mapper;
    private final DashboardScenariosService dashboardScenariosService;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final FeatureToggleService featureToggleService;

    public void uploadMediationDocumentsTaskList(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
            String[] scenarios = getScenarios(callbackParams);
            recordScenarios(scenarios, caseData, authToken);
        }
    }

    private String[] getScenarios(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
        if (isApplicantSolicitor(roles) || isLIPClaimant(roles)) {
            return new String[]{
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM.getScenario(),
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CHANGE_VIEW_MEDIATION_AVAILABLE_CARM.getScenario()
            };
        } else if (isRespondentSolicitorOne(roles) || isLIPDefendant(roles)) {
            return new String[]{
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM.getScenario(),
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CHANGE_VIEW_MEDIATION_AVAILABLE_CARM.getScenario()
            };
        }
        return new String[]{};
    }

    private void recordScenarios(String[] scenarios, CaseData caseData, String authorisation) {
        for (String scenario : scenarios) {
            dashboardScenariosService.recordScenarios(
                authorisation, scenario,
                caseData.getCcdCaseReference().toString(), ScenarioRequestParams.builder()
                    .params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }
    }
}
