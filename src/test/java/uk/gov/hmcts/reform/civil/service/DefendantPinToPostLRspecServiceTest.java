package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(classes = {
    DefendantPinToPostLRspecService.class,
})
class DefendantPinToPostLRspecServiceTest {

    @Autowired
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @Nested
    class BuildDefendantPinToPost {

        @Test
        void shouldBuildDefendantPinToPost_whenInvoked() {
            DefendantPinToPostLRspec defendantPinToPostLRspec = defendantPinToPostLRspecService.buildDefendantPinToPost();
            assertThat(defendantPinToPostLRspec.getExpiryDate()).isEqualTo(getDate180days());
            assertThat(defendantPinToPostLRspec.getRespondentCaseRole()).isEqualTo(CaseRole.RESPONDENTSOLICITORONESPEC.getFormattedName());
            assertThat(defendantPinToPostLRspec.getAccessCode()).isNotEmpty();
        }
    }

    private LocalDate getDate180days () {
        return LocalDate.now().plusDays(180);
    }

}
