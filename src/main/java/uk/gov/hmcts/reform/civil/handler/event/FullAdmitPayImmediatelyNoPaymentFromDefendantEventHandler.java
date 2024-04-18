package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DashboardApiClient dashboardApiClient;
    private final DashboardNotificationsParamsMapper mapper;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;

    @EventListener
    public void createClaimantDashboardScenario(FullAdmitPayImmediatelyNoPaymentFromDefendantEvent event) {
        CaseDetails caseDetails = coreCaseDataService.getCase(event.caseId());
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        dashboardApiClient.recordScenario(
            caseData.getCcdCaseReference().toString(),
            DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT.getScenario(),
            userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword()),
            ScenarioRequestParams.builder().params(mapper.mapCaseDataToParams(caseData)).build()
        );
    }
}
