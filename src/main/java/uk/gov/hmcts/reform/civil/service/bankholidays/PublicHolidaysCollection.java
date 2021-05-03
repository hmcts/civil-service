package uk.gov.hmcts.reform.civil.service.bankholidays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores all public holidays retrieved from Gov uk API: https://www.gov.uk/bank-holidays.json
 */
@Service
public class PublicHolidaysCollection {

    private final BankHolidaysApi bankHolidaysApi;
    private Set<LocalDate> cachedPublicHolidays;

    @Autowired
    public PublicHolidaysCollection(BankHolidaysApi bankHolidaysApi) {
        this.bankHolidaysApi = bankHolidaysApi;
    }

    private Set<LocalDate> retrieveAllPublicHolidays() {
        BankHolidays bankHolidays = bankHolidaysApi.retrieveAll();

        return bankHolidays.englandAndWales.events.stream()
            .map(item -> item.date)
            .collect(Collectors.toSet());
    }

    public Set<LocalDate> getPublicHolidays() {
        if (cachedPublicHolidays == null) {
            cachedPublicHolidays = retrieveAllPublicHolidays();
        }
        return cachedPublicHolidays;
    }
}
