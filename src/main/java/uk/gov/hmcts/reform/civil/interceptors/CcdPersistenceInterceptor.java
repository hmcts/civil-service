package uk.gov.hmcts.reform.civil.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

import java.io.IOException;

/**
 * Lets only CCD data-store call the decentralised runtime's ccd-persistence endpoints.
 *
 * <p>Those endpoints read and write case data directly, behind CCD's access control, so a valid user token is not
 * enough: the caller must also be data-store.
 */
@Slf4j
@Component
public class CcdPersistenceInterceptor implements HandlerInterceptor {

    public static final String PATH_PATTERN = "/ccd-persistence/**";

    static final String CCD_DATA = "ccd_data";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    // Lazy: the S2S Feign client needs the MVC configuration this interceptor is registered in.
    public CcdPersistenceInterceptor(@Lazy ServiceAuthorisationApi serviceAuthorisationApi) {
        this.serviceAuthorisationApi = serviceAuthorisationApi;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {
        if (CCD_DATA.equals(callingService(request.getHeader(SERVICE_AUTHORIZATION)))) {
            return true;
        }
        log.warn("Rejected a ccd-persistence request that was not made by {}", CCD_DATA);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only " + CCD_DATA + " may call ccd-persistence");
        return false;
    }

    private String callingService(String serviceAuthorization) {
        if (serviceAuthorization == null || serviceAuthorization.isBlank()) {
            return null;
        }
        String token = serviceAuthorization.startsWith(BEARER_PREFIX)
            ? serviceAuthorization
            : BEARER_PREFIX + serviceAuthorization;
        try {
            return serviceAuthorisationApi.getServiceName(token);
        } catch (RuntimeException e) {
            log.warn("Invalid ServiceAuthorization on a ccd-persistence request", e);
            return null;
        }
    }
}
