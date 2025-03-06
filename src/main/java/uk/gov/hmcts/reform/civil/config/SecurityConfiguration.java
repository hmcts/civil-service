package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

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
        "/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**",
        "/health", "/env", "/health/liveness", "/health/readiness", "/status/health",
        "/loggers/**", "/assignment/**", "/service-request-update",
        "/service-request-update-claim-issued", "/case/document/downloadDocument/**",
        "/testing-support/flowstate"
    };

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
        AuthCheckerUserOnlyFilter<User> filter = new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthCheckerUserOnlyFilter<User> authCheckerUserOnlyFilter) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .addFilter(authCheckerUserOnlyFilter)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .requestMatchers("/cases/callbacks/**", "/case/document/generateAnyDoc", "/dashboard/**")
                .hasAnyAuthority(AUTHORITIES)
                .anyRequest().authenticated()
            )
            .oauth2Client();

        return http.build();
    }
}
