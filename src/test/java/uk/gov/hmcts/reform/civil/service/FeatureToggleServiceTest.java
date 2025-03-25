package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void shouldReturnCorrectValue_whenIsGAForLipInvoked(Boolean toggleStat) {
        var caseFlagsKey = "GaForLips";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isGaForLipsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsAmendBundleEnabledInvoked(Boolean toggleStat) {
        var caseFlagsKey = "amend-bundle-enabled";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isAmendBundleEnabled()).isEqualTo(toggleStat);
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
    void shouldReturnCorrectValue_whenLocationWhiteListed(Boolean toggleStat) {
        final String feature = "case-progression-location-whitelist";
        String location = "000000";
        when(featureToggleApi.isFeatureEnabledForLocation(eq(feature), eq(location), eq(true)))
            .thenReturn(toggleStat);

        assertThat(featureToggleService.isLocationWhiteListedForCaseProgression(location)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @CsvSource({
        "someLocation, true, true",
        "someLocation, false, false",
        ", true, false",
        ", false, false"
    })
    void shouldReturnCorrectValueBasedOnLocationAndFeatureToggleForGaLips(String location, boolean isFeatureEnabled, boolean expected) {

        if (isFeatureEnabled && location != null) {
            when(featureToggleApi.isFeatureEnabledForLocation(
                "ea-courts-whitelisted-for-ga-lips",
                location,
                false
            )).thenReturn(isFeatureEnabled);
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(isFeatureEnabled);
        }

        boolean result = featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(location);

        assertEquals(expected, result);
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
    void shouldReturnCorrectValue_whenisCjesServiceAvailableEnabled(Boolean toggleStat) {
        var key = "isCjesServiceAvailable";
        givenToggle(key, toggleStat);

        assertThat(featureToggleService.isCjesServiceAvailable()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsCarmEnabled(Boolean toggleStat) {
        var carmDateKey = "cam-enabled-for-case";

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
        var caseFileKey = "multi-or-intermediate-track";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .build();
        if (toggleStat) {
            when(featureToggleApi.isFeatureEnabledForDate(eq(caseFileKey), anyLong(), eq(false)))
                .thenReturn(true);
        }
        assertThat(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).isEqualTo(toggleStat);
    }

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(eq(feature)))
            .thenReturn(state);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsDashboardEnabledForCase(Boolean toggleStat) {
        var cuiReKey = "cuiReleaseTwoEnabled";
        var dashboardKey = "is-dashboard-enabled-for-case";
        givenToggle(cuiReKey, toggleStat);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .setClaimTypeToSpecClaim()
            .build();

        if (toggleStat) {
            when(featureToggleApi.isFeatureEnabledForDate(eq(dashboardKey), anyLong(), eq(false)))
                .thenReturn(true);
        }

        assertThat(featureToggleService.isDashboardEnabledForCase(caseData)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsCoSCEnabled(Boolean toggleStat) {
        var isCoSCEnabledKey = "isCoSCEnabled";
        givenToggle(isCoSCEnabledKey, toggleStat);

        assertThat(featureToggleService.isCoSCEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @CsvSource({
        "someLocation, true, true",
        "someLocation, false, false",
        ", true, false",
        ", false, false"
    })
    void shouldReturnCorrectValueBasedOnLocationAndFeatureToggle(String location, boolean isFeatureEnabled, boolean expected) {

        if (isFeatureEnabled && location != null) {
            when(featureToggleApi.isFeatureEnabledForLocation(
                "case-progression-location-whitelist",
                location,
                true
            )).thenReturn(isFeatureEnabled);
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isFeatureEnabled);
        }

        boolean result = featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(location);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenisJOLiveFeedActive(Boolean toggleStat) {
        when(featureToggleService.isJudgmentOnlineLive())
            .thenReturn(toggleStat);
        when(featureToggleService.isJOLiveFeedActive())
            .thenReturn(toggleStat);
        assertThat(featureToggleService.isJOLiveFeedActive()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsDefendantNoCOnlineForCase(Boolean toggleStat) {
        var nocOnlineKey = "is-defendant-noc-online-for-case";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .setClaimTypeToSpecClaim()
            .build();

        when(featureToggleApi.isFeatureEnabledForDate(eq(nocOnlineKey), anyLong(), eq(false)))
            .thenReturn(toggleStat);

        assertThat(featureToggleService.isDefendantNoCOnlineForCase(caseData)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsHmcForLipEnabled(Boolean toggleStat) {
        var hmcCui = "hmc-cui-enabled";
        givenToggle(hmcCui, toggleStat);

        assertThat(featureToggleService.isHmcForLipEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsQMForLRs(Boolean toggleStat) {
        var caseFlagsKey = "query-management";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isQueryManagementLRsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsQMForLiPs(Boolean toggleStat) {
        var caseFlagsKey = "query-management-lips";
        givenToggle(caseFlagsKey, toggleStat);

        assertThat(featureToggleService.isQueryManagementLipEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenQMApplicableLiPCase(Boolean toggleStat) {
        var caseFileKey = "cui-query-management";

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .build();
        if (toggleStat) {
            when(featureToggleApi.isFeatureEnabledForDate(eq(caseFileKey), anyLong(), eq(false)))
                .thenReturn(true);
        }
        assertThat(featureToggleService.isQMApplicableLiPCase(caseData)).isEqualTo(toggleStat);
    }
}
