package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    @Mock
    private FeatureToggleApi featureToggleApi;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService(featureToggleApi);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenMyFeatureIsEnabledOrDisabled(Boolean toggleState) {
        givenToggle("myFeature", toggleState);

        assertThat(featureToggleService.isFeatureEnabled("myFeature")).isEqualTo(toggleState);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenAutomatedHearingNoticeEnabledInvoked(Boolean toggleStat) {
        var hearingAndListingKey = "ahn";
        givenToggle(hearingAndListingKey, toggleStat);

        assertThat(featureToggleService.isAutomatedHearingNoticeEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsCaseFlagsEnabledInvoked(Boolean toggleStat) {
        var caseFlagsKey = "case-flags";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isCaseFlagsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsGAForLipInvoked(Boolean toggleStat) {
        var caseFlagsKey = "GaForLips";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isGaForLipsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsBulkClaimInvoked(Boolean toggleStat) {
        var bulkClaimKey = "bulk_claim_enabled";
        givenToggle(bulkClaimKey, toggleStat);

        assertThat(featureToggleService.isBulkClaimEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsLipVLipInvoked(Boolean toggleStat) {
        var lipVlipKey = "cuiReleaseTwoEnabled";
        givenToggle(lipVlipKey, toggleStat);

        assertThat(featureToggleService.isLipVLipEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsDashboardServiceInvoked(Boolean toggleStat) {
        var dashboardKey = "dashboard-service";
        givenToggle(dashboardKey, toggleStat);

        assertThat(featureToggleService.isDashboardServiceEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsGeneralApplicationsEnabledInvoked(Boolean toggleStat) {
        var generalApplicationsKey = "general_applications_enabled";
        givenToggle(generalApplicationsKey, toggleStat);

        assertThat(featureToggleService.isGeneralApplicationsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsPinInPostEnabledInvoked(Boolean toggleStat) {
        var pinInPostKey = "pin-in-post";
        givenToggle(pinInPostKey, toggleStat);

        assertThat(featureToggleService.isPinInPostEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsPbaV3EnabledInvoked(Boolean toggleStat) {
        var pbaV3Key = "pba-version-3-ways-to-pay";
        givenToggle(pbaV3Key, toggleStat);

        assertThat(featureToggleService.isPbaV3Enabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenHmcIsEnabled(Boolean toggleStat) {
        var hmcKey = "hmc";
        givenToggle(hmcKey, toggleStat);

        assertThat(featureToggleService.isHmcEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenEnableRPAEmailsInvoked(Boolean toggleStat) {
        var enableRPAEmailsKey = "enable-rpa-emails";
        givenToggle(enableRPAEmailsKey, toggleStat);

        assertThat(featureToggleService.isRPAEmailEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenFastTrackUpliftsEnabled(Boolean toggleStat) {
        var caseFileKey = "fast-track-uplifts";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isFastTrackUpliftsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenUpdateContactDetailsEnabled(Boolean toggleStat) {
        var caseFileKey = "update-contact-details";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isUpdateContactDetailsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenLocationWhiteListed(Boolean toggleStat) {
        final String feature = "case-progression-location-whitelist";
        String location = "000000";
        when(featureToggleApi.isFeatureEnabledForLocation(eq(feature), eq(location), eq(true)))
            .thenReturn(toggleStat);

        assertThat(featureToggleService.isLocationWhiteListedForCaseProgression(location)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsTransferCaseOnlineEnabled(Boolean toggleStat) {
        var transferCaseOnlineKey = "isTransferOnlineCaseEnabled";
        givenToggle(transferCaseOnlineKey, toggleStat);

        assertThat(featureToggleService.isTransferOnlineCaseEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenCaseProgressionEnabled(Boolean toggleStat) {
        var caseFileKey = "cui-case-progression";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isCaseProgressionEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenEarlyAdopterEnabled(Boolean toggleStat) {
        var caseFileKey = "early-adopters";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isEarlyAdoptersEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsSdoR2Enabled(Boolean toggleStat) {
        var sdoR2Key = "isSdoR2Enabled";
        givenToggle(sdoR2Key, toggleStat);

        assertThat(featureToggleService.isSdoR2Enabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsJudgmentOnlineLive(Boolean toggleStat) {
        var isJudgmentOnlineLiveKey = "isJudgmentOnlineLive";
        givenToggle(isJudgmentOnlineLiveKey, toggleStat);

        assertThat(featureToggleService.isJudgmentOnlineLive()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsMintiEnabled(Boolean toggleStat) {
        var mintiKey = "minti";
        givenToggle(mintiKey, toggleStat);

        assertThat(featureToggleService.isMintiEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsCarmEnabled(Boolean toggleStat) {
        var carmKey = "carm";
        var carmDateKey = "cam-enabled-for-case";
        givenToggle(carmKey, toggleStat);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .setClaimTypeToSpecClaim()
            .build();

        if (toggleStat) {
            when(featureToggleApi.isFeatureEnabledForDate(eq(carmDateKey), anyLong(), eq(false)))
                .thenReturn(true);
        }

        assertThat(featureToggleService.isCarmEnabledForCase(caseData)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenMultiOrIntermediateTrackEnabled(Boolean toggleStat) {
        var mintiKey = "minti";
        var caseFileKey = "multi-or-intermediate-track";
        givenToggle(mintiKey, toggleStat);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .build();
        if (toggleStat) {
            when(featureToggleApi.isFeatureEnabledForDate(eq(caseFileKey), anyLong(), eq(false)))
                .thenReturn(true);
        }
        assertThat(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_isNationalRolloutEnabled(Boolean toggleStat) {
        var caseFlagsKey = "enable-national-rollout";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isNationalRolloutEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsPartOfNationalRollout(Boolean toggleStat) {
        final String feature = "national-rollout-whitelist";
        String location = "000000";
        when(featureToggleApi.isFeatureEnabledForLocation(eq(feature), eq(location), eq(false)))
            .thenReturn(toggleStat);

        assertThat(featureToggleService.isPartOfNationalRollout(location)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_isGenAppsAllowedPreSdo(Boolean toggleStat) {
        var caseFlagsKey = "ga-allowed-pre-sdo";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isGenAppsAllowedPreSdo()).isEqualTo(toggleStat);
    }

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(eq(feature)))
            .thenReturn(state);
    }
}
