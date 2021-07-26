package uk.gov.hmcts.reform.civil.launchdarkly;

import com.google.common.collect.ImmutableList;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    private static final String FAKE_FEATURE = "fake-feature";
    private static final String FAKE_ENVIRONMENT = "fake-env";

    @Mock
    private LDClientInterface ldClient;

    @Captor
    private ArgumentCaptor<LDUser> ldUserArgumentCaptor;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService(ldClient, FAKE_ENVIRONMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserIsProvided(Boolean toggleState) {
        LDUser ldUSer = new LDUser.Builder("civil-service")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", FAKE_ENVIRONMENT).build();
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleService.isFeatureEnabled(FAKE_FEATURE, ldUSer)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            ldUSer,
            false
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenDefaultServiceUser(Boolean toggleState) {
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleService.isFeatureEnabled(FAKE_FEATURE)).isEqualTo(toggleState);
        verifyBoolVariationCalled(FAKE_FEATURE, List.of("timestamp", "environment"));
    }

    //    @Test
    //    void shouldCallBoolVariation_whenIsMultipartyEnabledInvoked() {
    //        var multipartyFeatureKey = "multiparty";
    //        givenToggle(multipartyFeatureKey, true);
    //
    //        assertThat(featureToggleService.isMultipartyEnabled()).isTrue();
    //        verifyBoolVariationCalled(multipartyFeatureKey, List.of("timestamp", "environment"));
    //    }

    @Test
    void shouldCallBoolVariation_whenIsRpaContinuousFeedEnabledInvoked() {
        var multipartyFeatureKey = "rpaContinuousFeed";
        givenToggle(multipartyFeatureKey, true);

        assertThat(featureToggleService.isRpaContinuousFeedEnabled()).isTrue();
        verifyBoolVariationCalled(multipartyFeatureKey, List.of("timestamp", "environment"));
    }

    @Test
    void shouldCallBoolVariation_whenIsOrganisationOnboardedInvoked() {
        var organisationOnboardedFeatureKey = "isOrganisationOnboarded";
        givenToggle(organisationOnboardedFeatureKey, true);

        assertThat(featureToggleService.isOrganisationOnboarded("someId")).isTrue();
        verifyBoolVariationCalled(organisationOnboardedFeatureKey, List.of("timestamp", "environment", "orgId"));
    }

    private void givenToggle(String feature, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDUser.class), anyBoolean()))
            .thenReturn(state);
    }

    private void verifyBoolVariationCalled(String feature, List<String> customAttributesKeys) {
        verify(ldClient).boolVariation(
            eq(feature),
            ldUserArgumentCaptor.capture(),
            eq(false)
        );

        var capturedLdUser = ldUserArgumentCaptor.getValue();
        assertThat(capturedLdUser.getKey()).isEqualTo("civil-service");
        assertThat(ImmutableList.copyOf(capturedLdUser.getCustomAttributes())).extracting("name")
            .containsOnlyOnceElementsOf(customAttributesKeys);
    }
}
