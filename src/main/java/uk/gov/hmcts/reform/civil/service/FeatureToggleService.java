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

    public boolean isGeneralApplicationsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("general_applications_enabled");
    }

    public boolean isBulkClaimEnabled() {
        return this.featureToggleApi.isFeatureEnabled("bulk_claim_enabled");
    }

    public boolean isNoticeOfChangeEnabled() {
        return this.featureToggleApi.isFeatureEnabled("notice-of-change");
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

    public boolean isAutomatedHearingNoticeEnabled() {
        return this.featureToggleApi.isFeatureEnabled("ahn");
    }

    public boolean isFastTrackUpliftsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("fast-track-uplifts");
    }

    public boolean isUpdateContactDetailsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("update-contact-details");
    }

    public boolean isLipVLipEnabled() {
        return featureToggleApi.isFeatureEnabled("cuiReleaseTwoEnabled");;
    }

    public boolean isLocationWhiteListedForCaseProgression(String locationEpimms) {
        return featureToggleApi.isFeatureEnabledForLocation("case-progression-location-whitelist", locationEpimms,
                                                            true);
    }

    public boolean isTransferOnlineCaseEnabled() {
        return featureToggleApi.isFeatureEnabled("isTransferOnlineCaseEnabled");
    }

    public boolean isCaseProgressionEnabled() {
        return featureToggleApi.isFeatureEnabled("cui-case-progression");
    }
}
