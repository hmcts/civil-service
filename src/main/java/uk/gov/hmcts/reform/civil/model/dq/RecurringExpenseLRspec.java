package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.ExpenseTypeLRspec;
import uk.gov.hmcts.reform.civil.enums.dq.WeekMonthPeriodLRspec;

import java.math.BigDecimal;

@Data
public class RecurringExpenseLRspec {

    private final ExpenseTypeLRspec type;
    private final String typeOtherDetails;
    private final BigDecimal amount;
    private final WeekMonthPeriodLRspec frequency;
}
