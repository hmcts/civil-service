package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInsightsConfiguration {

    @Bean
    public TelemetryClient telemetryClient(
        @Value("${appinsights-connection-string:}") String connectionString
    ) {
        if (connectionString != null && !connectionString.isBlank()) {
            TelemetryConfiguration.getActive().setConnectionString(connectionString);
        }
        return new TelemetryClient();
    }
}
