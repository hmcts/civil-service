package uk.gov.hmcts.reform.civil.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;

@Component
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String BEARER = "Bearer ";
    public static final String TOKEN_NAME = "tokenName";

    private final UserService userService;

    @Autowired
    public JwtGrantedAuthoritiesConverter(UserService userService) {
        this.userService = userService;
    }

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String tokenNameClaim = jwt.getClaimAsString(TOKEN_NAME);

        if (ACCESS_TOKEN.equals(tokenNameClaim)) {
            UserInfo userInfo = userService.getUserInfo(BEARER + jwt.getTokenValue());
            return extractAuthorityFromClaims(userInfo.getRoles());
        }

        return Collections.emptyList();
    }

    private List<GrantedAuthority> extractAuthorityFromClaims(List<String> roles) {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
