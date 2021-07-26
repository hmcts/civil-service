package uk.gov.hmcts.reform.civil.service.postcode;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.config.PostcodeLookupConfiguration;

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
            ArgumentMatchers.<HttpEntity<?>> any(),
            ArgumentMatchers.<Class<String>> any())).thenReturn(responseEntity);

        assertThat(postcodeLookupService.validatePostCodeForDefendant("IG11 7YL")).isFalse();
    }

    @Test
    public void shouldReturnExpcetionWhenUrlIsEmpty() {
        assertThrows(
            RuntimeException.class, () -> postcodeLookupService.validatePostCodeForDefendant("IG11 7YL")
        );
    }
}
