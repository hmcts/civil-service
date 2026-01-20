package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting.LISTING;

@Service("hearingScheduledClaimantDashboardService")
public class HearingScheduledClaimantDashboardService extends DashboardScenarioService {

    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    public HearingScheduledClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    LocationReferenceDataService locationRefDataService,
                                                    CourtLocationUtils courtLocationUtils) {
        super(dashboardScenariosService, mapper);
        this.locationRefDataService = locationRefDataService;
        this.courtLocationUtils = courtLocationUtils;
    }

    public void notifyHearingScheduled(CaseData caseData, String authToken) {
        populateCourtName(caseData, authToken);
        recordScenario(caseData, authToken);
    }

    private void populateCourtName(CaseData caseData, String authToken) {
        List<LocationRefData> locations = locationRefDataService.getHearingCourtLocations(authToken);
        LocationRefData locationRefData = courtLocationUtils.fillPreferredLocationData(
            locations,
            caseData.getHearingLocation()
        );
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
            scenarios.put(
                DashboardScenarios.SCENARIO_AAA6_CP_HEARING_FEE_REQUIRED_CLAIMANT.getScenario(),
                isHearingFeeRequired(caseData)
            );

            scenarios.put(
                DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_CLAIMANT.getScenario(),
                isTrialArrangementEligible(caseData)
            );

            scenarios.put(DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_CLAIMANT.getScenario(), true);
        }

        return scenarios;
    }

    protected boolean isHearingFeeRequired(CaseData caseData) {
        boolean hasPaidFee = caseData.isHearingFeePaid();
        boolean isNewHearingWithoutFee = (caseData.getCcdState() == HEARING_READINESS && caseData.getListingOrRelisting() == LISTING);

        return !hasPaidFee && isNewHearingWithoutFee;
    }

    protected boolean isTrialArrangementEligible(CaseData caseData) {
        return AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack()) && isNull(caseData.getTrialReadyApplicant());
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }
}
