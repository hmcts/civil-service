package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.ZoneId;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;

    public boolean isFeatureEnabled(String feature) {
        if (feature.equals("isSdoR2Enabled") || feature.equals("bulk_claim_enabled")
            || feature.equals("hmc") || feature.equals("ahn") || feature.equals("update-contact-details")
            || feature.equals("cuiReleaseTwoEnabled")
            || feature.equals("cui-case-progression")
            || feature.equals("carm")
            || feature.equals("cam-enabled-for-case")) {
            return false;
        }
        return this.featureToggleApi.isFeatureEnabled(feature);
    }

    public boolean isGeneralApplicationsEnabled() {
        return true;
    }

    public boolean isBulkClaimEnabled() {
        return false;
    }

    public boolean isCaseFlagsEnabled() {
        return true;
    }

    public boolean isPinInPostEnabled() {
        return true;
    }

    public boolean isPbaV3Enabled() {
        return true;
    }

    public boolean isRPAEmailEnabled() {
        return true;
    }

    public boolean isHmcEnabled() {
        return false;
    }

    public boolean isCaseFileViewEnabled() {
        return true;
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

    public boolean isLocationWhiteListedForCaseProgression(String locationEpimms) {
        return
            // because default value is true
            locationEpimms != null
                && true;
    }

    public boolean isTransferOnlineCaseEnabled() {
        return true;
    }

    public boolean isCaseProgressionEnabled() {
        return false;
    }

    public boolean isEarlyAdoptersEnabled() {
        return true;
    }

    public boolean isSdoR2Enabled() {
        return false;
    }

    public boolean isCarmEnabledForCase(CaseData caseData) {
        return false;
    }
}
