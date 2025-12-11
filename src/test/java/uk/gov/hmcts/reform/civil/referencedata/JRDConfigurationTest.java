package uk.gov.hmcts.reform.civil.referencedata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JRDConfigurationTest {

    @Test
    void shouldExposeConfiguredValues() {
        JRDConfiguration configuration = new JRDConfiguration("http://jrd", "/endpoint");

        assertThat(configuration.getUrl()).isEqualTo("http://jrd");
        assertThat(configuration.getEndpoint()).isEqualTo("/endpoint");
    }
}
