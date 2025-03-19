package uk.gov.hmcts.reform.civil.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.Fee2Dto;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(prefix = "fees.api", name = "url")

public class FeesClientService {

    public static final String EVENT_ISSUE = "issue";
    public static final String EVENT_HEARING = "hearing";
    public static final String FAST_TRACK_HEARING = "FastTrackHrg";
    public static final String HEARING_SMALL_CLAIMS = "HearingSmallClaims";
    public static final String MONEY_CLAIM = "MoneyClaim";

    private final FeesApiClient feesApiClient;
    private final String service;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final String jurisdictionFastTrackClaim;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeesClientService(
        FeesApiClient feesApiClient,
        FeatureToggleService featureToggleService,
        @Value("${fees.api.service:}") String service,
        @Value("${fees.api.jurisdiction1:}") String jurisdiction1,
        @Value("${fees.api.jurisdiction2:}") String jurisdiction2,
        @Value("${fees.api.jurisdiction-fast-track-claim:}") String jurisdictionFastTrackClaim
    ) {
        this.feesApiClient = feesApiClient;
        this.service = service;
        this.jurisdiction1 = jurisdiction1;
        this.jurisdiction2 = jurisdiction2;
        this.jurisdictionFastTrackClaim = jurisdictionFastTrackClaim;
        this.featureToggleService = featureToggleService;
    }

    public FeeLookupResponseDto lookupFee(String channel, String event, BigDecimal amount) {
        if (featureToggleService.isFeatureEnabled("fee-keywords-enable")) {
            String keyword;
            String jurisdiction2;

            if (isFastTrackClaimAndHearingEvent(amount, event)) {
                keyword = FAST_TRACK_HEARING;
                jurisdiction2 = this.jurisdictionFastTrackClaim;
            } else {
                keyword = EVENT_ISSUE.equalsIgnoreCase(event)
                    ? MONEY_CLAIM
                    : HEARING_SMALL_CLAIMS;
                jurisdiction2 = this.jurisdiction2;
            }

            return feesApiClient.lookupFeeWithAmount(
                service,
                jurisdiction1,
                jurisdiction2,
                channel,
                event,
                keyword,
                amount
            );

        } else {
            return feesApiClient.lookupFeeWithoutKeyword(service, jurisdiction1, jurisdiction2, channel, event, amount);
        }
    }

    public Fee2Dto[] findRangeGroup(String channel, String event) {
        return feesApiClient.findRangeGroup(service, jurisdiction1, jurisdiction2, channel, event);
    }

    /**
     * Returns true if given amount fall under fast track claim and event is hearing.
     *
     * @param amount - Claim amount
     * @param event  - Claim event
     * @return boolean
     */
    private boolean isFastTrackClaimAndHearingEvent(BigDecimal amount, String event) {

        return EVENT_HEARING.equalsIgnoreCase(event) && AllocatedTrack.FAST_CLAIM == AllocatedTrack.getAllocatedTrack(
            amount,
            null,
            null
        );
    }
}
