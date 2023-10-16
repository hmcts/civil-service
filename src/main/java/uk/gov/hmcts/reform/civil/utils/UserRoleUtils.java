package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

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

    private static boolean hasRole(List<String> roles, CaseRole role) {
        return roles.stream().anyMatch(role.getFormattedName()::contains);
    }
}
