package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.referencedata.LRDConfiguration;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;

@SpringBootTest(classes = {GeneralAppFeesService.class, RestTemplate.class, GeneralAppFeesConfiguration.class})
class LocationRefDataServiceTest {

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LRDConfiguration lrdConfiguration;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private LocationRefDataService refDataService;

    @BeforeEach
    void setUp() {
        when(lrdConfiguration.getUrl()).thenReturn("dummy_url");
        when(lrdConfiguration.getEndpoint()).thenReturn("/fees-register/fees/lookup");
    }

    private ResponseEntity<List<LocationRefData>> getAllLocationsRefDataResponse() {
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

        return new ResponseEntity<List<LocationRefData>>(responseData, OK);
    }

    private ResponseEntity<List<LocationRefData>> getNonScotlandLocationsRefDataResponse() {
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

        return new ResponseEntity<List<LocationRefData>>(responseData, OK);
    }

    private ResponseEntity<List<LocationRefData>> getOnlyScotlandLocationsRefDataResponse() {
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

        return new ResponseEntity<List<LocationRefData>>(responseData, OK);
    }

    private LocationRefData getLocationRefData(String siteName, String region, String postcode, String courtAddress) {
        return LocationRefData.builder().siteName(siteName).region(region)
            .postcode(postcode).courtAddress(courtAddress).build();
    }

    @Nested
    class CourtLocationsForGeneralApplicationTest {
        @Test
        void shouldReturnLocations_whenLRDReturnsAllLocationsForDefaultJudgments() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(getAllLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForGeneralApplication("user_token");

            assertThat(courtLocations).isNotNull();
            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsNonScotlandLocations() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(getNonScotlandLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForGeneralApplication("user_token");

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
            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString())
                .isEqualTo(
                    "dummy_url/fees-register/fees/lookup?is_hearing_location=Y&is_case_management_location=Y&court_type_id=10&location_type=Court");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsOnlyScotlandLocations() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(getOnlyScotlandLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForGeneralApplication("user_token");

            assertThat(courtLocations.size()).isEqualTo(0);
            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString())
                .isEqualTo(
                    "dummy_url/fees-register/fees/lookup?is_hearing_location=Y&is_case_management_location=Y&court_type_id=10&location_type=Court");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
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
    }

    @Nested
    class CourtLocationsForDefaultJudgementTest {
        @Test
        void shouldReturnLocations_whenLRDReturnsAllLocationsForDefaultJudgments() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(getAllLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForDefaultJudgments("user_token");

            assertThat(courtLocations).isNotNull();
            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
        }
    }

    @Nested
    class CcmccLocationProviderTest {

        @Test
        void shouldReturnLocations_whenLRDReturnsOneCcmccLocations() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").build();
            ResponseEntity<List<LocationRefData>> mockedResponse = new ResponseEntity<>(List.of(ccmccLocation), OK);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(mockedResponse);

            LocationRefData result = refDataService.getCcmccLocation("user_token");

            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString()).isEqualTo(
                "dummy_url/fees-register/fees/lookup?court_venue_name=County%20Court%20Money%20Claims%20Centre");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
            assertThat(result.getEpimmsId()).isEqualTo("192280");
            assertThat(result.getRegionId()).isEqualTo("4");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsNullBody() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(new ResponseEntity<List<LocationRefData>>(OK));

