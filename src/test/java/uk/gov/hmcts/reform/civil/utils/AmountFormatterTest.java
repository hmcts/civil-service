package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AmountFormatterTest {

    @Test
    public void testFormatAmount() {
        String amount = AmountFormatter.formatAmount(BigDecimal.valueOf(100));
        assertEquals("100", amount);
    }

    @Test
    public void testFormatAmountWithOneDecimals() {
        String amount = AmountFormatter.formatAmount(BigDecimal.valueOf(100.5));
        assertEquals("100.50", amount);
    }
}
