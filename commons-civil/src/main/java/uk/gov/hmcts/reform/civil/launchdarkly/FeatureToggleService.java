package uk.gov.hmcts.reform.civil.launchdarkly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;

    public boolean isRpaContinuousFeedEnabled() {
        return this.featureToggleApi.isFeatureEnabled("rpaContinuousFeed");
    }

    public boolean isSpecRpaContinuousFeedEnabled() {
        return this.featureToggleApi.isFeatureEnabled("specified-rpa-continuous-feed");
    }

    public boolean isGlobalSearchEnabled() {
        return this.featureToggleApi.isFeatureEnabled("global-search-specified");
    }

    public boolean isSdoEnabled() {
        return this.featureToggleApi.isFeatureEnabled("enableSDO");
    }

    public boolean isGeneralApplicationsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("general_applications_enabled");
    }

    public boolean isNoticeOfChangeEnabled() {
        return this.featureToggleApi.isFeatureEnabled("notice-of-change");
    }

    public boolean isHearingAndListingSDOEnabled() {
        return this.featureToggleApi.isFeatureEnabled("hearing-and-listing-sdo");
    }

    public boolean isHearingAndListingLegalRepEnabled() {
        return this.featureToggleApi.isFeatureEnabled("hearing-and-listing-legal-rep");
    }

    public boolean isCourtLocationDynamicListEnabled() {
        return this.featureToggleApi.isFeatureEnabled("court-location-dynamic-list");
    }

    public boolean isCaseFlagsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("case-flags");
    }

    public boolean isPinInPostEnabled() {
        return this.featureToggleApi.isFeatureEnabled("pin-in-post");
    }

    public boolean isPbaV3Enabled() {
        return this.featureToggleApi.isFeatureEnabled("pba-version-3-ways-to-pay");
    }

    public boolean isSDOEnabled() {
        return this.featureToggleApi.isFeatureEnabled("enableSDO");
    }

    public boolean isCertificateOfServiceEnabled() {
        return this.featureToggleApi.isFeatureEnabled("isCertificateOfServiceEnabled");
    }

    public boolean isHmcEnabled() {
        return this.featureToggleApi.isFeatureEnabled("hmc");
    }

}
