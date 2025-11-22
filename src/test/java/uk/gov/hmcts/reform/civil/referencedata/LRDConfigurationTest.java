package uk.gov.hmcts.reform.civil.referencedata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LRDConfigurationTest {

    @Test
    void shouldExposeConfiguredValues() {
        LRDConfiguration configuration = new LRDConfiguration("http://lrd", "/endpoint");

        assertThat(configuration.getUrl()).isEqualTo("http://lrd");
        assertThat(configuration.getEndpoint()).isEqualTo("/endpoint");
    }
}
