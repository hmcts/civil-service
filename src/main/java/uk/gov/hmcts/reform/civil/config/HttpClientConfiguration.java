package uk.gov.hmcts.reform.civil.config;

import feign.Client;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
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
        return new ApacheHttp5Client(getHttpClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getRestTemplateHttpClient()));
        return restTemplate;
    }

    private CloseableHttpClient getRestTemplateHttpClient() {
        return buildHttpClient(readTimeout);
    }

    private CloseableHttpClient getHttpClient() {
        return buildHttpClient(10000);
    }

    private CloseableHttpClient buildHttpClient(int timeoutMillis) {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(timeoutMillis))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeoutMillis))
            .setResponseTimeout(Timeout.ofMilliseconds(timeoutMillis))
            .build();

        return HttpClients.custom()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }

}
