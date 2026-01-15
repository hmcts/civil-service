package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTab;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoLocationServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private LocationReferenceDataService locationReferenceDataService;

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    private SdoLocationService service;

    @BeforeEach
    void setUp() {
        service = new SdoLocationService(
            locationReferenceDataService,
            locationHelper,
            Optional.of(updateWaCourtLocationsService)
        );
    }

    @Test
    void shouldFetchHearingLocations() {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        List<LocationRefData> locations = List.of(location);
        when(locationReferenceDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(locations);

        List<LocationRefData> result = service.fetchHearingLocations(AUTH_TOKEN);

        assertThat(result).isEqualTo(locations);
        verify(locationReferenceDataService).getHearingCourtLocations(AUTH_TOKEN);
    }

    @Test
    void shouldFetchDefaultJudgmentLocations() {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId("456");
        List<LocationRefData> locations = List.of(location);
        when(locationReferenceDataService.getCourtLocationsForDefaultJudgments(AUTH_TOKEN)).thenReturn(locations);

        List<LocationRefData> result = service.fetchDefaultJudgmentLocations(AUTH_TOKEN);

        assertThat(result).isEqualTo(locations);
        verify(locationReferenceDataService).getCourtLocationsForDefaultJudgments(AUTH_TOKEN);
    }

    @Test
    void shouldFetchCourtLocationsByEpimmsId() {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId("789");
        List<LocationRefData> locations = List.of(location);
        when(locationReferenceDataService.getCourtLocationsByEpimmsId(AUTH_TOKEN, "789")).thenReturn(locations);

        List<LocationRefData> result = service.fetchCourtLocationsByEpimmsId(AUTH_TOKEN, "789");

        assertThat(result).isEqualTo(locations);
        verify(locationReferenceDataService).getCourtLocationsByEpimmsId(AUTH_TOKEN, "789");
    }

    @Test
    void shouldBuildLocationListWithMatchingSelection() {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Site");
        location.setCourtAddress("Address");
        location.setPostcode("AB1");
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setCaseLocation(new CaseLocationCivil().setBaseLocation("123"));

        when(locationHelper.getMatching(locations, requestedCourt)).thenReturn(Optional.of(location));

        DynamicList list = service.buildLocationList(requestedCourt, false, locations);

        assertThat(list.getValue().getCode()).isEqualTo("123");
        assertThat(list.getListItems()).extracting(DynamicListElement::getCode).containsExactly("123");
    }

    @Test
    void shouldBuildLocationListWithoutSelectionWhenAllCourtsRequested() {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Site");
        location.setCourtAddress("Address");
        location.setPostcode("AB1");
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = new RequestedCourt();

        DynamicList list = service.buildLocationList(requestedCourt, true, locations);

        assertThat(list.getValue()).isEqualTo(DynamicListElement.EMPTY);
    }

    @Test
    void shouldBuildCourtLocationListForSdoR2() {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Site");
        location.setCourtAddress("Address");
        location.setPostcode("AB1");
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setCaseLocation(new CaseLocationCivil().setBaseLocation("123"));

        when(locationHelper.getMatching(locations, requestedCourt)).thenReturn(Optional.of(location));

        DynamicList list = service.buildCourtLocationForSdoR2(requestedCourt, locations);

        assertThat(list.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly(
                LocationReferenceDataService.getDisplayEntry(location),
                "Other location"
            );
    }

    @Test
    void shouldTrimListToSelectedValue() {
        DynamicListElement selection = DynamicListElement.dynamicElementFromCode("123", "Site");
        DynamicList list = new DynamicList();
        list.setValue(selection);
        list.setListItems(List.of(selection));

        DynamicList trimmed = service.trimListItems(list);

        assertThat(trimmed.getValue()).isEqualTo(selection);
        assertThat(trimmed.getListItems()).isNull();
    }

    @Test
    void shouldUpdateWaLocationsWhenServicePresent() {
        CaseData caseData = CaseDataBuilder.builder().build();

        doAnswer(invocation -> null).when(updateWaCourtLocationsService)
            .updateCourtListingWALocations(AUTH_TOKEN, caseData);

        service.updateWaLocationsIfRequired(caseData, AUTH_TOKEN);

        verify(updateWaCourtLocationsService).updateCourtListingWALocations(AUTH_TOKEN, caseData);
    }

    @Test
    void shouldSkipWaLocationUpdateWhenServiceMissing() {
        service = new SdoLocationService(
            locationReferenceDataService,
            locationHelper,
            Optional.empty()
        );

        CaseData caseData = CaseDataBuilder.builder().build();

        service.updateWaLocationsIfRequired(caseData, AUTH_TOKEN);

        verify(updateWaCourtLocationsService, never()).updateCourtListingWALocations(anyString(), any());
    }

    @Test
    void shouldBuildAlternativeLocationList() {
        LocationRefData location1 = new LocationRefData();
        location1.setEpimmsId("123");
        location1.setSiteName("Site 1");
        location1.setCourtAddress("Address 1");
        location1.setPostcode("AB1");
        LocationRefData location2 = new LocationRefData();
        location2.setEpimmsId("456");
        location2.setSiteName("Site 2");
        location2.setCourtAddress("Address 2");
        location2.setPostcode("AB2");

        DynamicList list = service.buildAlternativeCourtLocations(List.of(location1, location2));

        assertThat(list.getListItems()).extracting(DynamicListElement::getCode).containsExactly("123", "456");
    }

    @Test
    void shouldClearWaMetadataFieldsOnBuilder() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .taskManagementLocations(new TaskManagementLocationTypes())
            .taskManagementLocationsTab(new TaskManagementLocationTab())
            .caseManagementLocationTab(new TaskManagementLocationTab())
            .build();

        service.clearWaLocationMetadata(caseData);

        assertThat(caseData.getTaskManagementLocations()).isNull();
        assertThat(caseData.getTaskManagementLocationsTab()).isNull();
        assertThat(caseData.getCaseManagementLocationTab()).isNull();
    }
}
