package uk.gov.hmcts.reform.civil.client.idam;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OAuth2Configuration {

    private String clientId;
    private String redirectUri;
    private String clientSecret;
    private String clientScope;

    @Autowired
    public OAuth2Configuration(
            @Value("${idam.client.redirect_uri:}") String redirectUri,
            @Value("${idam.client.id:}") String clientId,
            @Value("${idam.client.secret:}") String clientSecret,
            @Value("${idam.client.scope:openid profile roles}") String clientScope
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.clientSecret = clientSecret;
        this.clientScope = clientScope;
    }
}
