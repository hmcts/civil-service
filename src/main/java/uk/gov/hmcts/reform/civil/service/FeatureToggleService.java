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
        return false;
    }

    public boolean isBulkClaimEnabled() {
        return false;
    }

    public boolean isNoticeOfChangeEnabled() {
        return this.featureToggleApi.isFeatureEnabled("notice-of-change");
    }

    public boolean isCaseFlagsEnabled() {
        return false;
    }

    public boolean isPinInPostEnabled() {
        return false;
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
        return false;
    }

    public boolean isCaseFileViewEnabled() {
        return false;
    }

    public boolean isAutomatedHearingNoticeEnabled() {
        return false;
    }

    public boolean isFastTrackUpliftsEnabled() {
        return true;
    }

    public boolean isUpdateContactDetailsEnabled() {
        return false;
    }

    public boolean isLipVLipEnabled() {
        return false;
    }
}
