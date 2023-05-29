package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class DeadlineExtensionCalculatorServiceTest {

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldReturnTheSameGivenDateWhenDateIsWorkday() {
        given(workingDayIndicator.isWorkingDay(any())).willReturn(true);
        LocalDate proposedExtensionDeadline = LocalDate.now();

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline);

        assertThat(calculatedDeadline).isEqualTo(proposedExtensionDeadline);
        verify(workingDayIndicator).isWorkingDay(proposedExtensionDeadline);
        verify(workingDayIndicator, never()).getNextWorkingDay(proposedExtensionDeadline);
    }

    @Test
    void shouldReturnNextWorkingDayWhenDateIsHoliday() {
        given(workingDayIndicator.isWorkingDay(any())).willReturn(false);
        LocalDate calculatedNextWorkingDay = LocalDate.of(2022, 6, 4);
        given(workingDayIndicator.getNextWorkingDay(any())).willReturn(calculatedNextWorkingDay);
        LocalDate proposedExtensionDeadline = LocalDate.of(2022, 6, 3);

        LocalDate calculatedDeadline = deadlineExtensionCalculatorService.calculateExtendedDeadline(
            proposedExtensionDeadline);

        assertThat(calculatedDeadline).isEqualTo(calculatedNextWorkingDay);
        verify(workingDayIndicator).isWorkingDay(proposedExtensionDeadline);
        verify(workingDayIndicator).getNextWorkingDay(proposedExtensionDeadline);
    }

    @Test
    void shouldReturnRespondentSolicitor1AgreedDeadlineExtension() {
        //Given
        LocalDate agreedDeadlineExpected = LocalDate.now();
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
        CaseDetails caseDetails = CaseDetails.builder().build();
        given(coreCaseDataService.getCase(1L, "AUTH"))
            .willReturn(caseDetails);
        given(caseDetailsConverter.toCaseData(caseDetails))
            .willReturn(caseData);
        //When
        LocalDate agreedDeadline = deadlineExtensionCalculatorService.getAgreedDeadlineResponseDate(1L, "AUTH");
        //Then
        assertThat(agreedDeadline).isEqualTo(agreedDeadlineExpected);
    }
}
