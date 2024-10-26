package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;

@ExtendWith(MockitoExtension.class)
class LocationReferenceDataServiceTest {

    @Mock
    private LocationReferenceDataApiClient locationReferenceDataApiClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private LocationReferenceDataService refDataService;

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

    @Nested
    class CourtLocationsForGeneralApplicationTest {
        @Test
        void shouldReturnLocations_whenLRDReturnsAllLocationsForDefaultJudgments() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(getAllLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForGeneralApplication("user_token");

            assertThat(courtLocations).isNotNull();
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsAllLocationsForGaIfErrorThenError() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            )).thenThrow(new RuntimeException());

            List<LocationRefData> courtLocations = refDataService.getCourtLocationsForGeneralApplication("user_token");
            assertThat(courtLocations).isEmpty();
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsNonScotlandLocations() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(getNonScotlandLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForGeneralApplication("user_token");

            DynamicList courtLocationString = getLocationsFromList(courtLocations);

            assertThat(courtLocations).hasSize(12);
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
        void shouldReturnLocations_whenLRDReturnsOnlyScotlandLocations() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(getOnlyScotlandLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForGeneralApplication("user_token");

            assertThat(courtLocations).isEmpty();
        }

        private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
            return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                    .append(" - ").append(location.getCourtAddress())
                    .append(" - ").append(location.getPostcode()).toString())
                                .toList());
        }

        private List<String> locationsFromDynamicList(DynamicList dynamicList) {
            return dynamicList.getListItems().stream()
                .map(DynamicListElement::getLabel)
                .toList();
        }
    }

    @Nested
    class CourtLocationsForDefaultJudgementTest {
        @Test
        void shouldReturnLocations_whenLRDReturnsAllLocationsForDefaultJudgments() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(getAllLocationsRefDataResponse());

            List<LocationRefData> courtLocations = refDataService
                .getCourtLocationsForDefaultJudgments("user_token");

            assertThat(courtLocations).isNotNull();
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsAllLocationsForDefaultJudgmentsIfErrorThenError() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            )).thenThrow(new RuntimeException());

            List<LocationRefData> courtLocations = refDataService.getCourtLocationsForDefaultJudgments("user_token");
            assertThat(courtLocations).isEmpty();
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
            List<LocationRefData> mockedResponse = List.of(ccmccLocation);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            LocationRefData result = refDataService.getCcmccLocation("user_token");

            assertThat(result.getEpimmsId()).isEqualTo("192280");
            assertThat(result.getRegionId()).isEqualTo("4");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsOnCnbcLocationsMoreThanError() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            )).thenThrow(new RuntimeException());

            LocationRefData result = refDataService.getCnbcLocation("user_token");
            assertThat(result).isEqualTo(LocationRefData.builder().build());
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsOnCnbcLocationsMoreThan2() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192282")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CNBC").build();
            LocationRefData ccmccLocation2 = LocationRefData.builder().courtVenueId("9263").epimmsId("192281")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("NATIONAL BUSINESS CENTER")
                .venueName("CNBC").build();
            List<LocationRefData> mockedResponse = List.of(ccmccLocation, ccmccLocation2);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            LocationRefData result = refDataService.getCnbcLocation("user_token");

            assertThat(result.getEpimmsId()).isEqualTo("192282");
            assertThat(result.getRegionId()).isEqualTo("4");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsOnCnbcLocations() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CNBC").build();
            List<LocationRefData> mockedResponse = List.of(ccmccLocation);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            LocationRefData result = refDataService.getCnbcLocation("user_token");

            assertThat(result.getEpimmsId()).isEqualTo("192280");
            assertThat(result.getRegionId()).isEqualTo("4");

            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(null);

            result = refDataService.getCnbcLocation("user_token");

            assertThat(result.getEpimmsId()).isNull();
            assertThat(result.getRegionId()).isNull();

            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(List.of());

            result = refDataService.getCnbcLocation("user_token");

            assertThat(result.getEpimmsId()).isNull();
            assertThat(result.getRegionId()).isNull();
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsNullBody() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(new ArrayList<>());

            LocationRefData courtLocations = refDataService.getCcmccLocation("user_token");

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
            List<LocationRefData> mockedResponse = List.of(ccmccLocation1, ccmccLocation2);

            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            LocationRefData courtLocations = refDataService.getCcmccLocation("user_token");

            assertThat(courtLocations.getRegionId()).isIn("8", "4");
            assertThat(courtLocations.getEpimmsId()).isIn("192281", "192280");
        }

        @Test
        void shouldReturnEmptyList_whenLRDThrowsException() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByName(
                anyString(),
                anyString(),
                anyString()
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
            List<LocationRefData> mockedResponse = List.of(ccmccLocation);

            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsId("user_token", "192280");
            String prefferedCourtCode = result.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .toList().get(0).getCourtLocationCode();

            assertThat(result).isNotNull();
            assertThat(prefferedCourtCode).isEqualTo("121");
        }

        @Test
        void shouldReturnLocations_whenLRDReturnsCourtLocationByEpimmsIdAndCourtType() {
            LocationRefData ccmccLocation = LocationRefData.builder().courtVenueId("9263").epimmsId("192280")
                .siteName("site_name").regionId("4").region("North West").courtType("County Court")
                .courtTypeId("10").locationType("COURT").courtName("COUNTY COURT MONEY CLAIMS CENTRE")
                .venueName("CCMCC").courtLocationCode("121").build();
            List<LocationRefData> mockedResponse = List.of(ccmccLocation);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsIdAndCourtType(
                "user_token",
                "192280"
            );
            String prefferedCourtCode = result.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .toList().get(0).getCourtLocationCode();

            assertThat(result).isNotNull();
            assertThat(prefferedCourtCode).isEqualTo("121");
        }

        @Test
        void shouldReturnEmptyList_whenEpimmsIdThrowsException() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenThrow(new RestClientException("403"));

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsId("user_token", "192280");
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyList_whenEpimmsIdAndCourtTypeThrowsException() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByEpimmsIdAndType(
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenThrow(new RestClientException("403"));

            List<LocationRefData> result = refDataService.getCourtLocationsByEpimmsIdAndCourtType(
                "user_token",
                "192280"
            );
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class LocationRefMatchingLabel {

        @Test
        void whenEmpty_empty() {
            String bearer = "bearer";
            Assertions.assertTrue(refDataService.getLocationMatchingLabel(null, bearer).isEmpty());
            Assertions.assertTrue(refDataService.getLocationMatchingLabel("", bearer).isEmpty());
        }

        @Test
        void whenMatching_match() {
            when(authTokenGenerator.generate()).thenReturn("service_token");
            LocationRefData el1 = LocationRefData.builder()
                .siteName("site name")
                .courtAddress("court address")
                .postcode("postcode")
                .build();
            String bearer = "bearer";
            when(locationReferenceDataApiClient.getHearingVenue(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(Collections.singletonList(el1));

            Optional<LocationRefData> opt = refDataService.getLocationMatchingLabel(
                LocationReferenceDataService.getDisplayEntry(el1),
                bearer
            );
            Assertions.assertTrue(opt.isPresent());
            assertEquals(el1.getSiteName(), opt.get().getSiteName());
            assertEquals(el1.getCourtAddress(), opt.get().getCourtAddress());
            assertEquals(el1.getPostcode(), opt.get().getPostcode());
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

            List<LocationRefData> mockedResponse = List.of(ccmccLocation);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByLocationCode(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);

            LocationRefData result = refDataService.getCourtLocation("user_token", "10");

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
            List<LocationRefData> mockedResponse =
                List.of(ccmccLocation, ccmccLocationDuplicate);
            when(authTokenGenerator.generate()).thenReturn("service_token");
            when(locationReferenceDataApiClient.getCourtVenueByLocationCode(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            ))
                .thenReturn(mockedResponse);
            assertThrows(
                LocationRefDataException.class,
                () -> refDataService.getCourtLocation("user_token", "10")
            );
        }
    }

}
