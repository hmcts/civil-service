package uk.gov.hmcts.reform.civil.postcode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.exceptions.PostcodeLookupException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostcodeLookupServiceTest {

    private static final String VALID_URL = "https://api.ordnancesurvey.co.uk/opennames/v1/find";
    private static final String API_KEY = "test-api-key";
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PostcodeLookupConfiguration postcodeLookupConfiguration;
    private PostcodeLookupService postcodeLookupService;

    @BeforeEach
    void setUp() {
        postcodeLookupService = new PostcodeLookupService(restTemplate, postcodeLookupConfiguration);
    }

    private void setupMocks(String responseBody, HttpStatus status) {
        when(postcodeLookupConfiguration.getUrl()).thenReturn(VALID_URL);
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn(API_KEY);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, status);
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(responseEntity);
    }

    private void verifyRestTemplateCall() {
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    private String createSuccessResponse(String postcode, String country) {
        String cleanPostcode = postcode.replaceAll("\\s+", "");
        return String.format(
            """
                {
                    "results": [
                        {
                            "GAZETTEER_ENTRY": {
                                "NAME1": "%s",
                                "COUNTRY": "%s"
                            }
                        }
                    ]
                }
                """, cleanPostcode, country
        );
    }

    @Nested
    class ValidatePostCodeForDefendant {

        @Test
        void shouldReturnTrue_whenCountryIsEngland() {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = createSuccessResponse(postcode, "England");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isTrue();
            verifyRestTemplateCall();
        }

        @Test
        void shouldReturnTrue_whenCountryIsWales() {
            // Arrange
            String postcode = "CF10 1AA";
            String responseJson = createSuccessResponse(postcode, "Wales");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isTrue();
            verifyRestTemplateCall();
        }

        @Test
        void shouldReturnFalse_whenCountryIsScotland() {
            // Arrange
            String postcode = "EH1 1AA";
            String responseJson = createSuccessResponse(postcode, "Scotland");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isFalse();
            verifyRestTemplateCall();
        }

        @Test
        void shouldReturnFalse_whenCountryIsNorthernIreland() {
            // Arrange
            String postcode = "BT1 1AA";
            String responseJson = createSuccessResponse(postcode, "Northern Ireland");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenPostcodeNotFound() {
            // Arrange
            String postcode = "XX1 1XX";
            setupMocks("", HttpStatus.NOT_FOUND);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenResultsArrayIsEmpty() {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = "{\"results\":[]}";
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenPostcodeDoesNotMatchResponse() {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = createSuccessResponse("SW1A 2AA", "England");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenUnexpectedStatusCode() {
            // Arrange
            String postcode = "SW1A 1AA";
            setupMocks("", HttpStatus.INTERNAL_SERVER_ERROR);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"sw1a 1aa", "SW1A 1AA", "Sw1A 1aA"})
        void shouldHandlePostcodeInAnyCaseFormat(String postcode) {
            // Arrange
            String responseJson = createSuccessResponse("SW1A1AA", "England");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        void shouldHandlePostcodeWithSpaces() {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = createSuccessResponse("SW1A1AA", "England");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        void shouldThrowException_whenUrlIsNull() {
            // Arrange
            when(postcodeLookupConfiguration.getUrl()).thenReturn(null);
            when(postcodeLookupConfiguration.getAccessKey()).thenReturn(API_KEY);

            // Act & Assert
            assertThatThrownBy(() -> postcodeLookupService.validatePostCodeForDefendant("SW1A 1AA")).isInstanceOf(
                PostcodeLookupException.class).hasMessageContaining("Postcode lookup service failed for: SW1A 1AA").hasCauseInstanceOf(
                IllegalArgumentException.class).hasRootCauseMessage("Postcode url cannot be blank or empty");
        }

        @Test
        void shouldThrowException_whenApiKeyIsBlank() {
            // Arrange
            when(postcodeLookupConfiguration.getUrl()).thenReturn(VALID_URL);
            when(postcodeLookupConfiguration.getAccessKey()).thenReturn("");

            // Act & Assert
            assertThatThrownBy(() -> postcodeLookupService.validatePostCodeForDefendant("SW1A 1AA")).isInstanceOf(
                PostcodeLookupException.class).hasMessageContaining("Postcode lookup service failed for: SW1A 1AA").hasCauseInstanceOf(
                IllegalStateException.class).hasRootCauseMessage("Postcode API key is not configured");
        }

        @Test
        void shouldThrowException_whenApiKeyIsNull() {
            // Arrange
            when(postcodeLookupConfiguration.getUrl()).thenReturn(VALID_URL);
            when(postcodeLookupConfiguration.getAccessKey()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> postcodeLookupService.validatePostCodeForDefendant("SW1A 1AA")).isInstanceOf(
                PostcodeLookupException.class).hasMessageContaining("Postcode lookup service failed for: SW1A 1AA").hasCauseInstanceOf(
                IllegalStateException.class).hasRootCauseMessage("Postcode API key is not configured");
        }

        @Test
        void shouldThrowException_whenRestTemplateThrowsException() {
            // Arrange
            when(postcodeLookupConfiguration.getUrl()).thenReturn(VALID_URL);
            when(postcodeLookupConfiguration.getAccessKey()).thenReturn(API_KEY);
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            )).thenThrow(new RestClientException("Connection timeout"));

            // Act & Assert
            assertThatThrownBy(() -> postcodeLookupService.validatePostCodeForDefendant("SW1A 1AA")).isInstanceOf(
                PostcodeLookupException.class).hasMessageContaining("Postcode lookup service failed for: SW1A 1AA").hasCauseInstanceOf(
                RestClientException.class);
        }

        @Test
        void shouldThrowException_whenResponseBodyIsInvalid() {
            // Arrange
            String postcode = "SW1A 1AA";
            setupMocks("invalid json", HttpStatus.OK);

            // Act & Assert
            assertThatThrownBy(() -> postcodeLookupService.validatePostCodeForDefendant(postcode)).isInstanceOf(
                PostcodeLookupException.class).hasMessageContaining("Postcode lookup service failed for: " + postcode);
        }

        @Test
        void shouldReturnFalse_whenResponseHasNoResults() {
            // Arrange
            String responseJson = "{\"otherField\":\"value\"}";
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant("SW1A 1AA");

            // Assert
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"england", "ENGLAND", "England", "EnGlAnD"})
        void shouldReturnTrue_whenCountryIsEnglandInAnyCase(String country) {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = createSuccessResponse(postcode, country);
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"wales", "WALES", "Wales", "WaLeS"})
        void shouldReturnTrue_whenCountryIsWalesInAnyCase(String country) {
            // Arrange
            String postcode = "CF10 1AA";
            String responseJson = createSuccessResponse(postcode, country);
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            boolean result = postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    class HttpInteractionTests {

        @Test
        void shouldSetCorrectHeaders() {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = createSuccessResponse(postcode, "England");
            setupMocks(responseJson, HttpStatus.OK);

            // Act
            postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        }

        @Test
        void shouldBuildCorrectUrl() {
            // Arrange
            String postcode = "SW1A 1AA";
            String responseJson = createSuccessResponse(postcode, "England");
            when(postcodeLookupConfiguration.getUrl()).thenReturn(VALID_URL);
            when(postcodeLookupConfiguration.getAccessKey()).thenReturn(API_KEY);

            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            )).thenReturn(responseEntity);

            // Act
            postcodeLookupService.validatePostCodeForDefendant(postcode);

            // Assert
            verify(restTemplate).exchange(
                contains("query=SW1A1AA+localtype%3Dpostcode"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
            );
        }
    }
}
