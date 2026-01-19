package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class HearingScheduledDefendantDashboardServiceTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private LocationReferenceDataService locationRefDataService;

    @InjectMocks
    private HearingScheduledDefendantDashboardService service;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String CASE_ID = "12345678";

    @Test
    void shouldPopulateCourtName_whenBeforeRecordScenarioIsCalled() {
        CaseData caseData = CaseData.builder()
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.builder()
                                            .label("Court Name - Address - Postcode")
                                            .build())
                                 .build())
            .build();
        LocationRefData locationRefData = LocationRefData.builder()
            .siteName("Court Name")
            .courtAddress("Address")
            .postcode("Postcode")
            .build();
        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(List.of(locationRefData));

        service.beforeRecordScenario(caseData, AUTH_TOKEN);

        assert "Court Name".equals(caseData.getHearingLocationCourtName());
    }

    @Test
    void shouldNotPopulateCourtName_whenNoMatchingLocationFound() {
        CaseData caseData = CaseData.builder()
            .hearingLocation(DynamicList.builder()
                                 .value(DynamicListElement.builder()
                                            .label("Non-Matching Label")
                                            .build())
                                 .build())
            .build();

        LocationRefData otherLocation = LocationRefData.builder()
            .siteName("Other Court")
            .courtAddress("Other Address")
            .postcode("Other Postcode")
            .build();

        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN))
            .thenReturn(List.of(otherLocation));

        service.beforeRecordScenario(caseData, AUTH_TOKEN);

        assert caseData.getHearingLocationCourtName() == null;
    }

    @Test
    void shouldRecordScenarios_whenDefendantIsUnrepresented() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .respondent1Represented(NO)
            .build();

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }

    @Test
    void shouldNotRecordScenarios_whenDefendantIsRepresented() {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .respondent1Represented(YES)
            .build();

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            anyString(),
            anyString(),
            anyString(),
            any()
        );
    }

    @Test
    void shouldRecordRelistHearingScenario_whenFastClaimAndTrialReadyIsNull() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(Long.valueOf(CASE_ID))
            .respondent1Represented(NO)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .trialReadyRespondent1(null)
            .build();

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT.getScenario(),
            CASE_ID,
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }
}
