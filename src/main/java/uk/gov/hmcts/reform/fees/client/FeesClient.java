package uk.gov.hmcts.reform.fees.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(prefix = "fees.api", name = "url")

public class FeesClient {

    public static final String EVENT_ISSUE = "issue";
    public static final String EVENT_HEARING = "hearing";
    public static final String FAST_TRACK_HEARING = "FastTrackHrg";
    public static final String HEARING_SMALL_CLAIMS = "HearingSmallClaims";
    public static final String MONEY_CLAIM = "MoneyClaim";

    private final FeesApi feesApi;
    private final String service;
    private final String jurisdiction1;
    private final String jurisdiction2;
    private final String jurisdictionFastTrackClaim;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeesClient(
        FeesApi feesApi,
        FeatureToggleService featureToggleService,
        @Value("${fees.api.service:}") String service,
        @Value("${fees.api.jurisdiction1:}") String jurisdiction1,
        @Value("${fees.api.jurisdiction2:}") String jurisdiction2,
        @Value("${fees.api.jurisdiction-fast-track-claim:}") String jurisdictionFastTrackClaim
    ) {
        this.feesApi = feesApi;
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

            if (featureToggleService.isLipVLipEnabled() && isFastTrackClaimAndHearingEvent(amount, event)) {
                keyword = FAST_TRACK_HEARING;
                jurisdiction2 = this.jurisdictionFastTrackClaim;
            } else {
                keyword = EVENT_ISSUE.equalsIgnoreCase(event)
                    ? MONEY_CLAIM
                    : HEARING_SMALL_CLAIMS;
                jurisdiction2 = this.jurisdiction2;
            }

            return this.feesApi.lookupFee(
                service,
                jurisdiction1,
                jurisdiction2,
                channel,
                event,
                keyword,
                amount
            );

        } else {
            return this.feesApi.lookupFeeWithoutKeyword(service, jurisdiction1, jurisdiction2, channel, event, amount);
        }
    }

    public Fee2Dto[] findRangeGroup(String channel, String event) {
        return this.feesApi.findRangeGroup(service, jurisdiction1, jurisdiction2, channel, event);
    }

    /**
     * Returns true if given amount fall under fast track claim and event is hearing.
     *
     * @param amount - Claim amount
     * @param event  - Claim event
     * @return boolean
     */
    private boolean isFastTrackClaimAndHearingEvent(BigDecimal amount, String event) {
        CaseData caseData = CaseData.builder().totalClaimAmount(amount).build();
        return EVENT_HEARING.equalsIgnoreCase(event) && AllocatedTrack.FAST_CLAIM
            == AllocatedTrack.getAllocatedTrack(caseData);
    }
}
