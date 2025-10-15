package uk.gov.hmcts.reform.civil.referencedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationRefDataServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LRDConfiguration lrdConfiguration;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ResponseEntity<List<LocationRefData>> responseEntity;

    @Mock
    private Logger mockLogger;

    @InjectMocks
    private LocationRefDataService locationRefDataService;

    private static final String AUTH_TOKEN = "Bearer test-token";
    private static final String SERVICE_AUTH_TOKEN = "service-auth-token";
    private static final String BASE_URL = "http://test-url";
    private static final String ENDPOINT = "/locations";

    private LocationRefData testLocation;
    private LocationRefData testLocationScotland;
    private List<LocationRefData> testLocations;

    @BeforeEach
    void setUp() {
        // Only set up common test data - no mocking here to avoid unnecessary stubs
        testLocation = LocationRefData.builder()
            .siteName("Test Court")
            .courtAddress("123 Test Street")
            .postcode("TE1 2ST")
            .region("England")
            .courtLocationCode("123")
            .build();

        testLocationScotland = LocationRefData.builder()
            .siteName("Scotland Court")
            .courtAddress("456 Scotland Street")
            .postcode("SC1 2OT")
            .region("Scotland")
            .build();

        testLocations = Arrays.asList(testLocation, testLocationScotland);
    }

    @Test
    void getCcmccLocation_Success_SingleLocation() {
        // Arrange
        List<LocationRefData> ccmccLocations = Collections.singletonList(testLocation);
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(ccmccLocations);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        LocationRefData result = locationRefDataService.getCcmccLocation(AUTH_TOKEN);

        // Assert
        assertEquals(testLocation, result);
        verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                      ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any());
        verify(authTokenGenerator).generate();
        verify(lrdConfiguration).getUrl();
        verify(lrdConfiguration).getEndpoint();
    }

    @Test
    void getDisplayEntry_Success() {
        // Arrange
        // No mocking needed for static method test

        // Act
        String result = LocationRefDataService.getDisplayEntry(testLocation);

        // Assert
        assertEquals("Test Court - 123 Test Street - TE1 2ST", result);
    }

    @Test
    void getHeaders_Success() {
        // Arrange
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        LocationRefDataService service = new LocationRefDataService(restTemplate, lrdConfiguration, authTokenGenerator);

        // Act
        HttpEntity<String> result = service.getHeaders(AUTH_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(AUTH_TOKEN, result.getHeaders().getFirst("Authorization"));
        assertEquals(SERVICE_AUTH_TOKEN, result.getHeaders().getFirst("ServiceAuthorization"));
        verify(authTokenGenerator).generate();
    }

    // Additional tests with proper mocking in each test method
    @Test
    void getCcmccLocation_Success_MultipleLocations() {
        // Arrange
        List<LocationRefData> ccmccLocations = Arrays.asList(testLocation, testLocation);
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(ccmccLocations);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        LocationRefData result = locationRefDataService.getCcmccLocation(AUTH_TOKEN);

        // Assert
        assertEquals(testLocation, result);
    }

    @Test
    void getCcmccLocation_EmptyList() {
        // Arrange
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(new ArrayList<>());
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        LocationRefData result = locationRefDataService.getCcmccLocation(AUTH_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(LocationRefData.builder().build(), result);
    }

    @Test
    void getCcmccLocation_NullResponse() {
        // Arrange
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(null);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        LocationRefData result = locationRefDataService.getCcmccLocation(AUTH_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(LocationRefData.builder().build(), result);
    }

    @Test
    void getCcmccLocation_Exception() {
        // Arrange
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any()))
            .thenThrow(new RuntimeException("Test exception"));

        // Act
        LocationRefData result = locationRefDataService.getCcmccLocation(AUTH_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(LocationRefData.builder().build(), result);
    }

    @Test
    void getCnbcLocation_Success_SingleLocation() {
        // Arrange
        List<LocationRefData> cnbcLocations = Collections.singletonList(testLocation);
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(cnbcLocations);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        LocationRefData result = locationRefDataService.getCnbcLocation(AUTH_TOKEN);

        // Assert
        assertEquals(testLocation, result);
    }

    @Test
    void getCourtLocationsForDefaultJudgments_Success() {
        // Arrange
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(testLocations);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        List<LocationRefData> result = locationRefDataService.getCourtLocationsForDefaultJudgments(AUTH_TOKEN);

        // Assert
        assertEquals(testLocations, result);
    }

    @Test
    void getCourtLocationsForGeneralApplication_Success() {
        // Arrange
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(testLocations);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        List<LocationRefData> result = locationRefDataService.getCourtLocationsForGeneralApplication(AUTH_TOKEN);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testLocation, result.get(0));
        assertFalse(result.contains(testLocationScotland));
    }

    @Test
    void getLocationMatchingLabel_BlankLabel() {
        // Arrange
        // No mocking needed for blank label test

        // Act
        Optional<LocationRefData> result = locationRefDataService.getLocationMatchingLabel("", AUTH_TOKEN);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getLocationMatchingLabel_NullLabel() {
        // Arrange
        // No mocking needed for null label test

        // Act
        Optional<LocationRefData> result = locationRefDataService.getLocationMatchingLabel(null, AUTH_TOKEN);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getLocationMatchingLabel_Success() {
        // Arrange
        String expectedLabel = "Test Court - 123 Test Street - TE1 2ST";
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(Collections.singletonList(testLocation));
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        Optional<LocationRefData> result = locationRefDataService.getLocationMatchingLabel(expectedLabel, AUTH_TOKEN);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLocation, result.get());
    }

    @Test
    void getCourtLocation_Success() {
        // Arrange
        List<LocationRefData> locations = Collections.singletonList(testLocation);
        when(lrdConfiguration.getUrl()).thenReturn(BASE_URL);
        when(lrdConfiguration.getEndpoint()).thenReturn(ENDPOINT);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(responseEntity.getBody()).thenReturn(locations);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                                   ArgumentMatchers.<ParameterizedTypeReference<List<LocationRefData>>>any())).thenReturn(responseEntity);

        // Act
        LocationRefData result = locationRefDataService.getCourtLocation(AUTH_TOKEN, "123");

        // Assert
        assertEquals(testLocation, result);
    }

    @Test
    void getLocationRefData_Success() {
        // Arrange
        List<LocationRefData> locations = Collections.singletonList(testLocation);

        // Act
        LocationRefData result = LocationRefDataService.getLocationRefData(locations, "123", mockLogger);

        // Assert
        assertEquals(testLocation, result);
    }

    @Test
    void getLocationRefData_NoLocationFound() {
        // Arrange
        List<LocationRefData> locations = Collections.singletonList(testLocation);

        // Act & Assert
        LocationRefDataException exception = assertThrows(LocationRefDataException.class, () ->
            LocationRefDataService.getLocationRefData(locations, "999", mockLogger));

        assertEquals("No court Location Found for three digit court code : 999", exception.getMessage());
        verify(mockLogger).warn("No court Location Found for three digit court code : {}", "999");
    }

    @Test
    void onlyEnglandAndWalesLocations_FilterScotland() {
        // Arrange
        LocationRefDataService service = new LocationRefDataService(restTemplate, lrdConfiguration, authTokenGenerator);

        // Act
        List<LocationRefData> result = service.onlyEnglandAndWalesLocations(testLocations);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testLocation, result.get(0));
        assertFalse(result.contains(testLocationScotland));
    }

    @Test
    void onlyEnglandAndWalesLocations_NullInput() {
        // Arrange
        LocationRefDataService service = new LocationRefDataService(restTemplate, lrdConfiguration, authTokenGenerator);

        // Act
        List<LocationRefData> result = service.onlyEnglandAndWalesLocations(null);

        // Assert
        assertTrue(result.isEmpty());
    }
}
