package uk.gov.hmcts.reform.civil.security;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtGrantedAuthoritiesConverterTest {

    @InjectMocks
    private JwtGrantedAuthoritiesConverter converter;

    @Mock
    private UserService userService;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = mock(Jwt.class);
    }

    @Nested
    @DisplayName("Gets empty authorities")
    class EmptyAuthorities {

        @BeforeEach
        void setup() {
            when(jwt.containsClaim(anyString())).thenReturn(false);
        }

        @Test
        void shouldReturnEmptyAuthorities_whenNoRolesOnJwt() {
            assertEmptyAuthorities();
        }

        @Test
        void shouldReturnEmptyAuthorities_whenClaimNotAvailable() {
            when(jwt.containsClaim(anyString())).thenReturn(false);
            assertEmptyAuthorities();
        }

        @Test
        void shouldReturnEmptyAuthorities_whenClaimValueDoesNotMatch() {
            when(jwt.containsClaim(anyString())).thenReturn(true);
            when(jwt.getClaim(anyString())).thenReturn("Test");
            assertEmptyAuthorities();
        }

        @Test
        void shouldReturnEmptyAuthorities_whenIdamReturnsNoUsers() {
            setupMockJwtWithValidToken();
            UserInfo userInfo = mock(UserInfo.class);
            when(userInfo.getRoles()).thenReturn(Collections.emptyList());
            when(userService.getUserInfo(anyString())).thenReturn(userInfo);
            assertEmptyAuthorities();
        }

        private void assertEmptyAuthorities() {
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(0, authorities.size());
        }
    }

    @Nested
    class ValidAuthorities {

        @BeforeEach
        void setup() {
            setupMockJwtWithValidToken();
            UserInfo userInfo = mock(UserInfo.class);
            when(userInfo.getRoles()).thenReturn(ImmutableList.of("caseworker-solicitor"));
            when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        }

        @Test
        void shouldReturnAuthorities_whenIdamReturnsUserRoles() {
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(1, authorities.size());
        }
    }

    private void setupMockJwtWithValidToken() {
        when(jwt.containsClaim(anyString())).thenReturn(true);
        when(jwt.getClaim(anyString())).thenReturn("access_token");
        when(jwt.getTokenValue()).thenReturn("access_token");
    }
}
