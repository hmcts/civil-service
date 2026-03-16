package uk.gov.hmcts.reform.civil.client.sendletter.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import uk.gov.hmcts.reform.civil.client.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.civil.client.sendletter.api.proxy.SendLetterApiProxy;

import java.util.Collections;

/**
 * Retry configuration.
 */
@Configuration
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class RetryConfig {

    /**
     * Retry template.
     * @return The RetryTemplate
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setThrowLastExceptionOnExhausted(false);

        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setMaxInterval(3000);
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        retryTemplate.setRetryPolicy(new RetryPolicy(10, Collections.singletonList(HttpStatus.NOT_FOUND)));

        return retryTemplate;
    }

    /**
     * Send letter API.
     * @param sendLetterApiProxy The SendLetterApiProxy
     * @param retryTemplate The RetryTemplate
     * @return The SendLetterApi
     */
    @Bean
    public SendLetterApi sendLetterApi(SendLetterApiProxy sendLetterApiProxy, RetryTemplate retryTemplate) {
        return new SendLetterApi(sendLetterApiProxy, retryTemplate);
    }
}
