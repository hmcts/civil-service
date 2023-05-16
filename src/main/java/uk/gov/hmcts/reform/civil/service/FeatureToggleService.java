package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;

    public boolean isFeatureEnabled(String feature) {
        return this.featureToggleApi.isFeatureEnabled(feature);
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

    public boolean isRPAEmailEnabled() {
        return this.featureToggleApi.isFeatureEnabled("enable-rpa-emails");
    }

    public boolean isHmcEnabled() {
        return this.featureToggleApi.isFeatureEnabled("hmc");
    }

    public boolean isCaseFileViewEnabled() {
        return this.featureToggleApi.isFeatureEnabled("case-file-view");
    }

}
