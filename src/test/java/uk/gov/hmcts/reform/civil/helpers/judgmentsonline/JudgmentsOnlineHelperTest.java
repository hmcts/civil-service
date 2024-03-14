package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper.checkIfDateDifferenceIsGreaterThan31Days;

public class JudgmentsOnlineHelperTest {

    @Test
    void test_getRTLStatusBasedOnJudgementStatus() {
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.ISSUED)).isEqualTo("R");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.MODIFIED)).isEqualTo(
            "M");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.CANCELLED)).isEqualTo(
            "C");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.SET_ASIDE)).isEqualTo(
            "C");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.SATISFIED)).isEqualTo(
            "S");
    }

    @Test
    void test_validateIfFutureDate() {
        assertThat(JudgmentsOnlineHelper.validateIfFutureDate(LocalDate.now())).isFalse();
        assertThat(JudgmentsOnlineHelper.validateIfFutureDate(LocalDate.now().minusDays(3))).isFalse();
        assertThat(JudgmentsOnlineHelper.validateIfFutureDate(LocalDate.now().plusDays(3))).isTrue();
    }

    @Test
    void shouldCheckIfDateDifferenceIsGreaterThan31Days() {
        assertThat(checkIfDateDifferenceIsGreaterThan31Days(LocalDate.now(), LocalDate.now().plusDays(31))).isFalse();
        assertThat(checkIfDateDifferenceIsGreaterThan31Days(LocalDate.now(), LocalDate.now().plusDays(32))).isTrue();
    }

}
