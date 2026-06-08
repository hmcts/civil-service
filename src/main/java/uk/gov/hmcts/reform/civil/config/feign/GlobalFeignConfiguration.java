package uk.gov.hmcts.reform.civil.config.feign;

import feign.ExceptionPropagationPolicy;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalFeignConfiguration {

    @Value("${feign.initialBackoff:500}")
    private int initialBackoff;
    @Value("${feign.maxBackoff:5000}")
    private int maxBackoff;
    @Value("${feign.maxAttempts:3}")
    private int maxAttempts;

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new GlobalErrorDecoder();
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(initialBackoff, maxBackoff, maxAttempts);
    }

    @Bean
    public ExceptionPropagationPolicy exceptionPropagationPolicy() {
        return ExceptionPropagationPolicy.UNWRAP;
    }

}
