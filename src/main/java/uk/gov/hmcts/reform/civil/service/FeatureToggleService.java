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

    public boolean isPinInPostEnabled() {
        return this.featureToggleApi.isFeatureEnabled("pin-in-post");
    }

    public boolean isRPAEmailEnabled() {
        return this.featureToggleApi.isFeatureEnabled("enable-rpa-emails");
    }

    public boolean isFastTrackUpliftsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("fast-track-uplifts");
    }

    public boolean isLipVLipEnabled() {
        return featureToggleApi.isFeatureEnabled("cuiReleaseTwoEnabled");
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

    public boolean isSdoR2Enabled() {
        return featureToggleApi.isFeatureEnabled("isSdoR2Enabled");
    }

    public boolean isJudgmentOnlineLive() {
        return featureToggleApi.isFeatureEnabled("isJudgmentOnlineLive");
    }

    public boolean isMintiEnabled() {
        return featureToggleApi.isFeatureEnabled("minti");
    }

    public boolean isCjesServiceAvailable() {
        return featureToggleApi.isFeatureEnabled("isCjesServiceAvailable");
    }

    public boolean isCarmEnabledForCase(CaseData caseData) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        boolean isSpecClaim = SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
        return isSpecClaim
            && featureToggleApi.isFeatureEnabledForDate("cam-enabled-for-case",
                                                        epoch, false);
    }

    public boolean isGaForLipsEnabled() {
        return featureToggleApi.isFeatureEnabled("GaForLips");
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

    public boolean isDashboardEnabledForCase(CaseData caseData) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch;
        if (caseData.getSubmittedDate() == null) {
            epoch = LocalDateTime.now().atZone(zoneId).toEpochSecond();
        } else {
            epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        }
        return featureToggleApi.isFeatureEnabled("cuiReleaseTwoEnabled")
            && featureToggleApi.isFeatureEnabledForDate("is-dashboard-enabled-for-case", epoch, false);
    }

    public boolean isCaseEventsEnabled() {
        return featureToggleApi.isFeatureEnabled("cui-case-events-enabled");
    }

    public boolean isAmendBundleEnabled() {
        return featureToggleApi.isFeatureEnabled("amend-bundle-enabled");
    }

    public boolean isCoSCEnabled() {
        return featureToggleApi.isFeatureEnabled("isCoSCEnabled");
    }

    public boolean isCaseProgressionEnabledAndLocationWhiteListed(String location) {
        return location != null
            && featureToggleApi.isFeatureEnabledForLocation("case-progression-location-whitelist", location, true)
            && isCaseProgressionEnabled();
    }

    public boolean isGaForLipsEnabledAndLocationWhiteListed(String location) {
        return location != null
            && featureToggleApi.isFeatureEnabledForLocation("ea-courts-whitelisted-for-ga-lips", location, false)
            && isGaForLipsEnabled();
    }

    public boolean isHmcNroEnabled() {
        return featureToggleApi.isFeatureEnabled("hmc-nro");

    }

    public boolean isJOLiveFeedActive() {
        return isJudgmentOnlineLive()
            && featureToggleApi.isFeatureEnabled("isJOLiveFeedActive");
    }

    public boolean isDefendantNoCOnlineForCase(CaseData caseData)  {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch;
        if (caseData.getSubmittedDate() == null) {
            epoch = LocalDateTime.now().atZone(zoneId).toEpochSecond();
        } else {
            epoch = caseData.getSubmittedDate().atZone(zoneId).toEpochSecond();
        }
        return true;
    }

    public boolean isHmcForLipEnabled() {
        return featureToggleApi.isFeatureEnabled("hmc-cui-enabled");
    }
}
