package uk.gov.hmcts.reform.civil.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
@ConditionalOnProperty(prefix = "fees", name = "api.url")
@EnableFeignClients(basePackages = "uk.gov.hmcts.reform.fees.client")
public class FeesClientConfiguration {


    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    @Primary
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Encoder feignFormEncoder() {
        return new FormEncoder(new SpringEncoder(this.messageConverters));
    }
}
