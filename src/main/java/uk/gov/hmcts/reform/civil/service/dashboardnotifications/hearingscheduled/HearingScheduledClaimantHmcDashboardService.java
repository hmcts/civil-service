package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant.HearingScheduledClaimantNotificationHandler.fillPreferredLocationData;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;

@Service
public class HearingScheduledClaimantHmcDashboardService extends DashboardScenarioService {

    private final LocationReferenceDataService locationRefDataService;
    private final HearingNoticeCamundaService camundaService;
    private final HearingFeesService hearingFeesService;

    public HearingScheduledClaimantHmcDashboardService(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper, LocationReferenceDataService locationRefDataService, HearingNoticeCamundaService camundaService, HearingFeesService hearingFeesService) {
        super(dashboardScenariosService, mapper);
        this.locationRefDataService = locationRefDataService;
        this.camundaService = camundaService;
        this.hearingFeesService = hearingFeesService;
    }

    public void notifyHearingScheduled(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public void beforeRecordScenario(CaseData caseData, String authToken) {
        List<LocationRefData> locations = (locationRefDataService.getHearingCourtLocations(authToken));
        //TODO: move this logic out into helper
        LocationRefData locationRefData = fillPreferredLocationData(locations, caseData.getHearingLocation());
        if (nonNull(locationRefData)) {
            caseData.setHearingLocationCourtName(locationRefData.getSiteName());
        }
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_CLAIMANT.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>();

        if (shouldRecordScenario(caseData)) {
            boolean hasPaidFee = caseData.isHearingFeePaid();
            boolean isHearingFeeRequired = hearingFeeRequired(camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingType());

            boolean shouldRecordFeeScenario = !hasPaidFee && isHearingFeeRequired;
            if (shouldRecordFeeScenario) {
                caseData.setHearingFee(HearingFeeUtils.calculateAndApplyFee(
                    hearingFeesService,
                    caseData,
                    caseData.getAssignedTrack()
                ));
            }
            scenarios.put(
                DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
                shouldRecordFeeScenario
            );

            boolean shouldRecordFastScenario = AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack()) && isNull(
                caseData.getTrialReadyApplicant());
            scenarios.put(
                DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT.getScenario(),
                shouldRecordFastScenario
            );

            scenarios.put(DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(), true);
        }

        return scenarios;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }
}
