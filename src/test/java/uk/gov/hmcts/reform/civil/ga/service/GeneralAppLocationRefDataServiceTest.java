package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
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

    @BeforeEach
    void setUp() {
        when(lrdConfiguration.getUrl()).thenReturn("dummy_url");
        when(lrdConfiguration.getEndpoint()).thenReturn("/fees-register/fees/lookup");
    }

    private List<LocationRefData> getAllLocationsRefDataResponse() {
        List<LocationRefData> responseData = new ArrayList<LocationRefData>();
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

    private List<LocationRefData> getAllLocationsRefDataResponseByEpimms() {
        List<LocationRefData> responseData = new ArrayList<LocationRefData>();
        responseData.add(getLocationRefData("site_name_01", "London", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getLocationRefDataResponseForCcmcc() {
        List<LocationRefData> responseData = new ArrayList<LocationRefData>();
        responseData.add(getLocationRefData("banana", "orange", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getLocationRefDataResponseForCnbc() {
        List<LocationRefData> responseData = new ArrayList<LocationRefData>();
        responseData.add(getLocationRefData("pineapple", "mango", "AA0 0BB",
                                            "court address 1111"
        ));

        return responseData;
    }

    private List<LocationRefData> getNonScotlandLocationsRefDataResponse() {
        List<LocationRefData> responseData = new ArrayList<LocationRefData>();
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

    private List<LocationRefData> getOnlyScotlandLocationsRefDataResponse() {
        List<LocationRefData> responseData = new ArrayList<LocationRefData>();
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

    private LocationRefData getLocationRefData(String siteName, String region, String postcode, String courtAddress) {
        return LocationRefData.builder().siteName(siteName).region(region)
            .postcode(postcode).courtAddress(courtAddress).build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .collect(Collectors.toList()));
    }

    private List<String> locationsFromDynamicList(DynamicList dynamicList) {
        return dynamicList.getListItems().stream()
            .map(DynamicListElement::getLabel)
            .collect(Collectors.toList());
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsAllLocations() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString()
        )).thenReturn(getAllLocationsRefDataResponse());

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocations("user_token");

        DynamicList courtLocationString = getLocationsFromList(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "site_name_01 - court address 1111 - AA0 0BB",
                "site_name_02 - court address 2222 - AA0 0BB",
                "site_name_03 - court address 3333 - AA0 0BB",
                "site_name_04 - court address 4444 - AA0 0BB",
                "site_name_05 - court address 5555 - AA0 0BB",
                "site_name_06 - court address 6666 - AA0 0BB",
                "site_name_07 - court address 7777 - AA0 0BB",
                "site_name_08 - court address 8888 - AA0 0BB",
                "site_name_09 - court address 9999 - AA0 0BB",
                "site_name_10 - court address 1001 - AA0 0BB"
            );

        assertThat(courtLocations.size()).isEqualTo(10);
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsNullBody() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString()
        )).thenReturn(new ArrayList<>());

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocations("user_token");

        assertThat(courtLocations).isEmpty();
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsOnlyScotlandLocations() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString()
        )).thenReturn(getOnlyScotlandLocationsRefDataResponse());

        List<LocationRefData> courtLocations = refDataService.getCourtLocations("user_token");

        assertThat(courtLocations.size()).isEqualTo(0);
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsNonScotlandLocations() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString()
        )).thenReturn(getNonScotlandLocationsRefDataResponse());

        List<LocationRefData> courtLocations = refDataService.getCourtLocations("user_token");

        DynamicList courtLocationString = getLocationsFromList(courtLocations);

        assertThat(courtLocations.size()).isEqualTo(12);
        assertThat(locationsFromDynamicList(courtLocationString)).containsOnly(
            "site_name_01 - court address 1111 - AA0 0BB",
            "site_name_02 - court address 2222 - AA0 0BB",
            "site_name_03 - court address 3333 - AA0 0BB",
            "site_name_04 - court address 4444 - AA0 0BB",
            "site_name_05 - court address 5555 - AA0 0BB",
            "site_name_06 - court address 6666 - AA0 0BB",
            "site_name_07 - court address 7777 - AA0 0BB",
            "site_name_08 - court address 8888 - AA0 0BB",
            "site_name_09 - court address 9999 - AA0 0BB",
            "site_name_10 - court address 1001 - AA0 0BB",
            "site_name_11 - court address 1011 - AA0 0BB",
            "site_name_12 - court address 1012 - AA0 0BB"
        );
    }

    @Test
    void shouldReturnEmptyList_whenLRDThrowsException() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCMLAndHLCourts(
            anyString(),
            anyString()
        )).thenThrow(new RestClientException("403"));

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocations("user_token");

        assertThat(courtLocations.size()).isEqualTo(0);
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsAllLocationsByEpimmsId() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCourtByEpimmsId(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getAllLocationsRefDataResponseByEpimms());

        List<LocationRefData> courtLocations = refDataService
            .getCourtLocationsByEpimmsId("user_token", "00000");

        DynamicList courtLocationString = getLocationsFromList(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "site_name_01 - court address 1111 - AA0 0BB"
            );

        assertThat(courtLocations.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsCcmcc() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCourtVenueByName(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getLocationRefDataResponseForCcmcc());

        List<LocationRefData> courtLocations = refDataService
            .getCcmccLocation("user_token");

        DynamicList courtLocationString = getLocationsFromList(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "banana - court address 1111 - AA0 0BB"
            );

        assertThat(courtLocations).hasSize(1);
    }

    @Test
    void shouldReturnLocations_whenLRDReturnsCnbc() {
        when(authTokenGenerator.generate()).thenReturn("service_token");
        when(courtVenueService.getCourtVenueByName(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(getLocationRefDataResponseForCnbc());

        List<LocationRefData> courtLocations = refDataService
            .getCnbcLocation("user_token");

        DynamicList courtLocationString = getLocationsFromList(courtLocations);

        assertThat(locationsFromDynamicList(courtLocationString))
            .containsOnly(
                "pineapple - court address 1111 - AA0 0BB"
            );

        assertThat(courtLocations).hasSize(1);
    }
}
