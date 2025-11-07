package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Captor
    private ArgumentCaptor<CaseData.CaseDataBuilder<?, ?>> caseDataBuilderCaptor;

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
        List<LocationRefData> locations = List.of(LocationRefData.builder().epimmsId("123").build());
        when(locationReferenceDataService.getHearingCourtLocations(AUTH_TOKEN)).thenReturn(locations);

        List<LocationRefData> result = service.fetchHearingLocations(AUTH_TOKEN);

        assertThat(result).isEqualTo(locations);
        verify(locationReferenceDataService).getHearingCourtLocations(AUTH_TOKEN);
    }

    @Test
    void shouldFetchDefaultJudgmentLocations() {
        List<LocationRefData> locations = List.of(LocationRefData.builder().epimmsId("456").build());
        when(locationReferenceDataService.getCourtLocationsForDefaultJudgments(AUTH_TOKEN)).thenReturn(locations);

        List<LocationRefData> result = service.fetchDefaultJudgmentLocations(AUTH_TOKEN);

        assertThat(result).isEqualTo(locations);
        verify(locationReferenceDataService).getCourtLocationsForDefaultJudgments(AUTH_TOKEN);
    }

    @Test
    void shouldBuildLocationListWithMatchingSelection() {
        LocationRefData location = LocationRefData.builder()
            .epimmsId("123")
            .siteName("Site")
            .courtAddress("Address")
            .postcode("AB1")
            .build();
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .caseLocation(CaseLocationCivil.builder().baseLocation("123").build())
            .build();

        when(locationHelper.getMatching(locations, requestedCourt)).thenReturn(Optional.of(location));

        DynamicList list = service.buildLocationList(requestedCourt, false, locations);

        assertThat(list.getValue().getCode()).isEqualTo("123");
        assertThat(list.getListItems()).extracting(DynamicListElement::getCode).containsExactly("123");
    }

    @Test
    void shouldBuildLocationListWithoutSelectionWhenAllCourtsRequested() {
        LocationRefData location = LocationRefData.builder()
            .epimmsId("123")
            .siteName("Site")
            .courtAddress("Address")
            .postcode("AB1")
            .build();
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = RequestedCourt.builder().build();

        DynamicList list = service.buildLocationList(requestedCourt, true, locations);

        assertThat(list.getValue()).isEqualTo(DynamicListElement.EMPTY);
    }

    @Test
    void shouldBuildCourtLocationListForSdoR2() {
        LocationRefData location = LocationRefData.builder()
            .epimmsId("123")
            .siteName("Site")
            .courtAddress("Address")
            .postcode("AB1")
            .build();
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .caseLocation(CaseLocationCivil.builder().baseLocation("123").build())
            .build();

        when(locationHelper.getMatching(locations, requestedCourt)).thenReturn(Optional.of(location));

        DynamicList list = service.buildCourtLocationForSdoR2(requestedCourt, locations);

        assertThat(list.getListItems()).extracting(DynamicListElement::getLabel)
            .containsExactly(
                LocationReferenceDataService.getDisplayEntry(location),
                "Other location"
            );
    }

    @Test
    void shouldTrimListToSelectedValue() {
        DynamicListElement selection = DynamicListElement.dynamicElementFromCode("123", "Site");
        DynamicList list = DynamicList.builder()
            .value(selection)
            .listItems(List.of(selection))
            .build();

        DynamicList trimmed = service.trimListItems(list);

        assertThat(trimmed.getValue()).isEqualTo(selection);
        assertThat(trimmed.getListItems()).isNull();
    }

    @Test
    void shouldUpdateWaLocationsWhenServicePresent() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.updateWaLocationsIfRequired(caseData, builder, AUTH_TOKEN);

        verify(updateWaCourtLocationsService)
            .updateCourtListingWALocations(eq(AUTH_TOKEN), caseDataBuilderCaptor.capture());
        assertThat(caseDataBuilderCaptor.getValue()).isNotNull();
    }

    @Test
    void shouldSkipWaLocationUpdateWhenServiceMissing() {
        service = new SdoLocationService(
            locationReferenceDataService,
            locationHelper,
            Optional.empty()
        );

        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        service.updateWaLocationsIfRequired(caseData, builder, AUTH_TOKEN);

        verify(updateWaCourtLocationsService, never()).updateCourtListingWALocations(eq(AUTH_TOKEN), any());
    }

    @Test
    void shouldBuildAlternativeLocationList() {
        LocationRefData location1 = LocationRefData.builder()
            .epimmsId("123")
            .siteName("Site 1")
            .courtAddress("Address 1")
            .postcode("AB1")
            .build();
        LocationRefData location2 = LocationRefData.builder()
            .epimmsId("456")
            .siteName("Site 2")
            .courtAddress("Address 2")
            .postcode("AB2")
            .build();

        DynamicList list = service.buildAlternativeCourtLocations(List.of(location1, location2));

        assertThat(list.getListItems()).extracting(DynamicListElement::getCode).containsExactly("123", "456");
    }
}
