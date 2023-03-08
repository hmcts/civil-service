package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * just a pair of values String - amount in pounds to use in templates.
 */
@Builder
@Data
public class ReasonMoneyTemplateData {

    private String type;
    private BigDecimal amountPounds;
}
