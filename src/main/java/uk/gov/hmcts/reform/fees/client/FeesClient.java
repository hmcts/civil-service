package uk.gov.hmcts.reform.fees.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(prefix = "fees.api", name = "url")

public class FeesClient {
    private static final Logger LOG = LoggerFactory.getLogger(FeesClient.class);

    private final FeesApi feesApi;
    private final String service;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeesClient(
        FeesApi feesApi,
        FeatureToggleService featureToggleService,
        @Value("${fees.api.service:}") String service,
        @Value("${fees.api.jurisdiction1:}") String jurisdiction1,
        @Value("${fees.api.jurisdiction2:}") String jurisdiction2
    ) {
        this.feesApi = feesApi;
        this.service = service;
        this.jurisdiction1 = jurisdiction1;
        this.jurisdiction2 = jurisdiction2;
        this.featureToggleService = featureToggleService;
    }

    public FeeLookupResponseDto lookupFee(String channel, String event, BigDecimal amount) {
        if (featureToggleService.isFeatureEnabled("fee-keywords-enable")) {
            String keyword;

            if (featureToggleService.isLipVLipEnabled() && "hearing".equalsIgnoreCase(event) && AllocatedTrack.FAST_CLAIM == AllocatedTrack.getAllocatedTrack(
                amount,
                null
            )) {
                keyword = "FastTrackHrg";
            } else {
                keyword = event.equalsIgnoreCase("issue")
                    ? "MoneyClaim"
                    : "HearingSmallClaims";
            }
            LOG.info("Calling fee lookup service with keyword : " + keyword + " and event : " + event);
            FeeLookupResponseDto feeLookupResponseDto = this.feesApi.lookupFee(
                service,
                jurisdiction1,
                jurisdiction2,
                channel,
                event,
                keyword,
                amount
            );

            LOG.info("Fee returned from fee api is " + feeLookupResponseDto.getFeeAmount());
            return feeLookupResponseDto;
        } else {
            return this.feesApi.lookupFeeWithoutKeyword(service, jurisdiction1, jurisdiction2, channel, event, amount);
        }
    }

    public Fee2Dto[] findRangeGroup(String channel, String event) {
        return this.feesApi.findRangeGroup(service, jurisdiction1, jurisdiction2, channel, event);
    }
}
