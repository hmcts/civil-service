package uk.gov.hmcts.reform.civil.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class Boot3ClientLibraryConfigurationTest {

    private final Boot3ClientLibraryConfiguration configuration = new Boot3ClientLibraryConfiguration();

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateFeignHttpMessageConverters() {
        FeignHttpMessageConverters converters = configuration.feignHttpMessageConverters(
            mock(ObjectProvider.class),
            mock(ObjectProvider.class)
        );

        assertThat(converters).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateFormEncoder() {
        Encoder encoder = configuration.feignFormEncoder(mock(ObjectProvider.class));

        assertThat(encoder).isInstanceOf(FormEncoder.class);
    }
}
