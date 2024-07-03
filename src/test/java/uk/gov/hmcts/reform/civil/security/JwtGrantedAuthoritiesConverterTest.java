package uk.gov.hmcts.reform.civil.security;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.civil.security.JwtGrantedAuthoritiesConverter.TOKEN_NAME;

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
            when(jwt.getClaimAsString(TOKEN_NAME)).thenReturn(null);
        }

        @Test
        void shouldReturnEmptyAuthorities_whenNoRolesOnJwt() {
            assertEmptyAuthorities();
        }

        @Test
        void shouldReturnEmptyAuthorities_whenClaimNotAvailable() {
            assertEmptyAuthorities();
        }

        @Test
        void shouldReturnEmptyAuthorities_whenClaimValueDoesNotMatch() {
            when(jwt.getClaimAsString(TOKEN_NAME)).thenReturn("Test");
            assertEmptyAuthorities();
        }

        @Test
        void shouldReturnEmptyAuthorities_whenIdamReturnsNoUsers() {
            setupMockJwtWithValidToken();
            setupUserInfo(Collections.emptyList());
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
            setupUserInfo(List.of("caseworker-solicitor"));
        }

        @Test
        void shouldReturnAuthorities_whenIdamReturnsUserRoles() {
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(1, authorities.size());
        }
    }

    private void setupMockJwtWithValidToken() {
        when(jwt.getClaimAsString(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn("access_token");
    }

    private void setupUserInfo(List<String> roles) {
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getRoles()).thenReturn(roles);
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
    }
}
