package uk.gov.hmcts.reform.civil.postcode;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PostcodeLookupService.class)
class PostcodeLookupServiceTest {

    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private PostcodeLookupConfiguration postcodeLookupConfiguration;

    @Autowired
    private PostcodeLookupService postcodeLookupService;

    @Test
    public void shouldReturnFalseWhenCountryIsNullGivenPostCodeIsValid() {

        ResponseEntity<String> responseEntity = new ResponseEntity<String>("Ok", HttpStatus.ACCEPTED);
        when(postcodeLookupConfiguration.getUrl()).thenReturn("https://api.ordnancesurvey.co.uk/opennames/v1/find");
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("dummy");
        when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<?>>any(),
            ArgumentMatchers.<Class<String>>any()
        )).thenReturn(responseEntity);

        assertThat(postcodeLookupService.validatePostCodeForDefendant("IG11 7YL")).isFalse();
    }

    @Test
    public void shouldReturnExpcetionWhenUrlIsEmpty() {
        assertThrows(
            RuntimeException.class, () -> postcodeLookupService.validatePostCodeForDefendant("IG11 7YL")
        );
    }

    @Test
    public void whenBlank_thenNoUk() {
        Assertions.assertFalse(postcodeLookupService.validatePostCodeUk(null));
        Assertions.assertFalse(postcodeLookupService.validatePostCodeUk(""));
    }

    @Test
    public void whenNorthIreland_thenUk() {
        Assertions.assertTrue(postcodeLookupService.validatePostCodeUk("BT12 5JN"));
    }

    @ParameterizedTest
    @EnumSource(
        value = CountriesAllowed.class,
        names = {"ENGLAND", "SCOTLAND", "WALES"})
    public void whenEnglandWalesScot_thenUk(CountriesAllowed c) {
        String postCode = "POSTCODE";
        String country = c.name();

        Map<String, Object> map = Map.of(
            "results", Collections.singletonList(
                Map.of("GAZETTEER_ENTRY", Map.of(
                    "NAME1", postCode,
                    "COUNTRY", country
                ))
            )
        );
        String json = JSONObject.valueToString(map);
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(json, HttpStatus.OK);
        when(postcodeLookupConfiguration.getUrl()).thenReturn("https://api.ordnancesurvey.co.uk/opennames/v1/find");
        when(postcodeLookupConfiguration.getAccessKey()).thenReturn("dummy");
        when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.<HttpEntity<?>>any(),
            ArgumentMatchers.<Class<String>>any()
        )).thenReturn(responseEntity);

        Assertions.assertTrue(postcodeLookupService.validatePostCodeUk(postCode));
    }
}
