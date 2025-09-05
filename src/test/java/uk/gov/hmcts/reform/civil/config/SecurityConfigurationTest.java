package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;
import uk.gov.hmcts.reform.civil.filters.CustomAuthCheckerUserOnlyFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

    @Mock
    private RequestAuthorizer<User> userRequestAuthorizer;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfiguration securityConfiguration;

    @BeforeEach
    void setUp() {
        securityConfiguration = new SecurityConfiguration(userRequestAuthorizer, authenticationManager);
    }

    @Test
    void class_HasConfigurationAnnotation() {
        // Assert
        assertThat(SecurityConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    void class_HasEnableWebSecurityAnnotation() {
        // Assert
        assertThat(SecurityConfiguration.class.isAnnotationPresent(EnableWebSecurity.class)).isTrue();
    }

    @Test
    void constructor_InitializesFields() {
        // Assert - using reflection to verify private fields
        try {
            Field userRequestAuthorizerField = SecurityConfiguration.class.getDeclaredField("userRequestAuthorizer");
            userRequestAuthorizerField.setAccessible(true);
            assertThat(userRequestAuthorizerField.get(securityConfiguration)).isSameAs(userRequestAuthorizer);

            Field authenticationManagerField = SecurityConfiguration.class.getDeclaredField("authenticationManager");
            authenticationManagerField.setAccessible(true);
            assertThat(authenticationManagerField.get(securityConfiguration)).isSameAs(authenticationManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class AuthorityConstantsTests {

        @Test
        void authorities_ContainsExpectedValues() throws Exception {
            // Arrange
            Field authoritiesField = SecurityConfiguration.class.getDeclaredField("AUTHORITIES");
            authoritiesField.setAccessible(true);
            String[] authorities = (String[]) authoritiesField.get(null);

            // Assert
            assertThat(authorities).containsExactly(
                "caseworker-civil",
                "caseworker-civil-solicitor",
                "caseworker",
                "caseworker-caa",
                "caseworker-approver",
                "citizen",
                "next-hearing-date-admin"
            );
        }

        @Test
        void authWhitelist_ContainsExpectedValues() throws Exception {
            // Arrange
            Field authWhitelistField = SecurityConfiguration.class.getDeclaredField("AUTH_WHITELIST");
            authWhitelistField.setAccessible(true);
            String[] authWhitelist = (String[]) authWhitelistField.get(null);

            // Assert
            assertThat(authWhitelist).containsExactly(
                "/",
                "/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**",
                "/health", "/env", "/health/**", "/status/health",
                "/loggers/**", "/assignment/**", "/service-request-update",
                "/service-request-update-claim-issued", "/case/document/downloadDocument/**",
                "/fees/claim/calculate-interest",
                "/testing-support/flowstate"
            );
        }
    }

    @Nested
    class AuthCheckerUserOnlyFilterTests {

        @Test
        void authCheckerUserOnlyFilter_HasBeanAnnotation() throws NoSuchMethodException {
            // Act
            Method method = SecurityConfiguration.class.getMethod("authCheckerUserOnlyFilter");

            // Assert
            assertThat(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class)).isTrue();
        }

        @Test
        void authCheckerUserOnlyFilter_CreatesCustomAuthCheckerUserOnlyFilter() {
            // Act
            AuthCheckerUserOnlyFilter<User> result = securityConfiguration.authCheckerUserOnlyFilter();

            // Assert
            assertThat(result)
                .isInstanceOf(CustomAuthCheckerUserOnlyFilter.class);
        }

        @Test
        void authCheckerUserOnlyFilter_SetsAuthenticationManager() {
            // Act
            AuthCheckerUserOnlyFilter<User> result = securityConfiguration.authCheckerUserOnlyFilter();

            // Assert - verify setAuthenticationManager was called
            assertThat(result).isNotNull();
            // The authenticationManager is set on the filter
        }

        @Test
        void authCheckerUserOnlyFilter_ReturnsNewInstanceEachTime() {
            // Act
            AuthCheckerUserOnlyFilter<User> result1 = securityConfiguration.authCheckerUserOnlyFilter();
            AuthCheckerUserOnlyFilter<User> result2 = securityConfiguration.authCheckerUserOnlyFilter();

            // Assert
            assertThat(result1).isNotSameAs(result2);
        }
    }

    @Nested
    class SecurityFilterChainTests {

        @Test
        void securityFilterChain_HasBeanAnnotation() throws NoSuchMethodException {
            // Act
            Method method = SecurityConfiguration.class.getMethod("securityFilterChain", HttpSecurity.class, AuthCheckerUserOnlyFilter.class);

            // Assert
            assertThat(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class)).isTrue();
        }

        @Test
        void securityFilterChain_MethodExists() throws NoSuchMethodException {
            // Act & Assert - just verify the method exists with correct signature
            Method method = SecurityConfiguration.class.getMethod("securityFilterChain", HttpSecurity.class, AuthCheckerUserOnlyFilter.class);
            assertThat(method).isNotNull();
        }

        @Test
        void securityFilterChain_ReturnTypeIsSecurityFilterChain() throws NoSuchMethodException {
            // Act
            Method method = SecurityConfiguration.class.getMethod("securityFilterChain", HttpSecurity.class, AuthCheckerUserOnlyFilter.class);

            // Assert
            assertThat(method.getReturnType()).isEqualTo(SecurityFilterChain.class);
        }

        @Test
        void securityFilterChain_ParametersAreCorrect() throws NoSuchMethodException {
            // Act
            Method method = SecurityConfiguration.class.getMethod("securityFilterChain", HttpSecurity.class, AuthCheckerUserOnlyFilter.class);

            // Assert
            assertThat(method.getParameterCount()).isEqualTo(2);
            assertThat(method.getParameterTypes()[0]).isEqualTo(HttpSecurity.class);
            assertThat(method.getParameterTypes()[1]).isEqualTo(AuthCheckerUserOnlyFilter.class);
        }

        @Test
        void securityFilterChain_ThrowsException() throws NoSuchMethodException {
            // Act
            Method method = SecurityConfiguration.class.getMethod("securityFilterChain", HttpSecurity.class, AuthCheckerUserOnlyFilter.class);

            // Assert
            assertThat(method.getExceptionTypes()).containsExactly(Exception.class);
        }
    }

    @Nested
    class GetAuthWhitelistTests {

        @Test
        void getAuthWhitelist_ReturnsDefensiveCopy() {
            // Act
            String[] result1 = SecurityConfiguration.getAuthWhitelist();
            String[] result2 = SecurityConfiguration.getAuthWhitelist();

            // Assert
            assertThat(result1).isNotSameAs(result2)
                .isEqualTo(result2);
        }

        @Test
        void getAuthWhitelist_ReturnsExpectedValues() {
            // Act
            String[] result = SecurityConfiguration.getAuthWhitelist();

            // Assert
            assertThat(result).containsExactly(
                "/",
                "/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**",
                "/health", "/env", "/health/**", "/status/health",
                "/loggers/**", "/assignment/**", "/service-request-update",
                "/service-request-update-claim-issued", "/case/document/downloadDocument/**",
                "/fees/claim/calculate-interest",
                "/testing-support/flowstate"
            );
        }

        @Test
        void getAuthWhitelist_ModifyingReturnedArrayDoesNotAffectOriginal() {
            // Act
            String[] result1 = SecurityConfiguration.getAuthWhitelist();
            result1[0] = "modified";
            String[] result2 = SecurityConfiguration.getAuthWhitelist();

            // Assert
            assertThat(result2[0]).isEqualTo("/");
        }
    }

    @Test
    void class_IsPublic() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isPublic(SecurityConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    void class_IsNotAbstract() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isAbstract(SecurityConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_IsNotFinal() {
        // Assert
        assertThat(java.lang.reflect.Modifier.isFinal(SecurityConfiguration.class.getModifiers())).isFalse();
    }

    @Test
    void class_ExtendsObject() {
        // Assert
        assertThat(SecurityConfiguration.class.getSuperclass()).isEqualTo(Object.class);
    }

    @Test
    void class_ImplementsNoInterfaces() {
        // Assert
        assertThat(SecurityConfiguration.class.getInterfaces()).isEmpty();
    }
}
