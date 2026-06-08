package uk.gov.hmcts.reform.civil.config.feign;

import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalFeignConfigurationTest {

    private GlobalFeignConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new GlobalFeignConfiguration();
        ReflectionTestUtils.setField(configuration, "initialBackoff", 500);
        ReflectionTestUtils.setField(configuration, "maxBackoff", 5000);
        ReflectionTestUtils.setField(configuration, "maxAttempts", 3);
    }

    @Test
    void shouldReturnFeignErrorDecoder() {
        ErrorDecoder decoder = configuration.feignErrorDecoder();
        assertThat(decoder).isInstanceOf(GlobalErrorDecoder.class);
    }

    @Test
    void shouldReturnFeignRetryer() {
        Retryer retryer = configuration.feignRetryer();
        assertThat(retryer).isInstanceOf(Retryer.Default.class);
        
        assertThat(ReflectionTestUtils.getField(retryer, "period")).isEqualTo(500L);
        assertThat(ReflectionTestUtils.getField(retryer, "maxPeriod")).isEqualTo(5000L);
        assertThat(ReflectionTestUtils.getField(retryer, "maxAttempts")).isEqualTo(3);
    }

    @Test
    void shouldReturnFeignRetryerWithCustomValues() {
        ReflectionTestUtils.setField(configuration, "initialBackoff", 1000);
        ReflectionTestUtils.setField(configuration, "maxBackoff", 10000);
        ReflectionTestUtils.setField(configuration, "maxAttempts", 5);

        Retryer retryer = configuration.feignRetryer();
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
