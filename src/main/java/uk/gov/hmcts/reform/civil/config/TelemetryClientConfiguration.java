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

        String trimmedConnectionString = StringUtils.trimWhitespace(connectionString);
        String trimmedInstrumentationKey = StringUtils.trimWhitespace(instrumentationKey);

        if (isRealConnectionString(trimmedConnectionString)) {
            configuration.setConnectionString(trimmedConnectionString);
        } else if (isRealInstrumentationKey(trimmedInstrumentationKey)) {
            configuration.setInstrumentationKey(trimmedInstrumentationKey);
        }

        return new TelemetryClient(configuration);
    }

    private boolean isRealConnectionString(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lowerCase = value.toLowerCase();
        if ("dummy".equals(lowerCase)) {
            return false;
        }
        return lowerCase.contains("instrumentationkey=") || lowerCase.contains("ingestionendpoint=");
    }

    private boolean isRealInstrumentationKey(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lowerCase = value.toLowerCase();
        if (lowerCase.equals("00000000-0000-0000-0000-000000000000")) {
            return false;
        }
        return true;
    }
}
