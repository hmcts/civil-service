package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class ServiceOfDateValidationMessageUtilsTest {

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @Mock
    private Time time;

    @InjectMocks
    private ServiceOfDateValidationMessageUtils serviceUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowError_whenDeemedServedDateIsOlderThan14Days() {
        // Arrange
        LocalDate currentDate = LocalDate.now();
        LocalDate deemedServedDate = currentDate.minusDays(15);
        CertificateOfService certificateOfService = CertificateOfService.builder()
            .cosDateOfServiceForDefendant(currentDate)
            .cosDateDeemedServedForDefendant(deemedServedDate)
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
        when(deadlinesCalculator.plusWorkingDays(currentDate, 2)).thenReturn(LocalDate.now().plusDays(2));
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

        // Act
        List<String> errorMessages = serviceUtils.getServiceOfDateValidationMessages(certificateOfService);

        // Assert
        assertThat(errorMessages).contains(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_DATE_OLDER_THAN_14DAYS);
        assertThat(errorMessages).hasSize(1);
    }

    @Test
    void shouldThrowError_whenCosDefendantNotifyDateIsFutureDate() {
        LocalDate currentDate = LocalDate.now();
        LocalDate futureDate = currentDate.plusDays(1);
        CertificateOfService certificateOfService = CertificateOfService.builder()
            .cosDateOfServiceForDefendant(futureDate)
            .cosDateDeemedServedForDefendant(currentDate)
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
        when(deadlinesCalculator.plusWorkingDays(currentDate, 2)).thenReturn(LocalDate.now().plusDays(2));
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

        List<String> errorMessages = serviceUtils.getServiceOfDateValidationMessages(certificateOfService);

        assertThat(errorMessages).contains(ServiceOfDateValidationMessageUtils.DOC_SERVED_DATE_IN_FUTURE);
        assertThat(errorMessages).hasSize(1);
    }

    @Test
    void shouldThrowError_whenCosDefendantNotifyDateIsOlderThan14Days() {
        LocalDate currentDate = LocalDate.now();
        LocalDate olderDate = currentDate.minusDays(15);

        CertificateOfService certificateOfService = CertificateOfService.builder()
            .cosDateOfServiceForDefendant(olderDate)
            .cosDateDeemedServedForDefendant(currentDate)
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
        when(deadlinesCalculator.plusWorkingDays(currentDate, 2)).thenReturn(LocalDate.now().plusDays(2));
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

        List<String> errorMessages = serviceUtils.getServiceOfDateValidationMessages(certificateOfService);

        assertThat(errorMessages).contains(ServiceOfDateValidationMessageUtils.DOC_SERVED_DATE_OLDER_THAN_14DAYS);
        assertThat(errorMessages).hasSize(1);
    }

    @Test
    void shouldThrowError_whenDeemedServedDateIsNotWorkingDay() {
        LocalDate currentDate = LocalDate.now();
        LocalDate deemedServedDate = currentDate;
        CertificateOfService certificateOfService = CertificateOfService.builder()
            .cosDateOfServiceForDefendant(currentDate)
            .cosDateDeemedServedForDefendant(deemedServedDate)
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false);
        when(deadlinesCalculator.plusWorkingDays(currentDate, 2)).thenReturn(LocalDate.now().plusDays(2));

        List<String> errorMessages = serviceUtils.getServiceOfDateValidationMessages(certificateOfService);

        assertThat(errorMessages).contains(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_DATE_IS_WORKING_DAY);
        assertThat(errorMessages).hasSize(1);
    }

    @Test
    void shouldThrowError_whenCosDefendantNotifyDateIsOlderThan14DaysAndWithin2WorkingDays() {
        // Arrange
        LocalDate currentDate = LocalDate.now();
        LocalDate olderDate = currentDate.minusDays(15);

        CertificateOfService certificateOfService = CertificateOfService.builder()
            .cosDateOfServiceForDefendant(olderDate)
            .cosDateDeemedServedForDefendant(LocalDate.now().plusDays(3))
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
        when(deadlinesCalculator.plusWorkingDays(currentDate, 2)).thenReturn(LocalDate.now().plusDays(2));
        when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

        // Act
        List<String> errorMessages = serviceUtils.getServiceOfDateValidationMessages(certificateOfService);

        // Assert
        assertThat(errorMessages).contains(ServiceOfDateValidationMessageUtils.DOC_SERVED_DATE_OLDER_THAN_14DAYS);
        assertThat(errorMessages).contains(ServiceOfDateValidationMessageUtils.DATE_OF_SERVICE_NOT_GREATER_THAN_2_WORKING_DAYS);
        assertThat(errorMessages).hasSize(2);
    }
}
