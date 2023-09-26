package uk.gov.hmcts.reform.civil.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.*;

public class UserRoleUtils {
    private UserRoleUtils() {
        // NO-OP
    }

    public static boolean isApplicantSolicitor(List<String> roles) {
        return hasRole(roles, APPLICANTSOLICITORONE);
    }

    public static boolean isRespondentSolicitorOne(List<String> roles) {
        return hasRole(roles, RESPONDENTSOLICITORONE);
    }

    public static boolean isRespondentSolicitorTwo(List<String> roles) {
        return hasRole(roles, RESPONDENTSOLICITORTWO);
    }

    public static boolean isLIPClaimant(List<String> roles) {
        return hasRole(roles, CLAIMANT);
    }

    public static boolean isLIPDefendant(List<String> roles) {
        return hasRole(roles, DEFENDANT);
    }


    private static boolean hasRole(List<String> roles, CaseRole role) {
        return roles.stream().anyMatch(role.getFormattedName()::contains);
    }


}
