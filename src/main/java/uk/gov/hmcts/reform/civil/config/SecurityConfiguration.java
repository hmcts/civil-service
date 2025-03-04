package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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
        // -- swagger ui
        "/v2/api-docs",
        "/swagger-resources/**",
        "/swagger-ui.html",
        "/webjars/**",
        // other public endpoints of API
        "/health",
        "/env",
        "/health/liveness",
        "/health/readiness",
        "/status/health",
        "/",
        "/loggers/**",
        "/assignment/**",
        "/service-request-update",
        "/service-request-update-claim-issued",
        "/case/document/downloadDocument/**",
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

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(AUTH_WHITELIST);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthCheckerUserOnlyFilter<User> authCheckerUserOnlyFilter =
            new AuthCheckerUserOnlyFilter<>(userRequestAuthorizer);

        authCheckerUserOnlyFilter.setAuthenticationManager(authenticationManager);
        // @formatter:off
        http
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .addFilter(authCheckerUserOnlyFilter)
            .logout().disable()
            .authorizeRequests()
            .antMatchers(AUTH_WHITELIST).permitAll()
            .antMatchers("/cases/callbacks/**", "/case/document/generateAnyDoc", "/dashboard/**")
            .hasAnyAuthority(AUTHORITIES)
            .anyRequest()
            .authenticated();
    }
}
