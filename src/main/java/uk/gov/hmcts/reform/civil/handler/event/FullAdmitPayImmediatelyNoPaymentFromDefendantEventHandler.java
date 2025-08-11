package uk.gov.hmcts.reform.civil.handler.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DashboardScenariosService dashboardScenariosService;
    private final DashboardNotificationsParamsMapper mapper;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void createClaimantDashboardScenario(FullAdmitPayImmediatelyNoPaymentFromDefendantEvent event) {
        if (featureToggleService.isJudgmentOnlineLive()) {
            coreCaseDataService.triggerEvent(event.caseId(), CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ);
        } else {
            CaseDetails caseDetails = coreCaseDataService.getCase(event.caseId());
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            dashboardScenariosService.recordScenarios(
                userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword()),
                DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder()
                    .params(mapper.mapCaseDataToParams(caseData)).build()
            );
        }
    }
}
