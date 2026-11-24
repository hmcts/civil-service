package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GAResponseDeadlineFilterTest {

    @Mock
    private Time time;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private GAResponseDeadlineFilter filter;

    @Test
    void shouldReturnTrueWhenDeadlineIsExpired() {
        LocalDateTime now = LocalDateTime.now();
        when(time.now()).thenReturn(now);

        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppNotificationDeadlineDate(now.minusDays(1));
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        assertThat(filter.isDeadlineExpired(caseDetails)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDeadlineIsInFuture() {
        LocalDateTime now = LocalDateTime.now();
        when(time.now()).thenReturn(now);

        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppNotificationDeadlineDate(now.plusDays(1));
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        assertThat(filter.isDeadlineExpired(caseDetails)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenDeadlineIsNull() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppNotificationDeadlineDate(null);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(caseData);

        assertThat(filter.isDeadlineExpired(caseDetails)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenConverterThrowsException() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenThrow(new RuntimeException("Conversion failed"));

        assertThat(filter.isDeadlineExpired(caseDetails)).isFalse();
    }
}
