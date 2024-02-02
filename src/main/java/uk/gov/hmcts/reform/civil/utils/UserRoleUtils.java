package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@Slf4j
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
        log.info("checking if matches " + role.getFormattedName());
        log.info("result " + roles.stream().anyMatch(role.getFormattedName()::contains));
        return roles.stream().anyMatch(role.getFormattedName()::contains);
    }

}
