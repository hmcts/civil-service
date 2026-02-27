package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class HearingScheduledDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String CASE_ID = "12345678";
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private LocationReferenceDataService locationRefDataService;
    @Mock
    private CourtLocationUtils courtLocationUtils;
    @InjectMocks
    private HearingScheduledDefendantDashboardService service;

    @Test
    void shouldPopulateCourtName_whenNotifyHearingScheduledIsCalled() {
        DynamicList hearingLocation = new DynamicList();
        hearingLocation.setValue(DynamicListElement.dynamicElement("Court Name - Address - Postcode"));

        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted().build();
        caseData.setHearingLocation(hearingLocation);

        LocationRefData locationRefData = new LocationRefData();
        locationRefData.setSiteName("Court Name");
        locationRefData.setCourtAddress("Address");
        locationRefData.setPostcode("Postcode");

        List<LocationRefData> locations = List.of(locationRefData);
        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(locations);
        when(courtLocationUtils.fillPreferredLocationData(locations, hearingLocation)).thenReturn(locationRefData);

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        assertThat(caseData.getHearingLocationCourtName()).isEqualTo("Court Name");
    }

    @Test
    void shouldNotPopulateCourtName_whenNoMatchingLocationFound() {
        DynamicList hearingLocation = new DynamicList();
        hearingLocation.setValue(DynamicListElement.dynamicElement("Non-Matching Label"));

        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted().build();
        caseData.setHearingLocation(hearingLocation);

        LocationRefData otherLocation = new LocationRefData();
        otherLocation.setSiteName("Other Name");
        otherLocation.setCourtAddress("Other Address");
        otherLocation.setPostcode("Other Postcode");

        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN))
            .thenReturn(List.of(otherLocation));

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        assertThat(caseData.getHearingLocationCourtName()).isNull();
    }

    @Test
    void shouldNotPopulateCourtName_whenLocationRefDataIsNull() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted().build();
        when(locationRefDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(List.of());
        when(courtLocationUtils.fillPreferredLocationData(any(), any())).thenReturn(null);

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        assertThat(caseData.getHearingLocationCourtName()).isNull();
    }

    @Test
    void shouldRecordScenarios_whenDefendantIsUnrepresented() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.parseLong(CASE_ID))
            .respondent1Represented(YesOrNo.NO)
            .build();
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_HEARING_SCHEDULED_DEFENDANT.getScenario()),
            eq(CASE_ID),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario()),
            eq(CASE_ID),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarios_whenDefendantIsRepresented() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.parseLong(CASE_ID))
            .respondent1Represented(YesOrNo.YES)
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
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.parseLong(CASE_ID))
            .respondent1Represented(YesOrNo.NO)
            .build()
            .setAllocatedTrack(AllocatedTrack.FAST_CLAIM).build();
        caseData.setTrialReadyRespondent1(null);

        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT.getScenario()),
            eq(CASE_ID),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordRelistHearingScenario_whenFastClaimAndTrialReadyIsNotNull() {
        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .ccdCaseReference(Long.parseLong(CASE_ID))
            .respondent1Represented(YesOrNo.NO)
            .build()
            .setAllocatedTrack(AllocatedTrack.FAST_CLAIM).build();
        caseData.setTrialReadyRespondent1(YesOrNo.YES);

        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        service.notifyHearingScheduled(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            anyString(),
            eq(SCENARIO_AAA6_CP_TRIAL_ARRANGEMENTS_RELIST_HEARING_DEFENDANT.getScenario()),
            anyString(),
            any()
        );
    }
}
