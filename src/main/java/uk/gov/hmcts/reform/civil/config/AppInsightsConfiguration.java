package uk.gov.hmcts.reform.civil.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInsightsConfiguration {

    @Bean
    public TelemetryClient telemetryClient(
        @Value("${appinsights-connection-string:}") String connectionString
    ) {
        // Note: Connection string is typically configured via APPLICATIONINSIGHTS_CONNECTION_STRING
        // environment variable or applicationinsights.json config file in ApplicationInsights 3.x
        return new TelemetryClient();
    }
}
