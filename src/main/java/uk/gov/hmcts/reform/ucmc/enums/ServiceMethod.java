package uk.gov.hmcts.reform.ucmc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public enum ServiceMethod {
    POST(2),
    DOCUMENT_EXCHANGE(2),
    FAX(0),
    EMAIL(0),
    OTHER(2);

    private final int days;

    public LocalDate getDeemedDateOfService(LocalDateTime serviceTime) {
        if (this == FAX || this == EMAIL) {
            if (serviceTime.toLocalTime().isAfter(LocalTime.of(15, 59, 59))) {
                return serviceTime.toLocalDate().plusDays(1);
            }
        }

        return serviceTime.toLocalDate().plusDays(this.days);
    }

    public LocalDate getDeemedDateOfService(LocalDate serviceTime) {
        return getDeemedDateOfService(serviceTime.atStartOfDay());
    }
}
