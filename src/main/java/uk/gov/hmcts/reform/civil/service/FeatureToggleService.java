package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;
    List<String> featureToggleOffInProd = List.of("general_applications_enabled",
                                                  "bulk_claim_enabled", "hmc", "ahn", "update-contact-details",
                                                  "cuiReleaseTwoEnabled",
                                                  "isTransferOnlineCaseEnabled",
                                                  "cui-case-progression", "isSdoR2Enabled", "carm");

    public boolean isFeatureEnabled(String feature) {
        if (featureToggleOffInProd.contains(feature)) {
            return false;
        }
        return this.featureToggleApi.isFeatureEnabled(feature);
    }

    public boolean isGeneralApplicationsEnabled() {
        return false;
    }

    public boolean isBulkClaimEnabled() {
        return false;
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

    public boolean isRPAEmailEnabled() {
        return this.featureToggleApi.isFeatureEnabled("enable-rpa-emails");
    }

    public boolean isHmcEnabled() {
        return false;
    }

    public boolean isCaseFileViewEnabled() {
        return this.featureToggleApi.isFeatureEnabled("case-file-view");
    }

    public boolean isAutomatedHearingNoticeEnabled() {
        return false;
    }

    public boolean isFastTrackUpliftsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("fast-track-uplifts");
    }

    public boolean isUpdateContactDetailsEnabled() {
        return false;
    }

    public boolean isLipVLipEnabled() {
        return false;
    }

    public boolean isLocationWhiteListedForCaseProgression(String locationEpimms) {
        return featureToggleApi.isFeatureEnabledForLocation("case-progression-location-whitelist", locationEpimms,
                                                            true);
    }

    public boolean isTransferOnlineCaseEnabled() {
        return false;
    }

    public boolean isCaseProgressionEnabled() {
        return false;
    }

    public boolean isEarlyAdoptersEnabled() {
        return featureToggleApi.isFeatureEnabled("early-adopters");
    }

    public boolean isSdoR2Enabled() {
        return false;
    }

    public boolean isCarmEnabledForCase(LocalDateTime submittedDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch = submittedDate.atZone(zoneId).toEpochSecond();
        return false;
    }
}
