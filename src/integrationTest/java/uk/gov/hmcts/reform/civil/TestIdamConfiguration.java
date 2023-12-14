package uk.gov.hmcts.reform.civil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.context.ContextCleanupListener;

import static com.google.common.collect.ImmutableMap.of;

@Configuration
public class TestIdamConfiguration extends ContextCleanupListener {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(clientRegistration());
    }

    private ClientRegistration clientRegistration() {

        return ClientRegistration.withRegistrationId("oidc")
            .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope("read:user")
            .authorizationUri("http://idam/o/authorize")
            .tokenUri("http://idam/o/token")
            .userInfoUri("http://idam/o/userinfo")
            .jwkSetUri("http://idam/o/oauth/jwk")
            .providerConfigurationMetadata(of("end_session_endpoint", "https://idam/logout"))
            .userNameAttributeName("id")
            .clientName("Client Name")
            .clientId("client-id")
            .clientSecret("client-secret")
            .build();
    }
}
