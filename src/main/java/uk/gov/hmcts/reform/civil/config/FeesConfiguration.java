package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class FeesConfiguration {

    private final String channel;
    private final String event;

    public FeesConfiguration(@Value("${fees.api.channel}") String channel,
                             @Value("${fees.api.event}") String event) {
        this.channel = channel;
        this.event = event;
    }
}
