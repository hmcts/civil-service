package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class HttpClientRestConfigurationTest {

    @Test
    void shouldCreateRestTemplate() {
        HttpClientRestConfiguration configuration = new HttpClientRestConfiguration();
        RestTemplate restTemplate = configuration.restTemplate();
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
        assertThat(restTemplate.getInterceptors()).isEmpty();
    }
}
