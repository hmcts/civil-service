package uk.gov.hmcts.reform.civil.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Unit tests for {@link FeatureToggleAspect}.
 *
 * <p><strong>Technical Debt Coordination:</strong> This test class uses deprecated
 * LaunchDarkly {@code LDUser} API, which should be migrated to {@code LDContext}
 * as part of the broader authentication service modernization effort.</p>
 *
 * <p><strong>Migration Dependencies:</strong>
 * <ul>
 *   <li>{@link uk.gov.hmcts.reform.civil.service.UserService#getUserDetails(String)} → {@link uk.gov.hmcts.reform.civil.service.UserService#getUserInfo(String)}</li>
 *   <li>{@code LDUser} → {@code LDContext} (this class)</li>
 *   <li>Spring Boot 2.x → 3.x (project-wide)</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("deprecation") // Coordinated migration with UserService IDAM modernization
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    FeatureToggleAspect.class,
    FeatureToggleApi.class
})
class FeatureToggleAspectTest {

    private static final String NEW_FEATURE = "NEW_FEATURE";
    @Autowired
    FeatureToggleAspect featureToggleAspect;

    @MockBean
    LDClient ldClient;
    @MockBean
    ProceedingJoinPoint proceedingJoinPoint;
    @MockBean
    FeatureToggle featureToggle;
    @MockBean
    MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        when(featureToggle.feature()).thenReturn(NEW_FEATURE);
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("myFeatureToggledMethod");
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldProceedToMethodInvocation_whenFeatureToggleIsEnabled(Boolean state) {
        when(featureToggle.value()).thenReturn(state);
        givenToggle(state);

        featureToggleAspect.checkFeatureEnabled(proceedingJoinPoint, featureToggle);

        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotProceedToMethodInvocation_whenFeatureToggleIsDisabled(Boolean state) {
        when(featureToggle.value()).thenReturn(state);
        givenToggle(!state);

        featureToggleAspect.checkFeatureEnabled(proceedingJoinPoint, featureToggle);

        verify(proceedingJoinPoint, never()).proceed();
    }

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(eq(FeatureToggleAspectTest.NEW_FEATURE), any(LDUser.class), anyBoolean()))
            .thenReturn(state);
    }
}
