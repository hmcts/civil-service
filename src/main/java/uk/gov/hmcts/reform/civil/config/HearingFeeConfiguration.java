package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class HearingFeeConfiguration {

    private final String url;
    private final String endpoint;
    private final String service;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final String jurisdiction2Hearing;
    private final String channel;
    private final String event;
    private final String hearingEvent;
    private final String fastTrackHrgKey;
    private final String multiClaimKey;
    private final String smallClaimHrgKey;

    public HearingFeeConfiguration(@Value("${fees.api.url}") String url,
                                   @Value("${fees.api.endpoint}") String endpoint,
                                   @Value("${fees.api.service}") String service,
                                   @Value("${fees.api.jurisdiction1}") String jurisdiction1,
                                   @Value("${fees.api.jurisdiction2}") String jurisdiction2,
                                   @Value("${fees.api.jurisdiction2Hearing}") String jurisdiction2Hearing,
                                   @Value("${fees.api.channel}") String channel,
                                   @Value("${fees.api.event}") String event,
                                   @Value("${fees.api.hearingEvent}") String hearingEvent,
                                   @Value("${fees.api.keywords.fastTrackHrg}") String fastTrackHrgKey,
                                   @Value("${fees.api.keywords.multiTrackHrg}") String multiClaimKey,
                                   @Value("${fees.api.keywords.smallClaimHrg}") String smallClaimHrgKey) {
        this.url = url;
        this.endpoint = endpoint;
        this.service = service;
        this.jurisdiction1 = jurisdiction1;
        this.jurisdiction2 = jurisdiction2;
        this.jurisdiction2Hearing = jurisdiction2Hearing;
        this.channel = channel;
        this.event = event;
        this.hearingEvent = hearingEvent;
        this.fastTrackHrgKey = fastTrackHrgKey;
        this.multiClaimKey = multiClaimKey;
        this.smallClaimHrgKey = smallClaimHrgKey;
    }
}
