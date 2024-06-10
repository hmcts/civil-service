package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.ContextCleanupListener;

import static com.google.common.collect.ImmutableMap.of;

@Configuration
public class ContractTestsConfiguration extends ContextCleanupListener {

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
