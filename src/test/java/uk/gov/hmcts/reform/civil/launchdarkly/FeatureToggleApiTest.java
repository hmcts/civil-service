package uk.gov.hmcts.reform.civil.launchdarkly;

import com.launchdarkly.sdk.ContextKind;
import com.launchdarkly.sdk.LDContext;
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
    private ArgumentCaptor<LDContext> ldContextArgumentCaptor;

    private FeatureToggleApi featureToggleApi;

    @BeforeEach
    void setUp() {
        featureToggleApi = new FeatureToggleApi(ldClient, FAKE_ENVIRONMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserIsProvided(Boolean toggleState) {
        LDContext context = LDContext.builder("civil-service")
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", FAKE_ENVIRONMENT).build();
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, context)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            context,
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
        verify(ldClient).boolVariation(eq(FAKE_FEATURE), any(LDContext.class), eq(true));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldHonourProvidedDefaultValueForCustomUser(boolean toggleState) {
        LDContext context = LDContext.create("custom");
        when(ldClient.boolVariation(FAKE_FEATURE, context, true)).thenReturn(toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, context, true)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(FAKE_FEATURE, context, true);
    }

    @Test
    void shouldIncludeLocationAttributeWhenRequested() {
        givenToggle(FAKE_FEATURE, true);

        featureToggleApi.isFeatureEnabledForLocation(FAKE_FEATURE, "LON", true);

        verify(ldClient).boolVariation(eq(FAKE_FEATURE), ldContextArgumentCaptor.capture(), eq(true));
        assertThat(ldContextArgumentCaptor.getValue().getValue("location").stringValue()).isEqualTo("LON");
    }

    @Test
    void shouldIncludeDateAttributeWhenRequested() {
        givenToggle(FAKE_FEATURE, true);

        featureToggleApi.isFeatureEnabledForDate(FAKE_FEATURE, 123L, false);

        verify(ldClient).boolVariation(eq(FAKE_FEATURE), ldContextArgumentCaptor.capture(), eq(false));
        assertThat(ldContextArgumentCaptor.getValue().getValue("timestamp").longValue()).isEqualTo(123L);
    }

    @Test
    void shouldCloseClientWhenShutdownHookRuns() throws Exception {
        Method close = FeatureToggleApi.class.getDeclaredMethod("close");
        close.setAccessible(true);

        close.invoke(featureToggleApi);

        verify(ldClient).close();
    }

    private void givenToggle(String feature, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDContext.class), anyBoolean()))
            .thenReturn(state);
    }

    private void givenToggle(String feature, boolean defaultValue, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDContext.class), eq(defaultValue))).thenReturn(state);
    }

    private void verifyBoolVariationCalled(String feature, List<String> customAttributesKeys) {
        verify(ldClient).boolVariation(
            eq(feature),
            ldContextArgumentCaptor.capture(),
            eq(false)
        );

        var capturedContext = ldContextArgumentCaptor.getValue();
        assertThat(capturedContext.getKey()).isEqualTo("civil-service");
        assertThat(capturedContext.getKind()).isEqualTo(ContextKind.DEFAULT);
        assertThat(capturedContext.getCustomAttributeNames()).containsExactlyInAnyOrderElementsOf(customAttributesKeys);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserWithLocationIsProvided(Boolean toggleState) {
        LDContext context = LDContext.builder("civil-service")
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", FAKE_ENVIRONMENT)
            .set("location", "000000")
            .build();
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, context)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            context,
            false
        );
    }

    @Test
    void shouldEvaluateFeatureForMultiContext() {
        LDContext userContext = LDContext.create("civil-service");
        LDContext organisationContext = LDContext.create(ContextKind.of("organisation"), "hmcts");
        LDContext multiContext = LDContext.createMulti(userContext, organisationContext);
        when(ldClient.boolVariation(FAKE_FEATURE, multiContext, false)).thenReturn(true);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, multiContext)).isTrue();

        verify(ldClient).boolVariation(FAKE_FEATURE, multiContext, false);
    }
}
