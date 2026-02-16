package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;

import static java.util.Objects.nonNull;

public abstract class AbstractHearingScheduledDashboardService extends DashboardScenarioService {

    protected final LocationReferenceDataService locationRefDataService;
    protected final CourtLocationUtils courtLocationUtils;

    protected AbstractHearingScheduledDashboardService(DashboardScenariosService dashboardScenariosService,
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
}
