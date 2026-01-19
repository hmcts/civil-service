package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_SDO_DRAWN_PRE_CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_WITHOUT_UPLOAD_FILES_CARM;

@Service
@Slf4j
public class CreateSdoDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;
    private final CreateSdoDashboardDecisionService createSdoDashboardDecisionService;
    private final CreateSdoDashboardDateService createSdoDashboardDateService;

    public CreateSdoDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                              DashboardNotificationsParamsMapper mapper,
                                              FeatureToggleService featureToggleService,
                                              CreateSdoDashboardDecisionService createSdoDashboardDecisionService,
                                              CreateSdoDashboardDateService createSdoDashboardDateService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.createSdoDashboardDecisionService = createSdoDashboardDecisionService;
        this.createSdoDashboardDateService = createSdoDashboardDateService;
    }

    public void notifyBundleUpdated(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {

        final String scenario;

        if (createSdoDashboardDecisionService.isEligibleForReconsideration(caseData)
            && Objects.isNull(caseData.getIsReferToJudgeClaim())) {
            scenario = SCENARIO_AAA6_CP_SDO_MADE_BY_LA_DEFENDANT.getScenario();
        } else if (createSdoDashboardDecisionService.isCarmApplicableCase(caseData)
            && createSdoDashboardDecisionService.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(caseData)
            && createSdoDashboardDecisionService.hasTrackChanged(caseData)) {

            if (createSdoDashboardDecisionService.hasUploadDocuments(caseData)) {
                scenario = SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_CARM.getScenario();
            } else {
                scenario = SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_DEFENDANT_WITHOUT_UPLOAD_FILES_CARM.getScenario();
            }
        } else if (createSdoDashboardDecisionService.isSDODrawnPreCPRelease(caseData)) {
            scenario = SCENARIO_AAA6_DEFENDANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario();
        } else {
            scenario = SCENARIO_AAA6_CP_ORDER_MADE_DEFENDANT.getScenario();
        }

        return scenario;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented()
            && featureToggleService.isLipVLipEnabled()
            && createSdoDashboardDecisionService.isDashBoardEnabledForCase(caseData);
    }

    @Override
    protected ScenarioRequestParams scenarioRequestParamsFrom(CaseData caseData) {
        if (isNull(caseData.getRequestForReconsiderationDeadline())
            && createSdoDashboardDecisionService.isEligibleForReconsideration(caseData)) {
            caseData.setRequestForReconsiderationDeadline(
                createSdoDashboardDateService.getDateWithoutBankHolidays(LocalDateTime.now()));
        }

        return ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();
    }
}
