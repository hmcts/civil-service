package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

/**
 * Provides bilingual repayment strings and Welsh date formatting so DJ templates can
 * keep their orchestration logic free of hard-coded text.
 */
@Service
public class DjWelshTextService {

    public String getRepaymentString(RepaymentFrequencyDJ repaymentFrequency, boolean isWelsh) {
        if (repaymentFrequency == null) {
            return null;
        }
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> isWelsh ? "pob wythnos" : "each week";
            case ONCE_ONE_MONTH -> isWelsh ? "pob mis" : "each month";
            case ONCE_TWO_WEEKS -> isWelsh ? "pob 2 wythnos" : "every 2 weeks";
            default -> null;
        };
    }

    public String getRepaymentFrequency(RepaymentFrequencyDJ repaymentFrequency, boolean isWelsh) {
        if (repaymentFrequency == null) {
            return null;
        }
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> isWelsh ? "yr wythnos" : "per week";
            case ONCE_ONE_MONTH -> isWelsh ? "y mis" : "per month";
            case ONCE_TWO_WEEKS -> isWelsh ? "pob 2 wythnos" : "every 2 weeks";
            default -> null;
        };
    }

    public String getDateInWelsh(LocalDate dateToConvert) {
        return dateToConvert == null ? null : formatDateInWelsh(dateToConvert, false);
    }

    public String getInstallmentAmount(String amount) {
        if (amount == null) {
            return null;
        }
        var regularRepaymentAmountPennies = new BigDecimal(amount);
        return String.valueOf(MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies));
    }
}
