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
        return false;
    }

    public boolean isCaseFlagsEnabled() {
        return false;
    }

    public boolean isPinInPostEnabled() {
        return false;
    }

    public boolean isPbaV3Enabled() {
        return true;
    }

    public boolean isCertificateOfServiceEnabled() {
        return false;
    }

    public boolean isRPAEmailEnabled() {
        return true;
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
        return flse;
    }

    public boolean isUpdateContactDetailsEnabled() {
        return false;
    }

    public boolean isLipVLipEnabled() {
        return false;
    }
}
