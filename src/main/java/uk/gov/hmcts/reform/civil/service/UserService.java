package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamIdamException;
import uk.gov.hmcts.reform.civil.utils.MaskHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@Slf4j
public class UserService {

    private static final int IDAM_USER_DETAILS_MAX_ATTEMPTS = 2;
    private static final long IDAM_USER_DETAILS_RETRY_DELAY_MILLIS = 250L;

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
        for (int attempt = 1; attempt <= IDAM_USER_DETAILS_MAX_ATTEMPTS; attempt++) {
            try {
                return idamClient.getUserDetails(authorisation);
            } catch (FeignException e) {
                if (isServerError(e)) {
                    if (attempt < IDAM_USER_DETAILS_MAX_ATTEMPTS) {
                        sleepBeforeRetry();
                        continue;
                    }
                    throw new UpstreamIdamException("IDAM temporarily unavailable", e);
                }
                throw e;
            } catch (Exception e) {
                String maskedError = MaskHelper.maskEmailsInErrorMessages(e.getMessage());
                throw new IllegalArgumentException(maskedError, e);
            }
        }
        throw new UpstreamIdamException("IDAM temporarily unavailable");
    }

    private boolean isServerError(FeignException exception) {
        return exception.status() >= 500 && exception.status() < 600;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(IDAM_USER_DETAILS_RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpstreamIdamException("Interrupted while retrying IDAM user details request", e);
        }
    }
}
