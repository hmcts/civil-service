package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocUploadDashboardNotificationService {

    private final DashboardApiClient dashboardApiClient;
    private final GaForLipService gaForLipService;
    private final DashboardNotificationsParamsMapper mapper;

    public void createDashboardNotification(CaseData caseData, String role, String authToken, boolean itsUploadAddlDocEvent) {

        if (isWithNoticeOrConsent(caseData)) {
            log.info("Case {} is with notice or consent and the dashboard service is enabled", caseData.getCcdCaseReference());
            List<String> scenarios = getDashboardScenario(role, caseData, itsUploadAddlDocEvent);
            ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
                caseData)).build();
            scenarios.forEach(scenario -> dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                scenarioParams
            ));
        }
    }

    public void createResponseDashboardNotification(CaseData caseData, String role, String authToken) {

        if ((role.equalsIgnoreCase("APPLICANT")
            || (isWithNoticeOrConsent(caseData) && role.equalsIgnoreCase("RESPONDENT")))) {
            String scenario = getResponseDashboardScenario(role, caseData);
            ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
                caseData)).build();
            if (scenario != null) {
                dashboardApiClient.recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    scenario,
                    authToken,
                    scenarioParams
                );
            }
        }
    }

    private String getResponseDashboardScenario(String role, CaseData caseData) {
        if (role.equalsIgnoreCase("APPLICANT") && gaForLipService.isLipApp(caseData)) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT.getScenario();
        } else if (role.equalsIgnoreCase("RESPONDENT") && gaForLipService.isLipResp(caseData)) {
            return SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT.getScenario();
        }
        return null;
    }

    public void createOfflineResponseDashboardNotification(CaseData caseData, String role, String authToken) {

        String scenario = getResponseOfflineDashboardScenario(role, caseData, authToken);
        ScenarioRequestParams scenarioParams = ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(
            caseData)).build();
        if (scenario != null) {
            dashboardApiClient.recordScenario(
                caseData.getCcdCaseReference().toString(),
                scenario,
                authToken,
                scenarioParams
            );
        }
    }

    private String getResponseOfflineDashboardScenario(String role, CaseData caseData, String authToken) {
        if (role.equalsIgnoreCase("RESPONDENT")) {
            dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(caseData.getCcdCaseReference().toString(),
                                                                           "RESPONDENT", authToken);
            return SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT.getScenario();
        } else if (role.equalsIgnoreCase("APPLICANT")) {
            dashboardApiClient.deleteNotificationsForCaseIdentifierAndRole(caseData.getCcdCaseReference().toString(),
                                                                           "APPLICANT", authToken);
            return SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT.getScenario();
        }
        return null;
    }

    private List<String> getDashboardScenario(String role, CaseData caseData, boolean itsUploadAddlDocEvent) {
        List<String> scenarios = new ArrayList<>();
        if (DocUploadUtils.APPLICANT.equals(role) && gaForLipService.isLipResp(caseData)) {
            scenarios.add(SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT.getScenario());
        } else if (DocUploadUtils.RESPONDENT_ONE.equals(role) && gaForLipService.isLipApp(caseData)) {
            if (itsUploadAddlDocEvent
                && caseData.isUrgent()
                && caseData.getIsGaRespondentOneLip() == YesOrNo.NO) {
                scenarios.add(SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT.getScenario());
            }
            scenarios.add(SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT.getScenario());
        }
        return scenarios;
    }

    private boolean isWithNoticeOrConsent(CaseData caseData) {
        return JudicialDecisionNotificationUtil.isWithNotice(caseData)
            || caseData.getGeneralAppConsentOrder() == YES;
    }
}
