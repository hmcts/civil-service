package uk.gov.hmcts.reform.civil.client.idam;

import feign.Client;
import feign.Logger;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

public class CoreFeignConfiguration {

    @Value("${idam.api.timeout:10000}")
    private int REQUEST_TIMEOUT;

    @Value("${idam.api.loglevel:NONE}")
    private Logger.Level logLevel;

    @Bean
    Logger.Level feignLoggerLevel() {
        return logLevel;
    }

    @Autowired
    private ObjectProvider<FeignHttpMessageConverters> messageConverters;

    @Bean
    @Primary
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Encoder feignFormEncoder() {
        return new FormEncoder(new SpringEncoder(this.messageConverters));
    }

    @ConditionalOnProperty(value = "idam.apachehttpclient.enable", havingValue = "true", matchIfMissing = true)
    @Bean
    public Client getFeignHttpClient() {
        return new ApacheHttpClient(getHttpClient());
    }

    private CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(REQUEST_TIMEOUT)
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setSocketTimeout(REQUEST_TIMEOUT)
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .disableRedirectHandling()
            .setDefaultRequestConfig(config)
            .build();
    }
}
