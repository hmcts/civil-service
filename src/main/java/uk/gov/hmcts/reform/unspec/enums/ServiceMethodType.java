package uk.gov.hmcts.reform.unspec.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public enum ServiceMethodType {
    POST(2, DateOrDateTime.DATE),
    DOCUMENT_EXCHANGE(2, DateOrDateTime.DATE),
    FAX(0, DateOrDateTime.DATE_TIME),
    EMAIL(0, DateOrDateTime.DATE_TIME),
    OTHER(2, DateOrDateTime.DATE_TIME);

    private final int days;
    private final DateOrDateTime dateOrDateTime;

    private enum DateOrDateTime {
        DATE,
        DATE_TIME
    }

    public boolean requiresDateEntry() {
        return this.dateOrDateTime == DateOrDateTime.DATE;
    }

    public LocalDate getDeemedDateOfService(LocalDateTime serviceTime) {
        if (isFaxOrEmail() && isAfter4pm(serviceTime)) {
            return serviceTime.toLocalDate().plusDays(1);
        }

        return serviceTime.toLocalDate().plusDays(this.days);
    }

    private boolean isFaxOrEmail() {
        return this == FAX || this == EMAIL;
    }

    private boolean isAfter4pm(LocalDateTime serviceTime) {
        return serviceTime.toLocalTime().isAfter(LocalTime.of(15, 59, 59));
    }
}
