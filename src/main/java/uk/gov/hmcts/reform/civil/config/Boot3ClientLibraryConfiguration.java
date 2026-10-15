package uk.gov.hmcts.reform.civil.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * Stands in for the configuration classes of payments-java-client, fees-java-client,
 * send-letter-client and core-case-data-store-client 5.x. The first three have no Spring Boot 4
 * release; the CCD client's Boot 4 line (6.x) moved its models to Jackson 3, which civil-service
 * does not use yet. Their own configuration classes are
 * excluded from component scanning in {@code Application} because they reference Boot 3 health
 * and HTTP-converter types that no longer exist. Their health indicators are not replaced.
 * Remove once those libraries publish Boot 4 builds.
 */
@Configuration
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.payments.client",
    "uk.gov.hmcts.reform.sendletter"
})
public class Boot3ClientLibraryConfiguration {

    /**
     * openfeign 5 only defines this bean inside each Feign client's child context; the global
     * encoder below lives in the root context, so it needs its own, built the same way.
     */
    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
        ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
        ObjectProvider<HttpMessageConverterCustomizer> converterCustomizers) {
        return new FeignHttpMessageConverters(customizers, converterCustomizers);
    }

    /**
     * Same bean FeesClientAutoConfiguration contributed: a primary form-capable encoder used by
     * every Feign client that does not declare its own.
     */
    @Bean
    @Primary
    @Scope("prototype")
    public Encoder feignFormEncoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new FormEncoder(new SpringEncoder(messageConverters));
    }
}
