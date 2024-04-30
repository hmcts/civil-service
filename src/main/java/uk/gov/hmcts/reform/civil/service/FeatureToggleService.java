package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

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
        return this.featureToggleApi.isFeatureEnabled("hmc");
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
        return featureToggleApi.isFeatureEnabled("cuiReleaseTwoEnabled");
    }

    public boolean isDashboardServiceEnabled() {
        return featureToggleApi.isFeatureEnabled("dashboard-service");
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
        return featureToggleApi.isFeatureEnabled("cui-case-progression");
    }

    public boolean isEarlyAdoptersEnabled() {
        return featureToggleApi.isFeatureEnabled("early-adopters");
    }

    public boolean isSdoR2Enabled() {
        return featureToggleApi.isFeatureEnabled("isSdoR2Enabled");
    }

    public boolean isJudgmentOnlineLive() {
        return featureToggleApi.isFeatureEnabled("isJudgmentOnlineLive");
    }

    public boolean isMintiEnabled() {
        return featureToggleApi.isFeatureEnabled("minti");
    }

    public boolean isCarmEnabledForCase(CaseData caseData) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        boolean isSpecClaim = SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
        return isSpecClaim && featureToggleApi.isFeatureEnabled("carm")
            && featureToggleApi.isFeatureEnabledForDate("cam-enabled-for-case",
                                                        epoch, false);
    }

    public boolean isMultiOrIntermediateTrackEnabled(CaseData caseData) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch;
        if (caseData.getSubmittedDate() == null) {
            epoch = LocalDateTime.now().atZone(zoneId).toEpochSecond();
        } else {
            epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        }
        return featureToggleApi.isFeatureEnabled("minti")
            && featureToggleApi.isFeatureEnabledForDate("multi-or-intermediate-track", epoch, false);
    }
}
