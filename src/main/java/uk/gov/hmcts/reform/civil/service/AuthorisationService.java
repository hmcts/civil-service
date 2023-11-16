package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorisationService {

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    @Value("${civil.authorised-services}")
    private List<String> s2sAuthorisedServices;

    private final IdamClient idamClient;

    private UserInfo userInfo;

    public Boolean authoriseService(String serviceAuthHeader) {
        String callingService;
        try {
            String bearerJwt = serviceAuthHeader.startsWith("Bearer ") ? serviceAuthHeader : "Bearer " + serviceAuthHeader;
            callingService = serviceAuthorisationApi.getServiceName(bearerJwt);
            log.info("Calling Service... {}", callingService);
            return (callingService != null && s2sAuthorisedServices.contains(callingService));
        } catch (Exception ex) {
            //do nothing
            log.error("S2S token is not authorised" + ex);
            return false;
        }
    }

    public Boolean authoriseUser(String authorisation) {
        try {
            userInfo = idamClient.getUserInfo(authorisation);
            return (null != userInfo);
        } catch (Exception ex) {
            //do nothing
            log.error("User token is invalid");
            return false;
        }
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public boolean isServiceAndUserAuthorized(String authorisation, String s2sToken) {
        return Boolean.TRUE.equals(authoriseUser(authorisation))
            && Boolean.TRUE.equals(authoriseService(s2sToken));
    }

    public boolean isServiceAuthorized(String s2sToken) {
        return Boolean.TRUE.equals(authoriseService(s2sToken));
    }
}
