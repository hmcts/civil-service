package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

@Service
public class HearingScheduledDefendantDashboardService extends AbstractHearingScheduledDashboardService {

    public HearingScheduledDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                     DashboardNotificationsParamsMapper mapper,
                                                     LocationReferenceDataService locationRefDataService,
                                                     CourtLocationUtils courtLocationUtils) {
        super(dashboardScenariosService, mapper, locationRefDataService, courtLocationUtils);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT.getScenario();
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        Map<String, Boolean> scenarios = new HashMap<>();

        if (shouldRecordScenario(caseData)) {
            scenarios.put(
                DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT.getScenario(),
                AllocatedTrack.FAST_CLAIM.name().equals(caseData.getAssignedTrack()) && isNull(caseData.getTrialReadyRespondent1())
            );

            scenarios.put(DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario(), true);
        }

        return scenarios;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isRespondent1NotRepresented();
    }
}
