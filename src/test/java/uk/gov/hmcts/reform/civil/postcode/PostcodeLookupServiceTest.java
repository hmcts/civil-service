package uk.gov.hmcts.reform.civil.postcode;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = PostcodeLookupService.class)
class PostcodeLookupServiceTest {

    private static final String LOOKUP_URL = "https://api.ordnancesurvey.co.uk/opennames/v1/find";
    private static final String ACCESS_KEY = "dummy-key";

    @MockBean
    private RestTemplate restTemplate;
    @MockBean
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

    @Test
    void shouldWrapExceptionWhenLookupFails() {
        mockConfiguration();
        when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
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

    private void mockConfiguration() {
        when(postcodeLookupConfiguration.getUrl()).thenReturn(LOOKUP_URL);
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn(ACCESS_KEY);
    }

    private void mockExchangeResponse(ResponseEntity<String> responseEntity) {
        when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<?>>any(),
            ArgumentMatchers.<Class<String>>any()
        )).thenReturn(responseEntity);
    }
}
