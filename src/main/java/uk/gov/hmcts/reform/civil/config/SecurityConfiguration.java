package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.civil.filters.CustomAuthCheckerUserOnlyFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] AUTHORITIES = {
        "caseworker-civil",
        "caseworker-civil-solicitor",
        "caseworker",
        "caseworker-caa",
        "caseworker-approver",
        "citizen",
        "next-hearing-date-admin"
    };

    private static final String[] AUTH_WHITELIST = {
        "/",
        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**",
        "/health", "/health/**", "/status/health",
        "/loggers/**", "/assignment/**", "/service-request-update",
        "/service-request-update-claim-issued",
        "/fees/claim/calculate-interest",
        "/testing-support/**"
    };

    private static final String PERMISSIONS_POLICY =
        "accelerometer=(), camera=(), geolocation=(), gyroscope=(), "
            + "microphone=(), payment=(), usb=()";

    private final RequestAuthorizer<User> userRequestAuthorizer;
    private final AuthenticationManager authenticationManager;

    public SecurityConfiguration(
        RequestAuthorizer<User> userRequestAuthorizer,
        AuthenticationManager authenticationManager
    ) {
        this.userRequestAuthorizer = userRequestAuthorizer;
        this.authenticationManager = authenticationManager;
    }

    @Bean
    public AuthCheckerUserOnlyFilter<User> authCheckerUserOnlyFilter() {
        CustomAuthCheckerUserOnlyFilter<User> filter = new CustomAuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthCheckerUserOnlyFilter<User> authCheckerUserOnlyFilter) throws Exception {
        http
            .headers(headers -> {
                headers.contentTypeOptions(Customizer.withDefaults());
                headers.frameOptions(frame -> frame.sameOrigin());
                headers.referrerPolicy(rp -> rp.policy(ReferrerPolicy.NO_REFERRER));
                headers.permissionsPolicy(pp -> pp.policy(PERMISSIONS_POLICY));
                headers.httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000));
                headers.cacheControl(Customizer.withDefaults());
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            // Add the filter for all requests, but health endpoints will be excluded by the permitAll() rule
            .addFilterBefore(authCheckerUserOnlyFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AUTH_WHITELIST).permitAll()  // Health and other whitelisted endpoints
                .requestMatchers("/cases/callbacks/**", "/case/document/generateAnyDoc", "/dashboard/**")
                .hasAnyAuthority(AUTHORITIES)
                .anyRequest().authenticated()
            )
            .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    public static String[] getAuthWhitelist() {
        return Arrays.copyOf(AUTH_WHITELIST, AUTH_WHITELIST.length);
    }
}
