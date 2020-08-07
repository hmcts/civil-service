package uk.gov.hmcts.reform.unspec.security;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.unspec.service.UserService;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {JwtGrantedAuthoritiesConverter.class})
class JwtGrantedAuthoritiesConverterTest {

    @MockBean
    private UserService userService;

    @Autowired
    private JwtGrantedAuthoritiesConverter converter;

    @Nested
    @DisplayName("Gets empty authorities")
    class EmptyAuthorities {

        @Test
        void shouldReturnEmptyAuthorities_whenNoRolesOnJwt() {
            Jwt jwt = Mockito.mock(Jwt.class);
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(0, authorities.size());
        }

        @Test
        void shouldReturnEmptyAuthorities_whenClaimNotAvailable() {
            Jwt jwt = Mockito.mock(Jwt.class);
            when(jwt.containsClaim(anyString())).thenReturn(false);
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(0, authorities.size());
        }

        @Test
        void shouldReturnEmptyAuthorities_whenClaimValueDoesNotMatch() {
            Jwt jwt = Mockito.mock(Jwt.class);
            when(jwt.containsClaim(anyString())).thenReturn(true);
            when(jwt.getClaim(anyString())).thenReturn("Test");
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(0, authorities.size());
        }

        @Test
        void shouldReturnEmptyAuthorities_whenIdamReturnsNoUsers() {
            Jwt jwt = Mockito.mock(Jwt.class);
            when(jwt.containsClaim(anyString())).thenReturn(true);
            when(jwt.getClaim(anyString())).thenReturn("access_token");
            when(jwt.getTokenValue()).thenReturn("access_token");
            UserInfo userInfo = mock(UserInfo.class);
            doReturn(Collections.emptyList()).when(userInfo).getRoles();
            when(userService.getUserInfo(anyString())).thenReturn(userInfo);
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(0, authorities.size());
        }
    }

    @Nested
    class ValidAuthorities {

        @Test
        void shouldReturnAuthorities_whenIdamReturnsUserRoles() {
            Jwt jwt = Mockito.mock(Jwt.class);
            when(jwt.containsClaim(anyString())).thenReturn(true);
            when(jwt.getClaim(anyString())).thenReturn("access_token");
            when(jwt.getTokenValue()).thenReturn("access_token");
            UserInfo userInfo = mock(UserInfo.class);
            doReturn(ImmutableList.of("caseworker-solicitor")).when(userInfo).getRoles();
            when(userService.getUserInfo(anyString())).thenReturn(userInfo);
            Collection<GrantedAuthority> authorities = converter.convert(jwt);
            assertNotNull(authorities);
            assertEquals(1, authorities.size());
        }
    }
}
