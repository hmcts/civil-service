package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ServiceOfDateValidationMessageUtils {

    public static final String DOC_SERVED_DATE_IN_FUTURE =
        "On what day did you serve must be today or in the past";

    public static final String DOC_SERVED_DATE_OLDER_THAN_14DAYS =
        "On what day did you serve should not be more than 14 days old";

    public static final String DATE_OF_SERVICE_NOT_GREATER_THAN_2_WORKING_DAYS =
        "The date of service must be no greater than 2 working days in the future";

    public static final String DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS =
        "The date of service should not be more than 14 days old";

    public static final String DATE_OF_SERVICE_DATE_IS_WORKING_DAY =
        "For the date of service please enter a working day";

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    private final DeadlinesCalculator deadlinesCalculator;
    private final WorkingDayIndicator workingDayIndicator;
    private final Time time;

    public List<String> getServiceOfDateValidationMessages(CertificateOfService certificateOfService) {
        List<String> errorMessages = new ArrayList<>();
        if (Objects.nonNull(certificateOfService)) {
            if (isCosDefendantNotifyDateFutureDate(certificateOfService.getCosDateOfServiceForDefendant())) {
                errorMessages.add(DOC_SERVED_DATE_IN_FUTURE);
            }

            if (isCosDefendantNotifyDateOlderThan14Days(certificateOfService.getCosDateOfServiceForDefendant())) {
                errorMessages.add(DOC_SERVED_DATE_OLDER_THAN_14DAYS);
            }

            if (isDeemedServedWithinMaxWorkingDays(certificateOfService.getCosDateDeemedServedForDefendant())) {
                errorMessages.add(DATE_OF_SERVICE_NOT_GREATER_THAN_2_WORKING_DAYS);
            }

            if (isDeemedServedDateOlderThan14Days(certificateOfService.getCosDateDeemedServedForDefendant())) {
                errorMessages.add(DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS);
            }

            if (isDeemedServedDateIsNotWorkingDay(certificateOfService.getCosDateDeemedServedForDefendant())) {
                errorMessages.add(DATE_OF_SERVICE_DATE_IS_WORKING_DAY);
            }
        }
        return errorMessages;
    }

    public boolean isCosDefendantNotifyDateFutureDate(LocalDate cosDateOfServiceForDefendant) {
        return time.now().toLocalDate().isBefore(cosDateOfServiceForDefendant);
    }

    public boolean isCosDefendantNotifyDateOlderThan14Days(LocalDate cosDateOfServiceForDefendant) {
        LocalDateTime notificationDeadline = cosDateOfServiceForDefendant.atTime(END_OF_BUSINESS_DAY).plusDays(14);
        LocalDateTime currentDateTime = time.now();
        LocalDateTime today4pm = currentDateTime.toLocalDate().atTime(16, 0);

        boolean isAfter4pmToday = currentDateTime.isAfter(today4pm)
            && currentDateTime.toLocalDate().equals(notificationDeadline.toLocalDate());

        boolean isAfter14DaysAt4pmDeadline = currentDateTime.isAfter(notificationDeadline);

        return isAfter14DaysAt4pmDeadline || isAfter4pmToday;
    }

    public boolean isDeemedServedDateOlderThan14Days(LocalDate cosDateOfServiceForDefendant) {
        LocalDateTime deemedServedDeadline = cosDateOfServiceForDefendant.atTime(END_OF_BUSINESS_DAY).plusDays(14);
        LocalDateTime currentDateTime = time.now();
        LocalDateTime today4pm = currentDateTime.toLocalDate().atTime(16, 0);

        boolean isAfter4pmToday = currentDateTime.isAfter(today4pm)
            && currentDateTime.toLocalDate().equals(deemedServedDeadline.toLocalDate());

        boolean isAfter14DaysAt4pmDeadline = currentDateTime.isAfter(deemedServedDeadline);

        return isAfter14DaysAt4pmDeadline || isAfter4pmToday;
    }

    public boolean isDeemedServedWithinMaxWorkingDays(LocalDate cosDateOfServiceForDefendant) {
        LocalDate currentDate = LocalDate.now();
        LocalDate maxWorkingDaysDate = deadlinesCalculator.plusWorkingDays(currentDate, 2);

        return cosDateOfServiceForDefendant.isAfter(maxWorkingDaysDate);
    }

    public boolean isDeemedServedDateIsNotWorkingDay(LocalDate cosDateOfServiceForDefendant) {
        boolean isOlderThan14Days = isDeemedServedDateOlderThan14Days(cosDateOfServiceForDefendant);
        return !isOlderThan14Days && !workingDayIndicator.isWorkingDay(cosDateOfServiceForDefendant);
    }
}
