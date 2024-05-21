package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CHANGE_VIEW_MEDIATION_INACTIVE_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CHANGE_VIEW_MEDIATION_INACTIVE_CARM;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;

@Service
@AllArgsConstructor
public class UploadMediationService {

    private final DashboardNotificationsParamsMapper mapper;
    private final DashboardApiClient dashboardApiClient;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;

    public String[] getScenarios(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        CaseData caseData = callbackParams.getCaseData();
        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
        if (isApplicantSolicitor(roles) || isLIPClaimant(roles)) {
            return new String[]{
                //change the status of claimant
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CARM.getScenario(),
                //set view mediation documents available on defendant side
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CHANGE_VIEW_MEDIATION_INACTIVE_CARM.getScenario()

            };
        } else if (isRespondentSolicitorOne(roles) || isLIPDefendant(roles)){
            return new String[]{
                //change the status of defendant
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_DEFENDANT_CARM.getScenario(),
                //set view mediation documents available on claimant side
                SCENARIO_AAA6_UPLOAD_MEDIATION_DOCUMENT_CLAIMANT_CHANGE_VIEW_MEDIATION_INACTIVE_CARM.getScenario()
            };
        }
        return new String[]{};
    }

    public void recordScenarios(String[] scenarios, CaseData caseData, String authToken) {
        for (String scenario : scenarios) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                ScenarioRequestParams.builder()
                    .params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }
    }

    private boolean hasMediationApplicant1UploadedDocuments(CaseData caseData ){
        return caseData.getApp1MediationDocumentsReferred() != null || caseData.getApp1MediationNonAttendanceDocs() != null;
    }
    private boolean hasMediationDefendant1UploadedDocuments(CaseData caseData ){
        return caseData.getRes1MediationDocumentsReferred() != null || caseData.getRes1MediationNonAttendanceDocs() != null;
    }
}
