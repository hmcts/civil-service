package uk.gov.hmcts.reform.civil.launchdarkly;

import com.google.common.collect.ImmutableList;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
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
@SuppressWarnings("deprecation")
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
        givenToggle(toggleState);

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
        givenToggle(toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE)).isEqualTo(toggleState);
        verifyBoolVariationCalled(List.of("timestamp", "environment"));
    }

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(eq(FeatureToggleApiTest.FAKE_FEATURE), any(LDUser.class), anyBoolean()))
            .thenReturn(state);
    }

    private void verifyBoolVariationCalled(List<String> customAttributesKeys) {
        verify(ldClient).boolVariation(
            eq(FeatureToggleApiTest.FAKE_FEATURE),
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
        givenToggle(toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, ldUSer)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            ldUSer,
            false
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenDefaultValueProvided(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, true)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            eq(FAKE_FEATURE),
            ldUserArgumentCaptor.capture(),
            eq(true)
        );

        var capturedLdUser = ldUserArgumentCaptor.getValue();
        assertThat(capturedLdUser.getKey()).isEqualTo("civil-service");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserAndDefaultValueProvided(Boolean toggleState) {
        LDUser ldUser = new LDUser.Builder("test-user").build();
        givenToggle(toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, ldUser, true)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            ldUser,
            true
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenLocationProvided(Boolean toggleState) {
        String location = "test-location";
        givenToggle(toggleState);

        assertThat(featureToggleApi.isFeatureEnabledForLocation(FAKE_FEATURE, location, true)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            eq(FAKE_FEATURE),
            ldUserArgumentCaptor.capture(),
            eq(true)
        );

        var capturedLdUser = ldUserArgumentCaptor.getValue();
        assertThat(capturedLdUser.getKey()).isEqualTo("civil-service");
        assertThat(ImmutableList.copyOf(capturedLdUser.getCustomAttributes())).hasSize(3); // timestamp, environment, location
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenDateProvided(Boolean toggleState) {
        Long date = System.currentTimeMillis();
        givenToggle(toggleState);

        assertThat(featureToggleApi.isFeatureEnabledForDate(FAKE_FEATURE, date, true)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            eq(FAKE_FEATURE),
            ldUserArgumentCaptor.capture(),
            eq(true)
        );

        var capturedLdUser = ldUserArgumentCaptor.getValue();
        assertThat(capturedLdUser.getKey()).isEqualTo("civil-service");
        assertThat(ImmutableList.copyOf(capturedLdUser.getCustomAttributes())).hasSize(2); // timestamp (overridden), environment
    }
}
