package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullAdmitPayImmediatelyNoPaymentFromDefendantProcessor {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final IDashboardScenarioService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;

    public void createClaimantDashboardScenario(Long caseId) {
        CaseDetails caseDetails = coreCaseDataService.getCase(caseId);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        dashboardScenariosService.createScenario(
            userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword()),
            DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT,
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData)).build()
        );
    }
}
