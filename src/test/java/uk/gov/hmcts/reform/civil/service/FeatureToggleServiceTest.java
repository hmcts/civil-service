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
    void shouldReturnCorrectValue_whenIsRpaContinuousFeedEnabledInvoked(Boolean toggleState) {
        var multipartyFeatureKey = "rpaContinuousFeed";
        givenToggle(multipartyFeatureKey, toggleState);

        assertThat(featureToggleService.isRpaContinuousFeedEnabled()).isEqualTo(toggleState);
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
    void shouldReturnCorrectValue_whenIsHearingAndListingSDOEnabledInvoked(Boolean toggleStat) {
        var hearingAndListingKey = "hearing-and-listing-sdo";
        givenToggle(hearingAndListingKey, toggleStat);

        assertThat(featureToggleService.isHearingAndListingSDOEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenIsHearingAndListingLegalRepEnabledInvoked(Boolean toggleStat) {
        var hearingAndListingKey = "hearing-and-listing-legal-rep";
        givenToggle(hearingAndListingKey, toggleStat);

        assertThat(featureToggleService.isHearingAndListingLegalRepEnabled()).isEqualTo(toggleStat);
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
    void shouldReturnCorrectValue_whenIsSDOEnabledInvoked(Boolean toggleStat) {
        var enableSDOKey = "enableSDO";
        givenToggle(enableSDOKey, toggleStat);

        assertThat(featureToggleService.isSDOEnabled()).isEqualTo(toggleStat);
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
    public void rpaContinuousFeed_LDTagName(Boolean toggleStat) {
        var rpaContinuousFeed = "specified-rpa-continuous-feed";
        givenToggle(rpaContinuousFeed, toggleStat);
        assertThat(featureToggleService.isSpecRpaContinuousFeedEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void globalSearch_LDTagName(Boolean toggleStat) {
        givenToggle("global-search-specified", toggleStat);
        assertThat(featureToggleService.isGlobalSearchEnabled()).isEqualTo(toggleStat);
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

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(eq(feature)))
            .thenReturn(state);
    }
}
