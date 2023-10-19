package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleApi;

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldReturnCorrectValue_whenIsNoticeOfChangeEnabledInvoked(Boolean toggleState) {
        var noticeOfChangeKey = "notice-of-change";
        givenToggle(noticeOfChangeKey, toggleState);

        assertThat(featureToggleService.isNoticeOfChangeEnabled()).isEqualTo(toggleState);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenAutomatedHearingNoticeEnabledInvoked(Boolean toggleStat) {
        var hearingAndListingKey = "ahn";
        givenToggle(hearingAndListingKey, toggleStat);

        assertThat(featureToggleService.isAutomatedHearingNoticeEnabled()).isEqualTo(toggleStat);
    }

    // @ParameterizedTest
    // @ValueSource(booleans = {true, false})
    // void shouldReturnCorrectValue_whenIsCaseFlagsEnabledInvoked(Boolean toggleStat) {
    //     var caseFlagsKey = "case-flags";
    //     givenToggle(caseFlagsKey, toggleStat);
    //    assertThat(featureToggleService.isCaseFlagsEnabled()).isEqualTo(toggleStat);
    // }

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
    void shouldReturnCorrectValue_whenIsCertificateOfServiceEnabledInvoked(Boolean toggleStat) {
        var certificateOfServiceKey = "isCertificateOfServiceEnabled";
        givenToggle(certificateOfServiceKey, toggleStat);

        assertThat(featureToggleService.isCertificateOfServiceEnabled()).isEqualTo(toggleStat);
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
    void shouldReturnCorrectValue_whenCaseFileViewEnabled(Boolean toggleStat) {
        var caseFileKey = "case-file-view";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isCaseFileViewEnabled()).isEqualTo(toggleStat);
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

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(eq(feature)))
            .thenReturn(state);
    }
}
