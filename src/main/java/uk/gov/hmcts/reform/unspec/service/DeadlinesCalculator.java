package uk.gov.hmcts.reform.unspec.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.enums.ServiceMethodType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.EMAIL;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.FAX;

@Service
@RequiredArgsConstructor
public class DeadlinesCalculator {

    public static final LocalTime MID_NIGHT = LocalTime.of(23, 59, 59);

    private final WorkingDayIndicator workingDayIndicator;

    public LocalDate calculateDeemedDateOfService(
        @NonNull LocalDateTime dateOfService,
        @NonNull ServiceMethodType serviceMethodType
    ) {
        if (isFaxOrEmail(serviceMethodType) && is4pmOrAfter(dateOfService)) {
            return dateOfService.toLocalDate().plusDays(1);
        }
        return dateOfService.toLocalDate().plusDays(serviceMethodType.getDays());
    }

    public LocalDate calculateDeemedDateOfService(
        @NonNull LocalDate dateOfService,
        @NonNull ServiceMethodType serviceMethod
    ) {
        return calculateDeemedDateOfService(dateOfService.atStartOfDay(), serviceMethod);
    }

    private boolean is4pmOrAfter(@NonNull LocalDateTime dateOfService) {
        return dateOfService.getHour() >= 16;
    }

    private boolean isFaxOrEmail(@NonNull ServiceMethodType serviceMethod) {
        return serviceMethod == FAX || serviceMethod == EMAIL;
    }

    public LocalDateTime calculateRespondentResponseDeadline(@NonNull LocalDate deemedDateOfService) {
        LocalDate responseDeadline = deemedDateOfService.plusDays(14);
        return calculateFirstWorkingDay(responseDeadline).atTime(MID_NIGHT);
    }

    public LocalDateTime calculateConfirmationOfServiceDeadline(@NonNull LocalDate issueDate) {
        LocalDate confirmationOfService = issueDate.plusMonths(4);
        return calculateFirstWorkingDay(confirmationOfService).atTime(MID_NIGHT);
    }

    public LocalDate calculateFirstWorkingDay(LocalDate date) {
        while (!workingDayIndicator.isWorkingDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
