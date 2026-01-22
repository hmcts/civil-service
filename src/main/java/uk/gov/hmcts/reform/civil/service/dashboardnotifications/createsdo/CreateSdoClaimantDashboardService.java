package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_WITHOUT_UPLOAD_FILES_CARM;

@Service
public class CreateSdoClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;
    private final DashboardNotificationHelper dashboardDecisionHelper;
    private final CreateSdoDashboardDate createSdoDashboardDate;

    public CreateSdoClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                             DashboardNotificationsParamsMapper mapper,
                                             FeatureToggleService featureToggleService,
                                             DashboardNotificationHelper dashboardDecisionHelper,
                                             CreateSdoDashboardDate createSdoDashboardDate) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardDecisionHelper = dashboardDecisionHelper;
        this.createSdoDashboardDate = createSdoDashboardDate;
    }

    public void notifySdoCreated(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {

        final String scenario;

        if (dashboardDecisionHelper.isEligibleForReconsideration(caseData)
            && Objects.isNull(caseData.getIsReferToJudgeClaim())) {
            scenario = SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT.getScenario();
        } else if (dashboardDecisionHelper.isCarmApplicableCase(caseData)
            && dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData)
            && dashboardDecisionHelper.hasTrackChanged(caseData)) {

            if (dashboardDecisionHelper.hasUploadDocuments(caseData)) {
                scenario = SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM.getScenario();
            } else {
                scenario = SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_WITHOUT_UPLOAD_FILES_CARM.getScenario();
            }
        } else if (dashboardDecisionHelper.isSDODrawnPreCPRelease(caseData)) {
            scenario = SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario();
        } else {
            scenario = SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT.getScenario();
        }

        return scenario;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented()
            && featureToggleService.isLipVLipEnabled()
            && dashboardDecisionHelper.isDashBoardEnabledForCase(caseData);
    }

    @Override
    protected String getExtraScenario() {
        return SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordExtraScenario(CaseData caseData) {
        List<String> addNocNotificationScenarios = List.of(
            SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT.getScenario(),
            SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT.getScenario()
        );
        return caseData.isLipvLROneVOne()
            && addNocNotificationScenarios.contains(getScenario(caseData))
            && featureToggleService.isLipVLipEnabled()
            && dashboardDecisionHelper.isDashBoardEnabledForCase(caseData);
    }

    @Override
    protected ScenarioRequestParams scenarioRequestParamsFrom(CaseData caseData) {
        if (isNull(caseData.getRequestForReconsiderationDeadline())
            && dashboardDecisionHelper.isEligibleForReconsideration(caseData)) {
            caseData.setRequestForReconsiderationDeadline(
                createSdoDashboardDate.getDateWithoutBankHolidays(LocalDateTime.now()));
        }

        return ScenarioRequestParams.builder()
            .params(mapper.mapCaseDataToParams(caseData))
            .build();
    }
}
