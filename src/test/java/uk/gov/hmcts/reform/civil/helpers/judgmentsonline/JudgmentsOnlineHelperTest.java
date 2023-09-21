package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentStatusType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class JudgmentsOnlineHelperTest {

    @Test
    void test_getRTLStatusBasedOnJudgementStatus() {
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.ISSUED)).isEqualTo("R");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.MODIFIED)).isEqualTo(
            "M");
        assertThat(JudgmentsOnlineHelper.getRTLStatusBasedOnJudgementStatus(JudgmentStatusType.CANCELLED)).isEqualTo(
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
}
