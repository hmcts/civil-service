package uk.gov.hmcts.reform.civil.service.referencedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationReferenceDataServiceTest {

    private static final String AUTH = "Bearer token";

    @Mock
    private LocationReferenceDataApiClient locationReferenceDataApiClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private LocationReferenceDataService service;

    @BeforeEach
    void setUp() {
        lenient().when(authTokenGenerator.generate()).thenReturn("service-token");
    }

    @Test
    void shouldReturnFirstCnbcLocation() {
        LocationRefData expected = location("CNBC", "England", "123");
        when(locationReferenceDataApiClient.getCourtVenueByName(
            any(), eq(AUTH), eq(LocationReferenceDataService.CIVIL_NATIONAL_BUSINESS_CENTRE)))
            .thenReturn(List.of(expected));

        assertThat(service.getCnbcLocation(AUTH)).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyLocationWhenCnbcNotFound() {
        when(locationReferenceDataApiClient.getCourtVenueByName(any(), eq(AUTH),
            eq(LocationReferenceDataService.CIVIL_NATIONAL_BUSINESS_CENTRE)))
            .thenReturn(List.of());

        assertThat(service.getCnbcLocation(AUTH)).isEqualTo(LocationRefData.builder().build());
    }

    @Test
    void shouldReturnEmptyLocationWhenCnbcLookupFails() {
        when(locationReferenceDataApiClient.getCourtVenueByName(any(), eq(AUTH),
            eq(LocationReferenceDataService.CIVIL_NATIONAL_BUSINESS_CENTRE)))
            .thenThrow(new RuntimeException("boom"));

        assertThat(service.getCnbcLocation(AUTH)).isEqualTo(LocationRefData.builder().build());
    }

    @Test
    void shouldReturnEmptyLocationWhenCcmccNotFound() {
        when(locationReferenceDataApiClient.getCourtVenueByName(any(), eq(AUTH),
            eq(LocationReferenceDataService.COUNTY_COURT_MONEY_CLAIMS_CENTRE)))
            .thenReturn(List.of());

        assertThat(service.getCcmccLocation(AUTH)).isEqualTo(LocationRefData.builder().build());
    }

    @Test
    void shouldReturnEmptyLocationWhenCcmccLookupFails() {
        when(locationReferenceDataApiClient.getCourtVenueByName(any(), eq(AUTH),
            eq(LocationReferenceDataService.COUNTY_COURT_MONEY_CLAIMS_CENTRE)))
            .thenThrow(new RuntimeException("boom"));

        assertThat(service.getCcmccLocation(AUTH)).isEqualTo(LocationRefData.builder().build());
    }

    @Test
    void shouldReturnFirstCcmccLocation() {
        LocationRefData expected = location("CCMCC", "England", "456");
        when(locationReferenceDataApiClient.getCourtVenueByName(
            any(), eq(AUTH), eq(LocationReferenceDataService.COUNTY_COURT_MONEY_CLAIMS_CENTRE)))
            .thenReturn(List.of(expected));

        assertThat(service.getCcmccLocation(AUTH)).isEqualTo(expected);
    }

    @Test
    void shouldFilterGeneralApplicationLocationsToEnglandAndWales() {
        LocationRefData england = location("B", "England", "111");
        LocationRefData wales = location("A", "Wales", "222");
        LocationRefData scotland = location("C", "Scotland", "333");
        when(locationReferenceDataApiClient.getCourtVenue(any(), eq(AUTH), any(), any(), any(), any()))
            .thenReturn(List.of(england, wales, scotland));

        List<LocationRefData> locations = service.getCourtLocationsForGeneralApplication(AUTH);

        assertThat(locations).containsExactly(wales, england); // sorted by site name and no Scotland
    }

    @Test
    void shouldReturnEmptyListWhenGeneralApplicationLookupFails() {
        when(locationReferenceDataApiClient.getCourtVenue(any(), eq(AUTH), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("error"));

        assertThat(service.getCourtLocationsForGeneralApplication(AUTH)).isEmpty();
    }

    @Test
    void shouldReturnOptionalLocationMatchingLabel() {
        LocationRefData location = location("Site", "England", "111").toBuilder()
            .courtAddress("Address")
            .postcode("PC")
            .build();
        when(locationReferenceDataApiClient.getHearingVenue(any(), eq(AUTH), any(), any(), any()))
            .thenReturn(List.of(location));

        String label = LocationReferenceDataService.getDisplayEntry(location);
        Optional<LocationRefData> result = service.getLocationMatchingLabel(label, AUTH);

        assertThat(result).contains(location);
    }

    @Test
    void shouldReturnEmptyOptionalWhenLabelBlank() {
        assertThat(service.getLocationMatchingLabel(" ", AUTH)).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoLocationsMatch() {
        when(locationReferenceDataApiClient.getHearingVenue(any(), eq(AUTH), any(), any(), any()))
            .thenReturn(List.of());

        assertThat(service.getLocationMatchingLabel("Unk", AUTH)).isEmpty();
    }

    @Test
    void shouldReturnCourtLocationForMatchingCode() {
        LocationRefData match = location("Site", "England", "321");
        when(locationReferenceDataApiClient.getCourtVenueByLocationCode(any(), eq(AUTH), any(), any(), any(), any()))
            .thenReturn(List.of(match));

        assertThat(service.getCourtLocation(AUTH, "321")).isEqualTo(match);
    }

    @Test
    void shouldThrowWhenMatchingCourtLocationNotFound() {
        when(locationReferenceDataApiClient.getCourtVenueByLocationCode(any(), eq(AUTH), any(), any(), any(), any()))
            .thenReturn(List.of(location("Site", "England", "123")));

        assertThatThrownBy(() -> service.getCourtLocation(AUTH, "999"))
            .isInstanceOf(LocationRefDataException.class);
    }

    @Test
    void shouldThrowWhenMultipleMatchingCourtLocationsFound() {
        LocationRefData match = location("Site", "England", "123");
        when(locationReferenceDataApiClient.getCourtVenueByLocationCode(any(), eq(AUTH), any(), any(), any(), any()))
            .thenReturn(List.of(match, match.toBuilder().build()));

        assertThatThrownBy(() -> service.getCourtLocation(AUTH, "123"))
            .isInstanceOf(LocationRefDataException.class);
    }

    @Test
    void shouldReturnDisplayEntries() {
        LocationRefData location = location("Site", "Wales", "123").toBuilder()
            .welshSiteName("Welsh Site")
            .courtAddress("Address")
            .postcode("PC1")
            .build();

        assertThat(LocationReferenceDataService.getDisplayEntry(location))
            .isEqualTo("Site - Address - PC1");
        assertThat(LocationReferenceDataService.getDisplayEntryWelsh(location))
            .isEqualTo("Welsh Site - Address - PC1");
    }

    @Test
    void shouldDelegateToEpimmsLookup() {
        List<LocationRefData> expected = List.of(location("Site", "England", "1"));
        when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(any(), eq(AUTH), eq("epimms"), any()))
            .thenReturn(expected);

        assertThat(service.getCourtLocationsByEpimmsId(AUTH, "epimms")).isEqualTo(expected);
    }

    @Test
    void shouldReturnHearingCourtLocations() {
        List<LocationRefData> expected = List.of(location("Site", "England", "1"));
        when(locationReferenceDataApiClient.getHearingVenue(any(), eq(AUTH), any(), any(), any()))
            .thenReturn(expected);

        assertThat(service.getHearingCourtLocations(AUTH)).isEqualTo(expected);
        verify(locationReferenceDataApiClient)
            .getHearingVenue(any(), eq(AUTH), any(), any(), any());
    }

    @Test
    void shouldReturnEmptyListWhenHearingLookupFails() {
        when(locationReferenceDataApiClient.getHearingVenue(any(), eq(AUTH), any(), any(), any()))
            .thenThrow(new RuntimeException("err"));

        assertThat(service.getHearingCourtLocations(AUTH)).isEmpty();
    }

    @Test
    void shouldRetrieveDefaultJudgmentLocations() {
        List<LocationRefData> expected = List.of(location("Default", "England", "001"));
        when(locationReferenceDataApiClient.getCourtVenue(any(), eq(AUTH), any(), any(), any(), any()))
            .thenReturn(expected);

        assertThat(service.getCourtLocationsForDefaultJudgments(AUTH)).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyListWhenDefaultJudgmentLookupFails() {
        when(locationReferenceDataApiClient.getCourtVenue(any(), eq(AUTH), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("err"));

        assertThat(service.getCourtLocationsForDefaultJudgments(AUTH)).isEmpty();
    }

    @Test
    void shouldReturnLocationsByEpimmsIdAndCourtType() {
        List<LocationRefData> expected = List.of(location("Site", "England", "123"));
        when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(any(), eq(AUTH), eq("EP1"), any()))
            .thenReturn(expected);

        assertThat(service.getCourtLocationsByEpimmsIdAndCourtType(AUTH, "EP1")).isEqualTo(expected);
    }

    @Test
    void shouldReturnLocationsByEpimmsIdWithCml() {
        List<LocationRefData> expected = List.of(location("Site", "England", "123"));
        when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdWithCMLAndCourtType(
            any(), eq(AUTH), eq("EP2"), any(), any(), any()))
            .thenReturn(expected);

        assertThat(service.getCourtLocationsByEpimmsIdWithCML(AUTH, "EP2")).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyListWhenEpimmsLookupFails() {
        when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdWithCMLAndCourtType(
            any(), eq(AUTH), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("error"));

        assertThat(service.getCourtLocationsByEpimmsIdWithCML(AUTH, "EP3")).isEmpty();
    }

    private LocationRefData location(String site, String region, String code) {
        return LocationRefData.builder()
            .siteName(site)
            .welshSiteName(site + "-welsh")
            .courtAddress("address")
            .postcode("pc")
            .region(region)
            .courtLocationCode(code)
            .build();
    }
}
