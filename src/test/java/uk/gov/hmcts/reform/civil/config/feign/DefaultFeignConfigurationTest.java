package uk.gov.hmcts.reform.civil.config.feign;

import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFeignConfigurationTest {

    private DefaultFeignConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new DefaultFeignConfiguration();
        ReflectionTestUtils.setField(configuration, "initialBackoff", 500);
        ReflectionTestUtils.setField(configuration, "maxBackoff", 5000);
        ReflectionTestUtils.setField(configuration, "maxAttempts", 3);
    }

    @Test
    void shouldReturnCivilErrorDecoder() {
        ErrorDecoder decoder = configuration.civilErrorDecoder(null);
        assertThat(decoder).isInstanceOf(DefaultErrorDecoder.class);
    }

    @Test
    void shouldOnlyCreateCivilErrorDecoderWhenCurrentContextHasNoErrorDecoderBean() throws NoSuchMethodException {
        Method method = DefaultFeignConfiguration.class.getMethod(
            "civilErrorDecoder",
            FeignErrorTelemetryService.class);

        ConditionalOnMissingBean conditionalOnMissingBean = method.getAnnotation(ConditionalOnMissingBean.class);

        assertThat(conditionalOnMissingBean).isNotNull();
        assertThat(conditionalOnMissingBean.value()).containsExactly(ErrorDecoder.class);
        assertThat(conditionalOnMissingBean.search()).isEqualTo(SearchStrategy.CURRENT);
    }

    @Test
    void shouldReturnCivilRetryer() {
        Retryer retryer = configuration.civilRetryer();
        assertThat(retryer).isInstanceOf(Retryer.Default.class);

        assertThat(ReflectionTestUtils.getField(retryer, "period")).isEqualTo(500L);
        assertThat(ReflectionTestUtils.getField(retryer, "maxPeriod")).isEqualTo(5000L);
        assertThat(ReflectionTestUtils.getField(retryer, "maxAttempts")).isEqualTo(3);
    }

    @Test
    void shouldReturnCivilRetryerWithCustomValues() {
        ReflectionTestUtils.setField(configuration, "initialBackoff", 1000);
        ReflectionTestUtils.setField(configuration, "maxBackoff", 10000);
        ReflectionTestUtils.setField(configuration, "maxAttempts", 5);

        Retryer retryer = configuration.civilRetryer();
        assertThat(retryer).isInstanceOf(Retryer.Default.class);

        assertThat(ReflectionTestUtils.getField(retryer, "period")).isEqualTo(1000L);
        assertThat(ReflectionTestUtils.getField(retryer, "maxPeriod")).isEqualTo(10000L);
        assertThat(ReflectionTestUtils.getField(retryer, "maxAttempts")).isEqualTo(5);
    }

    @Test
    void shouldReturnExceptionPropagationPolicy() {
        ExceptionPropagationPolicy policy = configuration.exceptionPropagationPolicy();
        assertThat(policy).isEqualTo(ExceptionPropagationPolicy.UNWRAP);
    }
}
