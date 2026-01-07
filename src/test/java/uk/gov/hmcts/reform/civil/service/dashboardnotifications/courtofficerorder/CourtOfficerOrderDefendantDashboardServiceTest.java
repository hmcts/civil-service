package uk.gov.hmcts.reform.civil.service.dashboardnotifications.courtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class CourtOfficerOrderDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private CourtOfficerOrderDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioForLipDefendant() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(4567L)
            .respondent1Represented(YesOrNo.NO)
            .build();

        service.notifyCourtOfficerOrder(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario(),
            "4567",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldRecordExtraScenarioForFastTrackDefendantWithoutTrialReadiness() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(5678L)
            .respondent1Represented(YesOrNo.NO)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .build();

        service.notifyCourtOfficerOrder(caseData, AUTH_TOKEN);

        ScenarioRequestParams params = ScenarioRequestParams.builder().params(new HashMap<>()).build();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_DEFENDANT.getScenario(),
            "5678",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CASE_PROCEED_COURT_OFFICER_ORDER_TRIAL_READY_DEFENDANT.getScenario(),
            "5678",
            params
        );
    }
}
