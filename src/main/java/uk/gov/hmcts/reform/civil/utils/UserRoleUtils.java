package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.CaseRole;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
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

    public static boolean isLIPClaimant(List<String> roles) {
        return hasRole(roles, CLAIMANT);
    }

    public static boolean isLIPDefendant(List<String> roles) {
        return hasRole(roles, DEFENDANT);
    }

    public static Optional<CaseRole> getCaseRole(List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return Optional.of(APPLICANTSOLICITORONE);
        }
        if (isRespondentSolicitorOne(roles)) {
            return Optional.of(RESPONDENTSOLICITORONE);
        }
        if (isRespondentSolicitorTwo(roles)) {
            return Optional.of(RESPONDENTSOLICITORTWO);
        }
        if (isLIPClaimant(roles)) {
            return Optional.of(CLAIMANT);
        }
        if (isLIPDefendant(roles)) {
            return Optional.of(DEFENDANT);
        }
        return Optional.empty();
    }

    private static boolean hasRole(List<String> roles, CaseRole role) {
        return roles.stream().anyMatch(role.getFormattedName()::contains);
    }

}
