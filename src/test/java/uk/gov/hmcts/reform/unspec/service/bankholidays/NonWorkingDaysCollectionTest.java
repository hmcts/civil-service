package uk.gov.hmcts.reform.unspec.service.bankholidays;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonWorkingDaysCollectionTest {

    private NonWorkingDaysCollection collection;

    @Test
    void shouldReturnTrue_whenMatchingNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-valid.dat");
        assertTrue(collection.contains(LocalDate.of(2020, Month.DECEMBER, 2)));
    }

    @Test
    void shouldReturnFalse_whenNoNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-empty-file.dat");
        assertFalse(collection.contains(LocalDate.now()));
    }

    @Test
    void shouldReturnFalse_whenNonMatchingNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-valid.dat");
        assertFalse(collection.contains(LocalDate.of(2020, Month.DECEMBER, 3)));
    }

    @Test
    void shouldReturnFalse_whenIncoherentNonWorkingDays() {
        collection = new NonWorkingDaysCollection("/non-working-days/nwd-invalid.dat");
        assertFalse(collection.contains(LocalDate.now()));
    }
}
