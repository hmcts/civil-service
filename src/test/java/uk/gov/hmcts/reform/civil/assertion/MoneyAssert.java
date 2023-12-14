package uk.gov.hmcts.reform.civil.assertion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.poundsToPennies;

public class MoneyAssert extends CustomAssert<MoneyAssert, BigDecimal> {

    MoneyAssert(BigDecimal amountInPounds) {
        super("Amount", amountInPounds, MoneyAssert.class);
    }

    public MoneyAssert isEqualTo(BigDecimal amountInPennies) {
        isNotNull();

        BigInteger actualValue = poundsToPennies(actual);
        if (!Objects.equals(actualValue, amountInPennies.toBigInteger())) {
            failExpectedEqual("value", amountInPennies, actualValue);
        }

        return this;
    }

}
