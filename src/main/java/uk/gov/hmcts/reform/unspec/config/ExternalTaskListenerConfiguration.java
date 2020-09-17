package uk.gov.hmcts.reform.unspec.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalTaskListenerConfiguration {

    private final String baseUrl;

    public ExternalTaskListenerConfiguration(@Value("${feign.client.config.remoteRuntimeService.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Bean
    public ExternalTaskClient client() {
        return ExternalTaskClient.create()
            .baseUrl(baseUrl)
            .build();
    }
}
