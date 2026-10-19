package uk.gov.hmcts.reform.civil.cftlib;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsConfiguration;
import uk.gov.service.notify.NotificationClient;

/**
 * Points this service's outbound calls at local stand-ins under cftlib.
 */
@Configuration
public class LocalServicesConfiguration {

    /**
     * CDAM calls the local dm-store with a service token only, as it calls dm-store; tests sign in through the local
     * IDAM endpoints before they hold a token.
     */
    @Bean
    public WebSecurityCustomizer localStandInsAreOpen() {
        return web -> web.ignoring().requestMatchers(
            LocalDmStoreController.PATH, LocalDmStoreController.PATH + "/**",
            LocalIdamController.LOGIN_USER, LocalIdamController.USER_INFO
        );
    }

    /**
     * GOV.UK Notify, replaced by wiremock. {@link NotificationsConfiguration} always uses Notify's own API.
     */
    @Bean
    @Primary
    public NotificationClient localNotificationClient(NotificationsProperties properties,
                                                      @Value("${GOV_NOTIFY_BASE_URL}") String baseUrl) {
        return new NotificationClient(properties.getGovNotifyApiKey(), baseUrl);
    }
}
