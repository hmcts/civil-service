package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class DebtTemplateData {

    private final String debtOwedTo;
    private final BigDecimal poundsOwed;
    private final BigDecimal paidPerMonth;
}
