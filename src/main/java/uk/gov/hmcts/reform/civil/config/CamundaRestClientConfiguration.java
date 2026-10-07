package uk.gov.hmcts.reform.civil.config;

import feign.Retryer;
import org.camunda.community.rest.config.CamundaRemoteServicesConfiguration;
import org.camunda.community.rest.config.CamundaRestClientProperties;
import org.camunda.community.rest.config.FeignErrorDecoderConfiguration;
import org.camunda.community.rest.variables.ValueMapperConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stands in for {@code @EnableCamundaRestClient} from the holunda c7 REST client.
 *
 * <p>The library (2026.04.x) still targets Spring Boot 3. Its {@code FeignClientConfiguration} references
 * {@code org.springframework.boot.autoconfigure.http.HttpMessageConverters}, which Spring Boot 4 removed, so it
 * cannot be loaded. This imports the library's other configurations and registers its Feign clients directly.
 * It keeps the library's {@link Retryer} bean, which every Feign client in this service has always picked up,
 * and leaves out the library's encoder, so Feign clients use the default Spring Cloud OpenFeign encoder backed by
 * the application's message converters.
 *
 * <p>Switch back to {@code @EnableCamundaRestClient} once the library supports Spring Boot 4.
 */
@Configuration
@ConditionalOnProperty(prefix = "camunda.rest.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CamundaRestClientProperties.class)
@EnableFeignClients(basePackages = "org.camunda.community.rest.client")
@ImportAutoConfiguration({
    FeignErrorDecoderConfiguration.class,
    ValueMapperConfiguration.class,
    CamundaRemoteServicesConfiguration.class
})
public class CamundaRestClientConfiguration {

    @Bean
    public Retryer.Default camundaRetryer() {
        return new Retryer.Default();
    }
}
