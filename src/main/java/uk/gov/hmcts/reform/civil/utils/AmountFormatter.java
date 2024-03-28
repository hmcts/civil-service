package uk.gov.hmcts.reform.civil.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class AmountFormatter {

    private AmountFormatter() {
        //NO-OP
    }

    public static String formatAmount(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(amount.scale() > 0 ? 2 : 0);
        return df.format(amount);
    }

}
