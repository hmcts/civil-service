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

    public boolean isRpaContinuousFeedEnabled() {
        return true;
    }

    public boolean isSpecRpaContinuousFeedEnabled() {
        return true;
    }

    public boolean isGlobalSearchEnabled() {
        return true;
    }

    public boolean isSdoEnabled() {
        return true;
    }

    public boolean isGeneralApplicationsEnabled() {
        return false;
    }

    public boolean isNoticeOfChangeEnabled() {
        return false;
    }

    public boolean isHearingAndListingSDOEnabled() {
        return false;
    }

    public boolean isHearingAndListingLegalRepEnabled() {
        return false;
    }

    public boolean isCourtLocationDynamicListEnabled() {
        return true;
    }

    public boolean isCaseFlagsEnabled() {
        return true;
    }

    public boolean isPinInPostEnabled() {
        return false;
    }

    public boolean isPbaV3Enabled() {
        return false;
    }

    public boolean isSDOEnabled() {
        return true;
    }

    public boolean isCertificateOfServiceEnabled() {
        return false;
    }

    public boolean isHmcEnabled() {
        return false;
    }

}
