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
        if (isWhitelisted(request.getRequestURI())) {
            return null;
        }
        return super.getPreAuthenticatedPrincipal(request);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (isWhitelisted(request.getRequestURI())) {
            return null;
        }
        return super.getPreAuthenticatedCredentials(request);
    }

    boolean isWhitelisted(String requestURI) {
        for (String endpoint : SecurityConfiguration.getAuthWhitelist()) {
            if (endpoint.equals("/")) {
                if (requestURI.equals("/")) {
                    return true;
                }
                continue;
            }
            String strippedEndpoint = endpoint.replace("**", "").replace("*", "");
            if (requestURI.startsWith(strippedEndpoint)) {
                return true;
            }
        }
        return false;  // Apply filter for non-whitelisted paths
    }
}
