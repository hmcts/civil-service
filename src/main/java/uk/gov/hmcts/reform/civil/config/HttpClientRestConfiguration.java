package uk.gov.hmcts.reform.civil.config;

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
public class HttpClientRestConfiguration {

    @Value("${http.client.connectTimeout:5000}")
    private int connectTimeout;
    @Value("${http.client.requestTimeout:10000}")
    private int requestTimeout;
    @Value("${http.client.readTimeout:30000}")
    private int readTimeout;

    @Value("${http.client.maxPerRoute:5}")
    private int maxPerRoute;
    @Value("${http.client.maxTotal:25}")
    private int maxTotal;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getHttpClient()));
        return restTemplate;
    }

    private HttpClient getHttpClient() {

        final ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
            .setSocketTimeout(Timeout.ofMilliseconds(readTimeout)).build();

        final RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout)).build();

        return HttpClients.custom()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setDefaultConnectionConfig(connectionConfig)
                    .setMaxConnTotal(maxTotal)
                    .setMaxConnPerRoute(maxPerRoute).build())
            .build();
    }
}
