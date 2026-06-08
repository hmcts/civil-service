package uk.gov.hmcts.reform.civil.config.feign;

import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.civil.service.FeignErrorTelemetryService;

public class DefaultFeignConfiguration {

    @Value("${feign.initialBackoff:500}")
    private int initialBackoff;
    @Value("${feign.maxBackoff:5000}")
    private int maxBackoff;
    @Value("${feign.maxAttempts:3}")
    private int maxAttempts;

    @Bean
    @ConditionalOnMissingBean(value = ErrorDecoder.class, search = SearchStrategy.CURRENT)
    public ErrorDecoder civilErrorDecoder(FeignErrorTelemetryService telemetryService) {
        return new DefaultErrorDecoder(new ErrorDecoder.Default(), telemetryService);
    }

    @Bean
    @ConditionalOnMissingBean(value = Retryer.class, search = SearchStrategy.CURRENT)
    public Retryer civilRetryer() {
        return new DefaultRetryer(initialBackoff, maxBackoff, maxAttempts);
    }

    @Bean
    @ConditionalOnMissingBean(value = ExceptionPropagationPolicy.class, search = SearchStrategy.ALL)
    public ExceptionPropagationPolicy exceptionPropagationPolicy() {
        return ExceptionPropagationPolicy.UNWRAP;
    }

}
