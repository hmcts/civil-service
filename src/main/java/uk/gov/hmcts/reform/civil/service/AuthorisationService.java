package uk.gov.hmcts.reform.civil.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorisationService {

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    @Value("${civil.authorised-services}")
    private List<String> s2sAuthorisedServices;

    @Value("${civil.payment-callback-authorised-services}")
    private List<String> paymentCallbackAuthorisedServices;

    private final IdamClient idamClient;

    @Getter
    private UserInfo userInfo;

    public Boolean authoriseService(String serviceAuthHeader) {
        return authoriseService(serviceAuthHeader, s2sAuthorisedServices);
    }

    private Boolean authoriseService(String serviceAuthHeader, List<String> authorisedServices) {
        String callingService;
        try {
            String bearerJwt = serviceAuthHeader.startsWith("Bearer ") ? serviceAuthHeader : "Bearer " + serviceAuthHeader;
            callingService = serviceAuthorisationApi.getServiceName(bearerJwt);
            log.info("Calling Service... {}", callingService);
            return (callingService != null && authorisedServices.contains(callingService));
        } catch (Exception ex) {
            //do nothing
            log.error("S2S token is not authorised", ex);
        }
        return false;
    }

    public Boolean authoriseUser(String authorisation) {
        try {
            userInfo = idamClient.getUserInfo(authorisation);
            return (null != userInfo);
        } catch (Exception ex) {
            //do nothing
            log.error("User token is invalid");
        }
        return false;
    }

    public boolean isServiceAndUserAuthorized(String authorisation, String s2sToken) {
        return Boolean.TRUE.equals(authoriseUser(authorisation))
            && Boolean.TRUE.equals(authoriseService(s2sToken));
    }

    public boolean isServiceAuthorized(String s2sToken) {
        return Boolean.TRUE.equals(authoriseService(s2sToken));
    }

    public boolean isPaymentCallbackServiceAuthorized(String s2sToken) {
        return authoriseService(s2sToken, paymentCallbackAuthorisedServices);
    }
}
