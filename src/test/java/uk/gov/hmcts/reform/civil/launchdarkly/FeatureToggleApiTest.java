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

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleApiTest {

    private static final String FAKE_FEATURE = "fake-feature";
    private static final String FAKE_ENVIRONMENT = "fake-env";

    @Mock
    private LDClientInterface ldClient;

    @Captor
    private ArgumentCaptor<LDUser> ldUserArgumentCaptor;

    private FeatureToggleApi featureToggleApi;

    @BeforeEach
    void setUp() {
        featureToggleApi = new FeatureToggleApi(ldClient, FAKE_ENVIRONMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserIsProvided(Boolean toggleState) {
        LDUser ldUSer = new LDUser.Builder("civil-service")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", FAKE_ENVIRONMENT).build();
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, ldUSer)).isEqualTo(toggleState);

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

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE)).isEqualTo(toggleState);
        verifyBoolVariationCalled(FAKE_FEATURE, List.of("timestamp", "environment"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldHonourProvidedDefaultValue(boolean toggleState) {
        givenToggle(FAKE_FEATURE, true, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, true)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq(FAKE_FEATURE), any(LDUser.class), eq(true));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldHonourProvidedDefaultValueForCustomUser(boolean toggleState) {
        LDUser user = new LDUser.Builder("custom").build();
        when(ldClient.boolVariation(FAKE_FEATURE, user, true)).thenReturn(toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, user, true)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(FAKE_FEATURE, user, true);
    }

    @Test
    void shouldIncludeLocationAttributeWhenRequested() {
        givenToggle(FAKE_FEATURE, true);

        featureToggleApi.isFeatureEnabledForLocation(FAKE_FEATURE, "LON", true);

        verify(ldClient).boolVariation(eq(FAKE_FEATURE), ldUserArgumentCaptor.capture(), eq(true));
        assertThat(ImmutableList.copyOf(ldUserArgumentCaptor.getValue().getCustomAttributes()))
            .extracting("name")
            .contains("location");
    }

    @Test
    void shouldIncludeDateAttributeWhenRequested() {
        givenToggle(FAKE_FEATURE, true);

        featureToggleApi.isFeatureEnabledForDate(FAKE_FEATURE, 123L, false);

        verify(ldClient).boolVariation(eq(FAKE_FEATURE), ldUserArgumentCaptor.capture(), eq(false));
        assertThat(ImmutableList.copyOf(ldUserArgumentCaptor.getValue().getCustomAttributes()))
            .extracting("name")
            .contains("timestamp");
    }

    @Test
    void shouldCloseClientWhenShutdownHookRuns() throws Exception {
        Method close = FeatureToggleApi.class.getDeclaredMethod("close");
        close.setAccessible(true);

        close.invoke(featureToggleApi);

        verify(ldClient).close();
    }

    private void givenToggle(String feature, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDUser.class), anyBoolean()))
            .thenReturn(state);
    }

    private void givenToggle(String feature, boolean defaultValue, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDUser.class), eq(defaultValue))).thenReturn(state);
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserWithLocationIsProvided(Boolean toggleState) {
        LDUser ldUSer = new LDUser.Builder("civil-service")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", FAKE_ENVIRONMENT)
            .custom("location", "000000")
            .build();
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, ldUSer)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            ldUSer,
            false
        );
    }
}
