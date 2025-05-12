package uk.gov.hmcts.reform.civil.filters;

import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

import javax.servlet.http.HttpServletRequest;

public class CustomAuthCheckerUserOnlyFilter<T extends User> extends AuthCheckerUserOnlyFilter<T> {

    // Health check endpoints to be skipped
    private static final String[] HEALTH_ENDPOINTS = {"/health", "/env", "/status/health"};

    public CustomAuthCheckerUserOnlyFilter(RequestAuthorizer<T> userRequestAuthorizer) {
        super(userRequestAuthorizer);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // Skip processing for health endpoints
        if (isHealthEndpoint(request.getRequestURI())) {
            return null;  // Skip authentication logic for health endpoints
        }
        return super.getPreAuthenticatedPrincipal(request);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // Skip processing for health endpoints
        if (isHealthEndpoint(request.getRequestURI())) {
            return null;  // Skip credentials processing for health endpoints
        }
        return super.getPreAuthenticatedCredentials(request);
    }

    private boolean isHealthEndpoint(String requestURI) {
        for (String endpoint : HEALTH_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                return true;  // Skip the filter for any health check endpoints
            }
        }
        return false;  // Apply filter for other paths
    }
}
