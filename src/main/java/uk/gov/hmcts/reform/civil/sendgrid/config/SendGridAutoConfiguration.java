package uk.gov.hmcts.reform.civil.sendgrid.config;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridAPI;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isNotBlank(properties.getHost())) {
            sendGrid.setHost(properties.getHost());
        }
        if (StringUtils.isNotBlank(properties.getVersion())) {
            sendGrid.setVersion(properties.getVersion());
        }

        return sendGrid;
    }

    private SendGrid createSendGrid(SendGridProperties properties) {
        if (properties.getTest() != null) {
            return new SendGrid(properties.getApiKey(), properties.getTest());
        }
        return new SendGrid(properties.getApiKey());
    }
}
