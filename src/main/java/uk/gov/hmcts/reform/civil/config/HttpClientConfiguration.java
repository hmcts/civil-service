package uk.gov.hmcts.reform.civil.config;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
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
        return new ApacheHttpClient(getHttpClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getRestTemplateHttpClient()));
        return restTemplate;
    }

    private HttpClient getRestTemplateHttpClient() {
        final RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(readTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(readTimeout))
            .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        return HttpClients.custom()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build())
            .build();
    }

    private org.apache.http.impl.client.CloseableHttpClient getHttpClient() {
        org.apache.http.client.config.RequestConfig config = org.apache.http.client.config.RequestConfig.custom()
            .setConnectTimeout(readTimeout)
            .setConnectionRequestTimeout(readTimeout)
            .setSocketTimeout(readTimeout)
            .build();

        return org.apache.http.impl.client.HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }
}
