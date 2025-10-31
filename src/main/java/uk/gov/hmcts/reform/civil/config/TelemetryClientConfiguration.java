package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class TelemetryClientConfiguration {

    @Bean
    @ConditionalOnMissingBean(TelemetryClient.class)
    public TelemetryClient telemetryClient(
        @Value("${appinsights-connection-string:}") String connectionString,
        @Value("${azure.service-bus.application-insights.instrumentation-key:}") String instrumentationKey
    ) {
        TelemetryConfiguration configuration = TelemetryConfiguration.createDefault();

        if (StringUtils.hasText(connectionString)) {
            configuration.setConnectionString(connectionString);
        } else if (StringUtils.hasText(instrumentationKey)) {
            configuration.setInstrumentationKey(instrumentationKey);
        }

        return new TelemetryClient(configuration);
    }
}
