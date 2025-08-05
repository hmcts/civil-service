package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.utils.MaskHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
/**
 * Service for user operations via IDAM client.
 *
 * <p><strong>Technical Debt:</strong> This service uses deprecated IDAM methods that require
 * full project analysis before migration to new API methods.</p>
 *
 * @author gergelykiss
 * @version 1.0
 */
@SuppressWarnings("deprecation") // TO-REVIEW: Migrate deprecated IDAM methods after project analysis
@Service
@Slf4j
public class UserService {

    private final IdamClient idamClient;
    private final boolean hmcSupportEnabled;

    @Autowired
    public UserService(IdamClient idamClient,
                       @Value("${hmc.support.enabled:false}") boolean hmcSupportEnabled) {
        this.idamClient = idamClient;
        this.hmcSupportEnabled = hmcSupportEnabled;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        return idamClient.getUserInfo(bearerToken);
    }

    @Cacheable(value = "accessTokenCache")
    public String getAccessToken(String username, String password) {
        var token = idamClient.getAccessToken(username, password);

        if (hmcSupportEnabled) {
            log.info("system user token: {}", token);
        }

        return token;
    }

    public UserDetails getUserDetails(String authorisation) {
        try {
            return idamClient.getUserDetails(authorisation);
        } catch (Exception e) {
            String maskedError = MaskHelper.maskEmailsInErrorMessages(e.getMessage());
            throw new IllegalArgumentException(maskedError);
        }
    }
}
