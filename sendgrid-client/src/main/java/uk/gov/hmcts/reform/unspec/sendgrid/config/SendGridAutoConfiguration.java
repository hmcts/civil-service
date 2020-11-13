package uk.gov.hmcts.reform.unspec.sendgrid.config;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridAPI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SendGrid.class)
@ConditionalOnProperty(prefix = "sendgrid", value = "api-key")
@EnableConfigurationProperties(SendGridProperties.class)
public class SendGridAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SendGridAPI.class)
    public SendGrid sendGrid(SendGridProperties properties) {
        SendGrid sendGrid = createSendGrid(properties);
        if (properties.getHost() != null) {
            sendGrid.setHost(properties.getHost());
        }
        if (properties.getVersion() != null) {
            sendGrid.setVersion(properties.getVersion());
        }

        return sendGrid;
    }

    private SendGrid createSendGrid(SendGridProperties properties) {
        if (properties.getTest() != null && properties.getTest()) {
            return new SendGrid(properties.getApiKey(), properties.getTest());
        }
        return new SendGrid(properties.getApiKey());
    }
}
