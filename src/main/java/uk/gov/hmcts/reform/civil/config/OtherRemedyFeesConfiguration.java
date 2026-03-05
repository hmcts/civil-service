package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class OtherRemedyFeesConfiguration {

    private final String url;
    private final String endpoint;
    private final String service;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final String channel;
    private final String event;
    private final String jurisdiction2Hearing;
    private final String anyOtherRemedyKeyword;

    public OtherRemedyFeesConfiguration(
            @Value("${fees.api.url}") String url,
            @Value("${fees.api.endpoint}") String endpoint,
            @Value("${otherRemediesFees.api.service}") String service,
            @Value("${fees.api.jurisdiction1}") String jurisdiction1,
            @Value("${otherRemediesFees.api.jurisdiction2}") String jurisdiction2,
            @Value("${fees.api.channel}") String channel,
            @Value("${fees.api.event}") String event,
            @Value("${fees.api.jurisdiction2Hearing}") String jurisdiction2Hearing,
            @Value("${fees.api.keywords.anyOtherRemedy}") String anyOtherRemedyKeyword) {
        this.url = url;
        this.endpoint = endpoint;
        this.service = service;
        this.jurisdiction1 = jurisdiction1;
        this.jurisdiction2 = jurisdiction2;
        this.channel = channel;
        this.event = event;
        this.jurisdiction2Hearing = jurisdiction2Hearing;
        this.anyOtherRemedyKeyword = anyOtherRemedyKeyword;
    }
}
