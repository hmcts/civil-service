package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

class UserRoleUtilsTest {

    private final List<String> roles = Arrays.asList("[APPLICANTSOLICITORONE]", "[RESPONDENTSOLICITORONE]", "[RESPONDENTSOLICITORTWO]", "[CLAIMANT]", "[DEFENDANT]");
    private final List<String> randomRoles = Arrays.asList("random role", "another role");

    @Test
    void shouldReturnTrue_whenUserHasApplicantSolicitorOneRole() {
        assertThat(isApplicantSolicitor(roles)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserHasApplicantSolicitorOneRole() {
        assertThat(isApplicantSolicitor(randomRoles)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUserHasRespondentSolicitorOneRole() {
        assertThat(isRespondentSolicitorOne(roles)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserHasRespondentSolicitorOneRole() {
        assertThat(isRespondentSolicitorOne(randomRoles)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUserHasRespondentSolicitorTwoRole() {
        assertThat(isRespondentSolicitorTwo(roles)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserHasRespondentSolicitorTwoRole() {
        assertThat(isRespondentSolicitorTwo(randomRoles)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUserHasClaimantRole() {
        assertThat(isLIPClaimant(roles)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserHasNotClaimantRole() {
        assertThat(isLIPClaimant(randomRoles)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUserHasDefendantRole() {
        assertThat(isLIPDefendant(roles)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserHasNotDefendantRole() {
        assertThat(isLIPDefendant(randomRoles)).isFalse();
    }
}
