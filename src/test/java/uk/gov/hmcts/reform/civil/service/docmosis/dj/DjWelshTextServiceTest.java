package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DjWelshTextServiceTest {

    private final DjWelshTextService service = new DjWelshTextService();

    @Test
    void shouldReturnEnglishRepaymentStrings() {
        assertThat(service.getRepaymentString(RepaymentFrequencyDJ.ONCE_ONE_WEEK, false)).isEqualTo("each week");
        assertThat(service.getRepaymentFrequency(RepaymentFrequencyDJ.ONCE_TWO_WEEKS, false)).isEqualTo("every 2 weeks");
    }

    @Test
    void shouldReturnWelshRepaymentStrings() {
        assertThat(service.getRepaymentString(RepaymentFrequencyDJ.ONCE_ONE_MONTH, true)).isEqualTo("pob mis");
        assertThat(service.getRepaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH, true)).isEqualTo("y mis");
    }

    @Test
    void shouldFormatDateInWelsh() {
        LocalDate date = LocalDate.of(2025, 2, 10);
        assertThat(service.getDateInWelsh(date)).isEqualTo("10 Chwefror 2025");
    }

    @Test
    void shouldConvertInstallmentAmount() {
        assertThat(service.getInstallmentAmount("200")).isEqualTo("2.00");
    }
}
