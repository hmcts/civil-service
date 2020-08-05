package uk.gov.hmcts.reform.unspec.service.bankholidays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicHolidaysCollectionTest {

    private static final LocalDate BANK_HOLIDAY_1 = LocalDate.of(2020, 12, 24);
    private static final LocalDate BANK_HOLIDAY_2 = LocalDate.of(2020, 12, 25);

    @Mock
    private BankHolidaysApi bankHolidaysApi;

    @Test
    void shouldReturnAllBankHolidays_whenSuccessfulResponse() {
        when(bankHolidaysApi.retrieveAll()).thenReturn(createExpectedResponse());
        PublicHolidaysCollection publicHolidaysCollection = new PublicHolidaysCollection(bankHolidaysApi);

        Set<LocalDate> response = publicHolidaysCollection.getPublicHolidays();

        assertAll(
            "Bank holidays",
            () -> assertThat(response).contains(BANK_HOLIDAY_1),
            () -> assertThat(response).contains(BANK_HOLIDAY_2)
        );
    }

    @Test
    void shouldMakeSingleExternalApiCall_whenCacheIsNotPopulated() {
        when(bankHolidaysApi.retrieveAll()).thenReturn(createExpectedResponse());
        PublicHolidaysCollection publicHolidaysCollection = new PublicHolidaysCollection(bankHolidaysApi);

        Set<LocalDate> resultFromApi = publicHolidaysCollection.getPublicHolidays();
        Set<LocalDate> resultFromCache = publicHolidaysCollection.getPublicHolidays();
        Set<LocalDate> resultFromCacheAgain = publicHolidaysCollection.getPublicHolidays();

        verify(bankHolidaysApi).retrieveAll();
        assertThat(resultFromApi).isSameAs(resultFromCache).isSameAs(resultFromCacheAgain);
    }

    private static BankHolidays createExpectedResponse() {
        BankHolidays expResponse = new BankHolidays();
        expResponse.englandAndWales = new BankHolidays.Division();
        BankHolidays.Division.EventDate item1 = createItem(BANK_HOLIDAY_1);
        BankHolidays.Division.EventDate item2 = createItem(BANK_HOLIDAY_2);
        expResponse.englandAndWales.events = new ArrayList<>(Arrays.asList(item1, item2));

        return expResponse;
    }

    private static BankHolidays.Division.EventDate createItem(LocalDate date) {
        BankHolidays.Division.EventDate item = new BankHolidays.Division.EventDate();
        item.date = date;

        return item;
    }
}
