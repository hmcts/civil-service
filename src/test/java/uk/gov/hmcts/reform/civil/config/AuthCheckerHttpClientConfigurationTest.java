package uk.gov.hmcts.reform.civil.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCheckerHttpClientConfigurationTest {

    @Test
    void shouldCreateDefaultHttpClient() throws Exception {
        try (CloseableHttpClient client = new AuthCheckerHttpClientConfiguration().authCheckerHttpClient()) {
            assertThat(client).isNotNull();
        }
    }
}
