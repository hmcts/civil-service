package uk.gov.hmcts.reform.civil.postcode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostcodeLookupConfigurationTest {

    @Test
    void shouldExposeConfiguredValues() {
        PostcodeLookupConfiguration configuration = new PostcodeLookupConfiguration("http://postcode", "secret");

        assertThat(configuration.getUrl()).isEqualTo("http://postcode");
        assertThat(configuration.getAccessKey()).isEqualTo("secret");
    }
}
