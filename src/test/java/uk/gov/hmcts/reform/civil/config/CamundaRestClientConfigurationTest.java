package uk.gov.hmcts.reform.civil.config;

import feign.Retryer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CamundaRestClientConfigurationTest {

    @Test
    void shouldUseFeignDefaultRetryer() {
        assertThat(new CamundaRestClientConfiguration().camundaRetryer()).isExactlyInstanceOf(Retryer.Default.class);
    }
}
