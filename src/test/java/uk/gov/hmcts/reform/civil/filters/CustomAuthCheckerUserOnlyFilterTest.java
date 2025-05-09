package uk.gov.hmcts.reform.civil.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomAuthCheckerUserOnlyFilterTest {

    @Mock
    private RequestAuthorizer<User> userRequestAuthorizer;

    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private CustomAuthCheckerUserOnlyFilter<User> filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes mocks
    }

    @Test
    void testIsWhitelistedWithExactMatch() {
        // Test if the path is whitelisted with an exact match
        when(mockRequest.getRequestURI()).thenReturn("/health");

        // Check if /health is whitelisted
        assertTrue(filter.isWhitelisted(mockRequest.getRequestURI()), "/health should be in the whitelist");
    }

    @Test
    void testIsWhitelistedWithWildcardMatch() {
        // Test if the path is whitelisted with a wildcard match
        when(mockRequest.getRequestURI()).thenReturn("/health/check");

        // Check if /health/** is whitelisted
        assertTrue(filter.isWhitelisted(mockRequest.getRequestURI()), "/health/check should be in the whitelist");
    }

    @Test
    void testIsWhitelistedWithWildcardMatchAndSinglePath() {
        // Test if the path is whitelisted with a wildcard pattern for single-level paths
        when(mockRequest.getRequestURI()).thenReturn("/swagger-resources/docs");

        // Check if /swagger-resources/** is whitelisted
        assertTrue(filter.isWhitelisted(mockRequest.getRequestURI()), "/swagger-resources/docs should be in the whitelist");
    }

    @Test
    void testIsNotWhitelisted() {
        // Test if the path is not whitelisted
        when(mockRequest.getRequestURI()).thenReturn("/dashboard");

        // Check if /dashboard is not whitelisted
        assertFalse(filter.isWhitelisted(mockRequest.getRequestURI()), "/dashboard should not be in the whitelist");
    }

    @Test
    void testFilterSkipForWhitelistedEndpoint() {
        // Test if the filter skips authentication for a whitelisted path
        when(mockRequest.getRequestURI()).thenReturn("/health/check");

        // Simulate skipping the filter
        assertNull(filter.getPreAuthenticatedPrincipal(mockRequest), "Should skip authentication for /health/check");
    }

    @Test
    void testFilterNotSkippedForNonWhitelistedEndpoint() {
        // Test if the filter does not skip authentication for a non-whitelisted path
        when(mockRequest.getRequestURI()).thenReturn("/user/profile");

        // Simulate principal being returned by authorization
        when(userRequestAuthorizer.authorise(mockRequest)).thenReturn(mock(User.class));

        // Should not skip authentication for /user/profile
        assertNotNull(filter.getPreAuthenticatedPrincipal(mockRequest), "Should not skip authentication for non-whitelisted endpoints");
    }
}


