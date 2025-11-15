package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjAuthorisationFieldServiceTest {

    private final DjAuthorisationFieldService service = new DjAuthorisationFieldService();

    @Test
    void shouldReturnTrueWhenRoleContainsJudge() {
        UserDetails user = UserDetails.builder()
            .forename("Judge")
            .roles(List.of("caseworker-civil-judge"))
            .build();

        assertThat(service.isJudge(user)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoJudgeRole() {
        UserDetails user = UserDetails.builder()
            .forename("Caseworker")
            .roles(List.of("caseworker-civil"))
            .build();

        assertThat(service.isJudge(user)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenUserHasNoRoles() {
        assertThat(service.isJudge(null)).isFalse();
    }
}
