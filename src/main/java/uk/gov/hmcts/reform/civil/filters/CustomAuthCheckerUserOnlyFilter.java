package uk.gov.hmcts.reform.civil.filters;

import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;
import uk.gov.hmcts.reform.civil.config.SecurityConfiguration;

import javax.servlet.http.HttpServletRequest;

public class CustomAuthCheckerUserOnlyFilter<T extends User> extends AuthCheckerUserOnlyFilter<T> {

    public CustomAuthCheckerUserOnlyFilter(RequestAuthorizer<T> userRequestAuthorizer) {
        super(userRequestAuthorizer);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // Skip processing for whitelisted endpoints
        if (isWhitelisted(request.getRequestURI())) {
            return null;  // Skip authentication logic for whitelisted endpoints
        }
        return super.getPreAuthenticatedPrincipal(request);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // Skip processing for whitelisted endpoints
        if (isWhitelisted(request.getRequestURI())) {
            return null;  // Skip credentials processing for whitelisted endpoints
        }
        return super.getPreAuthenticatedCredentials(request);
    }

    boolean isWhitelisted(String requestURI) {
        for (String endpoint : SecurityConfiguration.getAuthWhitelist()) {
            // Special case for the root path "/"
            if (endpoint.equals("/")) {
                if (requestURI.equals("/")) {
                    return true;  // Exact match for the root path "/"
                }
                continue;
            }
            // Strip out wildcards ** or * and match the URI using startsWith()
            String strippedEndpoint = endpoint.replace("**", "").replace("*", "");
            // Use startsWith to check if the requestURI starts with the stripped endpoint
            if (requestURI.startsWith(strippedEndpoint)) {
                return true;
            }
        }
        return false;  // Apply filter for non-whitelisted paths
    }
}
