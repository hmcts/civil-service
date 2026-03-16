package uk.gov.hmcts.reform.civil.client.payments.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class PaymentClientConfiguration {
    @Bean
    @Primary
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
