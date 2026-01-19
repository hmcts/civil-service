package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CreateSdoDashboardDateService {

    private final WorkingDayIndicator workingDayIndicator;

    public CreateSdoDashboardDateService(WorkingDayIndicator workingDayIndicator) {
        this.workingDayIndicator = workingDayIndicator;
    }

    public LocalDateTime getDateWithoutBankHolidays(LocalDateTime fromDateTime) {
        LocalDate date = fromDateTime.toLocalDate();
        try {
            for (int i = 0; i < 7; i++) {
                if (workingDayIndicator.isPublicHoliday(date)) {
                    date = date.plusDays(2);
                } else {
                    date = date.plusDays(1);
                }
            }
        } catch (Exception e) {
            log.error("Error when retrieving public days");
            date = LocalDate.now().plusDays(7);
        }

        return date.atTime(16, 0, 0);
    }
}