            LocationRefData courtLocations = refDataService.getCcmccLocation("user_token");

            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString()).isEqualTo(
                "dummy_url/fees-register/fees/lookup?court_venue_name=County%20Court%20Money%20Claims%20Centre");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
            assertThat(courtLocations.getRegionId()).isNull();
            assertThat(courtLocations.getEpimmsId()).isNull();
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsMultipleCcmccLocations() {
            LocationRefData ccmccLocation1 = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").build();
            LocationRefData ccmccLocation2 = LocationRefData.builder().courtVenueId("9264").epimmsId("192281")
                .siteName("site_name").regionId("8").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").build();
            ResponseEntity<List<LocationRefData>> mockedResponse =
                new ResponseEntity<>(List.of(ccmccLocation1, ccmccLocation2), OK);

            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(mockedResponse);

            LocationRefData courtLocations = refDataService.getCcmccLocation("user_token");

            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString()).isEqualTo(
                "dummy_url/fees-register/fees/lookup?court_venue_name=County%20Court%20Money%20Claims%20Centre");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization"))
                .isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
            assertThat(courtLocations.getRegionId()).isIn("8", "4");
            assertThat(courtLocations.getEpimmsId()).isIn("192281", "192280");
        }

        @Test
        void shouldReturnEmptyList_whenLRDThrowsException() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenThrow(new RestClientException("403"));

            LocationRefData courtLocations = refDataService.getCcmccLocation("user_token");

            assertThat(courtLocations.getRegionId()).isNull();
            assertThat(courtLocations.getEpimmsId()).isNull();
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsCourtLocationByEpimmsId() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").courtLocationCode("121").build();
            ResponseEntity<List<LocationRefData>> mockedResponse = new ResponseEntity<>(List.of(ccmccLocation), OK);

            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()))
                .thenReturn(mockedResponse);

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsId("user_token", "192280");
            String prefferedCourtCode = result.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .collect(Collectors.toList()).get(0).getCourtLocationCode();

            assertThat(result).isNotNull();
            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString()).isEqualTo(
                "dummy_url/fees-register/fees/lookup?epimms_id=192280");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
            assertThat(prefferedCourtCode).isEqualTo("121");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsCourtLocationByEpimmsIdAndCourtType() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").courtLocationCode("121").build();
            ResponseEntity<List<LocationRefData>> mockedResponse = new ResponseEntity<>(List.of(ccmccLocation), OK);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()))
                .thenReturn(mockedResponse);

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsIdAndCourtType("user_token", "192280");
            String prefferedCourtCode = result.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .collect(Collectors.toList()).get(0).getCourtLocationCode();

            assertThat(result).isNotNull();
            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString()).isEqualTo(
                "dummy_url/fees-register/fees/lookup?epimms_id=192280&court_type_id=10");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
            assertThat(prefferedCourtCode).isEqualTo("121");
        }

        @Test
        void shouldReturnEmptyList_whenEpimmsIdThrowsException() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()))
                .thenThrow(new RestClientException("403"));

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsId("user_token", "192280");

            assertThat(result.isEmpty());
        }

        @Test
        void shouldReturnEmptyList_whenEpimmsIdAndCourtTypeThrowsException() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()))
                .thenThrow(new RestClientException("403"));

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsIdAndCourtType("user_token", "192280");

            assertThat(result.isEmpty());
        }
    }

    @Nested
    class LocationRefMatchingLabel {

        @Test
        public void whenEmpty_empty() {
            String bearer = "bearer";
            Assertions.assertTrue(refDataService.getLocationMatchingLabel(null, bearer).isEmpty());
            Assertions.assertTrue(refDataService.getLocationMatchingLabel("", bearer).isEmpty());
        }

        @Test
        public void whenMatching_match() {
            LocationRefData el1 = LocationRefData.builder()
                .siteName("site name")
                .courtAddress("court address")
                .postcode("postcode")
                .build();
            String bearer = "bearer";
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(ResponseEntity.ok(Collections.singletonList(el1)));

            Optional<LocationRefData> opt = refDataService.getLocationMatchingLabel(
                LocationRefDataService.getDisplayEntry(el1),
                bearer
            );
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertEquals(el1.getSiteName(), opt.get().getSiteName());
            Assertions.assertEquals(el1.getCourtAddress(), opt.get().getCourtAddress());
            Assertions.assertEquals(el1.getPostcode(), opt.get().getPostcode());
        }
    }

    @Nested
    class CourtLocationProviderTest {

        @Test
        void shouldReturnLocations_whenLRDReturnsOneLocations() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").courtLocationCode("10").build();

            ResponseEntity<List<LocationRefData>> mockedResponse = new ResponseEntity<>(
                List.of(ccmccLocation), OK);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(mockedResponse);

            LocationRefData result = refDataService.getCourtLocation("user_token", "10");

            verify(lrdConfiguration, times(1)).getUrl();
            verify(lrdConfiguration, times(1)).getEndpoint();
            assertThat(uriCaptor.getValue().toString()).isEqualTo(
                "dummy_url/fees-register/fees/lookup?court_type_id=10&is_case_management_location=Y" +
                    "&court_location_code=10&court_status=Open");
            assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.GET);
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("user_token");
            assertThat(httpEntityCaptor.getValue().getHeaders().getFirst("ServiceAuthorization"))
                .isEqualTo("service_token");
            assertThat(result.getEpimmsId()).isEqualTo("192280");
            assertThat(result.getRegionId()).isEqualTo("4");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsTwoLocations() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").courtLocationCode("10").build();
            LocationRefData ccmccLocationDuplicate = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").courtLocationCode("10").build();
            ResponseEntity<List<LocationRefData>> mockedResponse = new ResponseEntity<>(
                List.of(ccmccLocation, ccmccLocationDuplicate), OK);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(restTemplate.exchange(
                uriCaptor.capture(),
                httpMethodCaptor.capture(),
                httpEntityCaptor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()
            ))
                .thenReturn(mockedResponse);
            assertThrows(
                LocationRefDataException.class,
                () -> refDataService.getCourtLocation("user_token", "10")
            );
        }
    }

}
