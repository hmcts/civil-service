package uk.gov.hmcts.reform.civil.model.docmosis.common;

import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.IncomeTypeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReasonMoneyTemplateDataTest {

    @Test
    void shouldNotThrowException() {
        RecurringIncomeLRspec item = new RecurringIncomeLRspec()
            .setType(IncomeTypeLRspec.UNIVERSAL_CREDIT)
            .setFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH);

        assertDoesNotThrow(() -> ReasonMoneyTemplateData.toReasonMoneyTemplateData(item));
    }
}
