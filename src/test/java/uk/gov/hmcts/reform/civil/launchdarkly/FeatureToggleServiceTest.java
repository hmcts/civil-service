package uk.gov.hmcts.reform.civil.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    private static final String FAKE_FEATURE = "fake-feature";
    private static final String ENVIRONMENT = "fake-env";
    private static final LDUser LD_USER = new LDUser.Builder("civil-service")
        .custom("timestamp", String.valueOf(System.currentTimeMillis()))
        .custom("environment", ENVIRONMENT).build();

    @Mock
    private LDClientInterface ldClient;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService(ldClient, ENVIRONMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserIsProvided(Boolean toggleState) {
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleService.isFeatureEnabled(FAKE_FEATURE, LD_USER)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            LD_USER,
            false
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenDefaultServiceUser(Boolean toggleState) {
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleService.isFeatureEnabled(FAKE_FEATURE)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            eq(FAKE_FEATURE),
            any(LDUser.class),
            eq(false)
        );
    }

    private void givenToggle(String feature, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDUser.class), anyBoolean()))
            .thenReturn(state);
    }
}
