package uk.gov.hmcts.reform.civil.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
        return new ApacheHttpClient(getHttpClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getRestTemplateHttpClient()));
        return restTemplate;
    }

    private CloseableHttpClient getRestTemplateHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(readTimeout)
            .setConnectionRequestTimeout(readTimeout)
            .setSocketTimeout(readTimeout)
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
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }

}
