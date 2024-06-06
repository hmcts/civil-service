package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;

    List<String> featureToggleOffInProd = List.of("minti",
                                                  "isJudgmentOnlineLive", "GaForLips", "dashboard-service", "multi-or-intermediate-track",
                                                  "cam-enabled-for-case",
                                                  "update-contact-details",
                                                  "cuiReleaseTwoEnabled", "bulk_claim_enabled", "carm", "shutter-pcq", "ahn",
                                                  "cui-case-progression", "hmc");

    public boolean isFeatureEnabled(String feature) {
        if (feature.equals("isSdoR2Enabled")) {
            return true;
        }
        if (featureToggleOffInProd.contains(feature)) {
            return false;
        }
        return this.featureToggleApi.isFeatureEnabled(feature);
    }

    public boolean isGeneralApplicationsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("general_applications_enabled");
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

    public boolean isDashboardServiceEnabled() {
        return false;
    }

    public boolean isLocationWhiteListedForCaseProgression(String locationEpimms) {
        return
            // because default value is true
            locationEpimms != null
                && featureToggleApi
                .isFeatureEnabledForLocation(
                    "case-progression-location-whitelist",
                    locationEpimms,
                    true
                );
    }

    public boolean isTransferOnlineCaseEnabled() {
        return featureToggleApi.isFeatureEnabled("isTransferOnlineCaseEnabled");
    }

    public boolean isCaseProgressionEnabled() {
        return false;
    }

    public boolean isEarlyAdoptersEnabled() {
        return featureToggleApi.isFeatureEnabled("early-adopters");
    }

    public boolean isSdoR2Enabled() {
        return true; //featureToggleApi.isFeatureEnabled("isSdoR2Enabled");
    }

    public boolean isJudgmentOnlineLive() {
        return false;
    }

    public boolean isMintiEnabled() {
        return false;
    }

    public boolean isCarmEnabledForCase(CaseData caseData) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        boolean isSpecClaim = SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
        return false;
    }

    public boolean isGaForLipsEnabled() {
        return false;
    }

    public boolean isMultiOrIntermediateTrackEnabled(CaseData caseData) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch;
        if (caseData.getSubmittedDate() == null) {
            epoch = LocalDateTime.now().atZone(zoneId).toEpochSecond();
        } else {
            epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        }
        return false;
    }
}
