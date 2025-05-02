package uk.gov.hmcts.reform.civil.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CustomAuthCheckerUserOnlyFilterTest {

    @Mock
    private RequestAuthorizer<User> userRequestAuthorizer;

    @Mock
    private HttpServletRequest request;

    private CustomAuthCheckerUserOnlyFilter<User> filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new CustomAuthCheckerUserOnlyFilter<>(userRequestAuthorizer);
    }

    @Test
    void shouldSkipAuthenticationForHealthEndpoints() {
        when(request.getRequestURI()).thenReturn("/health");

        Object principal = filter.getPreAuthenticatedPrincipal(request);
        Object credentials = filter.getPreAuthenticatedCredentials(request);

        assertThat(principal).isNull();
        assertThat(credentials).isNull();
        verifyNoInteractions(userRequestAuthorizer);
    }

    @Test
    void shouldProcessAuthenticationForNonHealthEndpoints() {
        when(request.getRequestURI()).thenReturn("/cases/callbacks/some-endpoint");
        when(request.getHeader(anyString())).thenReturn("some-header-value");
        User mockUser = Mockito.mock(User.class);
        when(userRequestAuthorizer.authorise(request)).thenReturn(mockUser);

        Object principal = filter.getPreAuthenticatedPrincipal(request);
        Object credentials = filter.getPreAuthenticatedCredentials(request);

        assertThat(principal).isNotNull();
        assertThat(credentials).isNotNull();
    }
}


