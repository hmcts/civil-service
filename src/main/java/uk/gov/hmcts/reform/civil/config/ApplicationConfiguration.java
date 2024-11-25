package uk.gov.hmcts.reform.civil.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final HttpConfiguration httpConfiguration;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(httpClient());
    }

    private SimpleClientHttpRequestFactory httpClient() {

        SimpleClientHttpRequestFactory clientHttpRequestFactory  = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(httpConfiguration.getTimeout());
        clientHttpRequestFactory.setReadTimeout(httpConfiguration.getRequestTimeout());

        return clientHttpRequestFactory;
    }
}
