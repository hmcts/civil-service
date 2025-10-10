package uk.gov.hmcts.reform.civil.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfiguration {

    private final int readTimeout;

    public HttpClientConfiguration(@Value("${http.client.readTimeout}") int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Bean
    public Client getFeignHttpClient() {
        return new ApacheHttpClient();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory())
            .setHttpClient(getRestTemplateHttpClient());
        return restTemplate;
    }

    private CloseableHttpClient getRestTemplateHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(readTimeout))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(readTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }

    private CloseableHttpClient getHttpClient() {
        int timeout = 10000;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(timeout))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout))
            .setResponseTimeout(Timeout.ofMilliseconds(timeout))
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }

}
