package uk.gov.hmcts.reform.civil.postcode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(classes = PostcodeLookupService.class)
class PostcodeLookupServiceTest {

    private static final String LOOKUP_URL = "https://api.ordnancesurvey.co.uk/opennames/v1/find";
    private static final String ACCESS_KEY = "dummy-key";

    @MockitoBean
    private RestTemplate restTemplate;
    @MockitoBean
    private PostcodeLookupConfiguration postcodeLookupConfiguration;

    @Autowired
    private PostcodeLookupService postcodeLookupService;

    @Test
    void shouldReturnFalseWhenCountryIsNullGivenPostCodeIsValid() {
        mockConfiguration();
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        mockExchangeResponse(responseEntity);

        assertThat(postcodeLookupService.validatePostCodeForDefendant("IG11 7YL"))
            .isFalse();
    }

    @Test
    void shouldReturnTrueWhenCountryIsEngland() {
        mockConfiguration();
        String response = "{\"results\":[{\"GAZETTEER_ENTRY\":{\"NAME1\":\"IG11 7YL\",\"COUNTRY\":\"England\"}}]}";
        mockExchangeResponse(new ResponseEntity<>(response, HttpStatus.OK));

        assertThat(postcodeLookupService.validatePostCodeForDefendant("IG11 7YL"))
            .isTrue();
    }

    @Test
    void shouldReturnFalseWhenPostcodeNotFound() {
        mockConfiguration();
        mockExchangeResponse(new ResponseEntity<>("{}", HttpStatus.NOT_FOUND));

        assertThat(postcodeLookupService.validatePostCodeForDefendant("IG11 7YL"))
            .isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t"})
    void shouldRejectBlankPostcodeWithoutLookup(String postcode) {
        assertThat(postcodeLookupService.validatePostCodeForDefendant(postcode)).isFalse();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldReturnFalseWhenResultsAreEmpty() {
        mockConfiguration();
        mockExchangeResponse(ResponseEntity.ok("{\"results\":[]}"));

        assertThat(postcodeLookupService.validatePostCodeForDefendant("CM145NB")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRestTemplateThrowsNotFound() {
        mockConfiguration();
        when(restTemplate.exchange(
            ArgumentMatchers.any(URI.class),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<?>>any(),
            ArgumentMatchers.<Class<String>>any()
        )).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThat(postcodeLookupService.validatePostCodeForDefendant("CM145NB")).isFalse();
    }

    @Test
    void shouldWrapExceptionWhenLookupFails() {
        mockConfiguration();
        when(restTemplate.exchange(
            ArgumentMatchers.any(URI.class),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<?>>any(),
            ArgumentMatchers.<Class<String>>any()
        )).thenThrow(new RuntimeException("Boom"));

        assertThatThrownBy(() -> postcodeLookupService.validatePostCodeForDefendant("IG11 7YL"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldReturnExceptionWhenUrlIsEmpty() {
        org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class, () -> postcodeLookupService.validatePostCodeForDefendant("IG11 7YL")
        );
    }

    @ParameterizedTest
    @CsvSource({
        "CM14 5NB, CM14 5NB, England",
        "CM145NB, CM14 5NB, England",
        "M1 1AE, M1 1AE, England",
        "M11AE, M1 1AE, England",
        "SR7 1BJ, SR7 1BJ, England",
        "SR71BJ, SR7 1BJ, England",
        "SA10 7DR, SA10 7DR, Wales",
        "SA107DR, SA10 7DR, Wales",
        "cm145nb, CM14 5NB, England"
    })
    void shouldLookUpFormattedPostcode(String input, String formatted, String country) {
        mockConfiguration();
        String response = "{\"results\":[{\"GAZETTEER_ENTRY\":{\"NAME1\":\"%s\",\"COUNTRY\":\"%s\"}}]}"
            .formatted(formatted, country);
        mockExchangeResponse(new ResponseEntity<>(response, HttpStatus.OK));

        assertThat(postcodeLookupService.validatePostCodeForDefendant(input)).isTrue();

        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uri.capture(), ArgumentMatchers.eq(HttpMethod.GET),
                                     ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.eq(String.class));
        assertThat(uri.getValue().getRawQuery().split("&"))
            .containsExactlyInAnyOrder("query=" + formatted.replace(" ", "%20"),
                                      "fq=LOCAL_TYPE:Postcode", "maxresults=1", "key=" + ACCESS_KEY);
    }

    @ParameterizedTest
    @CsvSource({"CM14 5NB, Scotland", "M1 1AE, England"})
    void shouldRejectIneligibleLookupResult(String returnedPostcode, String country) {
        mockConfiguration();
        String response = "{\"results\":[{\"GAZETTEER_ENTRY\":{\"NAME1\":\"%s\",\"COUNTRY\":\"%s\"}}]}"
            .formatted(returnedPostcode, country);
        mockExchangeResponse(new ResponseEntity<>(response, HttpStatus.OK));

        assertThat(postcodeLookupService.validatePostCodeForDefendant("CM145NB")).isFalse();
    }

    private void mockConfiguration() {
        when(postcodeLookupConfiguration.getUrl()).thenReturn(LOOKUP_URL);
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn(ACCESS_KEY);
    }

    private void mockExchangeResponse(ResponseEntity<String> responseEntity) {
        when(restTemplate.exchange(
            ArgumentMatchers.any(URI.class),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<?>>any(),
            ArgumentMatchers.<Class<String>>any()
        )).thenReturn(responseEntity);
    }
}
