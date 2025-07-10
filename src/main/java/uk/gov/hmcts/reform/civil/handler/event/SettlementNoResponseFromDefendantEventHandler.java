package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

@Service
@RequiredArgsConstructor
public class SettlementNoResponseFromDefendantEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;

    @EventListener
    public void createClaimantDashboardScenario(SettlementNoResponseFromDefendantEvent event) {
        CaseDetails caseDetails = coreCaseDataService.getCase(event.getCaseId());
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        dashboardScenariosService.recordScenarios(
            userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword()),
            DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_SETTLEMENT_NO_RESPONSE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder()
                .params(mapper.mapCaseDataToParams(caseData)).build()
        );
    }
}
