package uk.gov.hmcts.reform.unspec.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LaunchDarklyConfiguration {

    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk-key}") String sdkKey,
                             @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode
    ) {
        LDConfig config = new LDConfig.Builder()
            .offline(offlineMode)
            .build();
        return new LDClient(sdkKey, config);
    }
}
