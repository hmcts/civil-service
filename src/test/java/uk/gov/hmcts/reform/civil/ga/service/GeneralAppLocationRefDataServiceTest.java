package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.ga.config.GeneralAppLRDConfiguration;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.refdata.CourtVenueService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@ExtendWith(MockitoExtension.class)
class GeneralAppLocationRefDataServiceTest {

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    @Mock
    private CourtVenueService courtVenueService;

    @Mock
    private GeneralAppLRDConfiguration lrdConfiguration;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private GeneralAppLocationRefDataService refDataService;

    private List<LocationRefData> getAllLocationsRefDataResponse() {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", "London", "AA0 0BB",
                                            "court address 1111"
        ));
        responseData.add(getLocationRefData("site_name_02", "London", "AA0 0BB",
                                            "court address 2222"
        ));
        responseData.add(getLocationRefData("site_name_03", "Midlands", "AA0 0BB",
                                            "court address 3333"
        ));
        responseData.add(getLocationRefData("site_name_04", "Midlands", "AA0 0BB",
                                            "court address 4444"
        ));
        responseData.add(getLocationRefData("site_name_05", "North East", "AA0 0BB",
                                            "court address 5555"
        ));
        responseData.add(getLocationRefData("site_name_06", "South East", "AA0 0BB",
                                            "court address 6666"
        ));
        responseData.add(getLocationRefData("site_name_07", "North West", "AA0 0BB",
                                            "court address 7777"
        ));
        responseData.add(getLocationRefData("site_name_08", "South West", "AA0 0BB",
                                            "court address 8888"
        ));
        responseData.add(getLocationRefData("site_name_09", "Wales", "AA0 0BB",
                                            "court address 9999"
        ));
        responseData.add(getLocationRefData("site_name_10", "London", "AA0 0BB",
                                            "court address 1001"
        ));
        responseData.add(getLocationRefData("site_name_11", "Scotland", "AA0 0BB",
                                            "court address 1011"
        ));
        responseData.add(getLocationRefData("site_name_12", "Scotland", "AA0 0BB",
                                            "court address 1012"
        ));

        return responseData;
    }

    private List<LocationRefData> getAllLocationsRefDataResponse(String serviceId) {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", serviceId, "London", "AA0 0BB",
                                            "court address 1111"
        ));
        responseData.add(getLocationRefData("site_name_02", serviceId, "London", "AA0 0BB",
                                            "court address 2222"
        ));
        responseData.add(getLocationRefData("site_name_03", serviceId, "Midlands", "AA0 0BB",
                                            "court address 3333"
        ));
        responseData.add(getLocationRefData("site_name_04", serviceId,  "Midlands", "AA0 0BB",
                                            "court address 4444"
        ));
        responseData.add(getLocationRefData("site_name_05", serviceId, "North East", "AA0 0BB",
                                            "court address 5555"
        ));
        responseData.add(getLocationRefData("site_name_06", serviceId, "South East", "AA0 0BB",
                                            "court address 6666"
        ));
        responseData.add(getLocationRefData("site_name_07", serviceId, "North West", "AA0 0BB",
                                            "court address 7777"
        ));
        responseData.add(getLocationRefData("site_name_08", serviceId, "South West", "AA0 0BB",
                                            "court address 8888"
        ));
        responseData.add(getLocationRefData("site_name_09", serviceId, "Wales", "AA0 0BB",
                                            "court address 9999"
        ));
        responseData.add(getLocationRefData("site_name_10", serviceId, "London", "AA0 0BB",
                                            "court address 1001"
        ));
        responseData.add(getLocationRefData("site_name_11", serviceId, "Scotland", "AA0 0BB",
                                            "court address 1011"
        ));
        responseData.add(getLocationRefData("site_name_12", serviceId, "Scotland", "AA0 0BB",
                                            "court address 1012"
        ));

        return responseData;
    }

    private List<LocationRefData> getAllLocationsRefDataResponseByEpimms() {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", "London", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getAllLocationsRefDataResponseByEpimms(String serviceId) {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", serviceId, "London", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getLocationRefDataResponseForCcmcc() {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("banana", "orange", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getLocationRefDataResponseForCcmcc(String serviceId) {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("banana", serviceId, "orange", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getLocationRefDataResponseForCnbc() {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("pineapple", "mango", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getLocationRefDataResponseForCnbc(String serviceId) {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("pineapple", serviceId, "mango", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getNonScotlandLocationsRefDataResponse() {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", "London", "AA0 0BB",
                                            "court address 1111"
        ));
        responseData.add(getLocationRefData("site_name_02", "London", "AA0 0BB",
                                            "court address 2222"
        ));
        responseData.add(getLocationRefData("site_name_03", "Midlands", "AA0 0BB",
                                            "court address 3333"
        ));
        responseData.add(getLocationRefData("site_name_04", "Midlands", "AA0 0BB",
                                            "court address 4444"
        ));
        responseData.add(getLocationRefData("site_name_05", "North East", "AA0 0BB",
                                            "court address 5555"
        ));
        responseData.add(getLocationRefData("site_name_06", "South East", "AA0 0BB",
                                            "court address 6666"
        ));
        responseData.add(getLocationRefData("site_name_07", "North West", "AA0 0BB",
                                            "court address 7777"
        ));
        responseData.add(getLocationRefData("site_name_08", "South West", "AA0 0BB",
                                            "court address 8888"
        ));
        responseData.add(getLocationRefData("site_name_09", "Wales", "AA0 0BB",
                                            "court address 9999"
        ));
        responseData.add(getLocationRefData("site_name_10", "London", "AA0 0BB",
                                            "court address 1001"
        ));
        responseData.add(getLocationRefData("site_name_11", "Midlands", "AA0 0BB",
                                            "court address 1011"
        ));
        responseData.add(getLocationRefData("site_name_12", "Wales", "AA0 0BB",
                                            "court address 1012"
        ));

        return responseData;
    }

    private List<LocationRefData> getNonScotlandLocationsRefDataResponse(String serviceId) {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", serviceId, "London", "AA0 0BB",
                                            "court address 1111"
        ));
        responseData.add(getLocationRefData("site_name_02", serviceId, "London", "AA0 0BB",
                                            "court address 2222"
        ));
        responseData.add(getLocationRefData("site_name_03", serviceId, "Midlands", "AA0 0BB",
                                            "court address 3333"
        ));
        responseData.add(getLocationRefData("site_name_04", serviceId, "Midlands", "AA0 0BB",
                                            "court address 4444"
        ));
        responseData.add(getLocationRefData("site_name_05", serviceId, "North East", "AA0 0BB",
                                            "court address 5555"
        ));
        responseData.add(getLocationRefData("site_name_06", serviceId, "South East", "AA0 0BB",
                                            "court address 6666"
        ));
        responseData.add(getLocationRefData("site_name_07", serviceId, "North West", "AA0 0BB",
                                            "court address 7777"
        ));
        responseData.add(getLocationRefData("site_name_08", serviceId, "South West", "AA0 0BB",
                                            "court address 8888"
        ));
        responseData.add(getLocationRefData("site_name_09", serviceId, "Wales", "AA0 0BB",
                                            "court address 9999"
        ));
        responseData.add(getLocationRefData("site_name_10", serviceId,  "London", "AA0 0BB",
                                            "court address 1001"
        ));
        responseData.add(getLocationRefData("site_name_11", serviceId, "Midlands", "AA0 0BB",
                                            "court address 1011"
        ));
        responseData.add(getLocationRefData("site_name_12", serviceId, "Wales", "AA0 0BB",
                                            "court address 1012"
        ));

        return responseData;
    }

    private List<LocationRefData> getOnlyScotlandLocationsRefDataResponse() {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", "Scotland", "AA0 0BB",
                                            "court address 1111"
        ));
        responseData.add(getLocationRefData("site_name_02", "Scotland", "AA0 0BB",
                                            "court address 2222"
        ));
        responseData.add(getLocationRefData("site_name_03", "Scotland", "AA0 0BB",
                                            "court address 3333"
        ));
        responseData.add(getLocationRefData("site_name_04", "Scotland", "AA0 0BB",
                                            "court address 4444"
        ));
        responseData.add(getLocationRefData("site_name_05", "Scotland", "AA0 0BB",
                                            "court address 5555"
        ));

        return responseData;
    }

    private List<LocationRefData> getOnlyScotlandLocationsRefDataResponse(String serviceId) {
        List<LocationRefData> responseData = new ArrayList<>();
        responseData.add(getLocationRefData("site_name_01", serviceId, "Scotland", "AA0 0BB",
                                            "court address 1111"
        ));
        responseData.add(getLocationRefData("site_name_02", serviceId, "Scotland", "AA0 0BB",
                                            "court address 2222"
        ));
        responseData.add(getLocationRefData("site_name_03", serviceId, "Scotland", "AA0 0BB",
                                            "court address 3333"
        ));
        responseData.add(getLocationRefData("site_name_04", serviceId, "Scotland", "AA0 0BB",
                                            "court address 4444"
        ));
        responseData.add(getLocationRefData("site_name_05", serviceId, "Scotland", "AA0 0BB",
                                            "court address 5555"
        ));

        return responseData;
    }

    private LocationRefData getLocationRefData(String siteName, String region, String postcode, String courtAddress) {
        return new LocationRefData().setSiteName(siteName).setRegion(region)
            .setPostcode(postcode).setCourtAddress(courtAddress);
    }

    private LocationRefData getLocationRefData(String siteName, String serviceId, String region, String postcode, String courtAddress) {
        return new LocationRefData().setSiteName(siteName).setServiceId(serviceId).setRegion(region)
            .setPostcode(postcode).setCourtAddress(courtAddress);
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> location.getSiteName() +
                " - " + location.getCourtAddress() +
                " - " + location.getPostcode())
                            .toList());
    }

    private DynamicList getLocationsFromListWithServiceId(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> location.getSiteName() +
                " - " + location.getServiceId() +
                " - " + location.getCourtAddress() +
                " - " + location.getPostcode())
                            .toList());
    }

    private List<String> locationsFromDynamicList(DynamicList dynamicList) {
        return dynamicList.getListItems().stream()
            .map(DynamicListElement::getLabel)
            .toList();
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsAllLocations(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getAllLocationsRefDataResponse(serviceId));

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocations("user_token", serviceId);

        DynamicList courtLocationString = getLocationsFromListWithServiceId(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly("site_name_01 - " + serviceId + " - court address 1111 - AA0 0BB",
                          "site_name_02 - " + serviceId + " - court address 2222 - AA0 0BB",
                          "site_name_03 - " + serviceId + " - court address 3333 - AA0 0BB",
                          "site_name_04 - " + serviceId + " - court address 4444 - AA0 0BB",
                          "site_name_05 - " + serviceId + " - court address 5555 - AA0 0BB",
                          "site_name_06 - " + serviceId + " - court address 6666 - AA0 0BB",
                          "site_name_07 - " + serviceId + " - court address 7777 - AA0 0BB",
                          "site_name_08 - " + serviceId + " - court address 8888 - AA0 0BB",
                          "site_name_09 - " + serviceId + " - court address 9999 - AA0 0BB",
                          "site_name_10 - " + serviceId + " - court address 1001 - AA0 0BB"
            );

        assertThat(courtLocations).hasSize(10);
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsNullBody(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(new ArrayList<>());

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocations("user_token", serviceId);

        assertThat(courtLocations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsOnlyScotlandLocations(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getOnlyScotlandLocationsRefDataResponse(serviceId));

        List<LocationRefData> courtLocations = refDataService.getCourtLocations("user_token", serviceId);

        assertThat(courtLocations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsNonScotlandLocations(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getNonScotlandLocationsRefDataResponse(serviceId));

        List<LocationRefData> courtLocations = refDataService.getCourtLocations("user_token", serviceId);

        DynamicList courtLocationString = getLocationsFromListWithServiceId(courtLocations);

        assertThat(courtLocations).hasSize(12);
        assertThat(locationsFromDynamicList(courtLocationString)).containsOnly(
            "site_name_01 - " + serviceId + " - court address 1111 - AA0 0BB",
            "site_name_02 - " + serviceId + " - court address 2222 - AA0 0BB",
            "site_name_03 - " + serviceId + " - court address 3333 - AA0 0BB",
            "site_name_04 - " + serviceId + " - court address 4444 - AA0 0BB",
            "site_name_05 - " + serviceId + " - court address 5555 - AA0 0BB",
            "site_name_06 - " + serviceId + " - court address 6666 - AA0 0BB",
            "site_name_07 - " + serviceId + " - court address 7777 - AA0 0BB",
            "site_name_08 - " + serviceId + " - court address 8888 - AA0 0BB",
            "site_name_09 - " + serviceId + " - court address 9999 - AA0 0BB",
            "site_name_10 - " + serviceId + " - court address 1001 - AA0 0BB",
            "site_name_11 - " + serviceId + " - court address 1011 - AA0 0BB",
            "site_name_12 - " + serviceId + " - court address 1012 - AA0 0BB"
        );
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnEmptyList_whenLRDThrowsException(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString(),
            anyString()
        )).thenThrow(new RestClientException("403"));

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocations("user_token", serviceId);

        assertThat(courtLocations).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsAllLocationsByEpimmsId(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCourtByEpimmsId(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getAllLocationsRefDataResponseByEpimms(serviceId));

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocationsByEpimmsId("user_token", "00000", serviceId);

        DynamicList courtLocationString = getLocationsFromListWithServiceId(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "site_name_01 - " + serviceId + " - court address 1111 - AA0 0BB"
            );

        assertThat(courtLocations).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsCcmcc(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCourtVenueByName(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getLocationRefDataResponseForCcmcc(serviceId));

        List<LocationRefData> courtLocations = refDataService
            .getCcmccLocation("user_token", serviceId);

        DynamicList courtLocationString = getLocationsFromListWithServiceId(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "banana - " + serviceId + " - court address 1111 - AA0 0BB"
            );

        assertThat(courtLocations).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"AAA6", "AAA7"})
    void shouldReturnLocations_whenLRDReturnsCnbc(String serviceId) {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCourtVenueByName(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getLocationRefDataResponseForCnbc(serviceId));

        List<LocationRefData> courtLocations = refDataService
            .getCnbcLocation("user_token", serviceId);

        DynamicList courtLocationString = getLocationsFromListWithServiceId(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "pineapple - " + serviceId + " - court address 1111 - AA0 0BB"
            );

        assertThat(courtLocations).hasSize(1);
    }
}
