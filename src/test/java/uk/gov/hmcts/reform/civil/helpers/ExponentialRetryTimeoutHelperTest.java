package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.helpers.ExponentialRetryTimeoutHelper.calculateExponentialRetryTimeout;

class ExponentialRetryTimeoutHelperTest {

    public static final int START_VALUE = 500;

    @Test
    void shouldStartWithInitialValueForRetryTime_whenAllRetriesLeft() {
        assertEquals(START_VALUE, getActual(3, 3));
    }

    @Test
    void shouldRiseExponentially_whenRemainingRetriesLessThanTotalRetries() {
        assertEquals(2000, getActual(3, 1));
    }

    @Test
    void shouldReturn0_whenRemainingRetriesIsLessThan0() {
        assertEquals(0, getActual(3, -1));
    }

    @Test
    void shouldReturn0_whenRemainingRetriesIsMoreThanTotal() {
        assertEquals(0, getActual(3, 4));
    }

    @Test
    void shouldReturn0_whenTotalAndRemainingRetriesAre0() {
        assertEquals(0, getActual(0, 0));
    }

    private long getActual(int totalRetries, int remainingRetries) {
        return calculateExponentialRetryTimeout(START_VALUE, totalRetries, remainingRetries);
    }
}
