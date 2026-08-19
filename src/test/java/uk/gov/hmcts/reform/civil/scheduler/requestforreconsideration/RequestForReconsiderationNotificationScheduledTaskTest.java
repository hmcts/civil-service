package uk.gov.hmcts.reform.civil.scheduler.requestforreconsideration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.RequestForReconsiderationNotificationDeadlineEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestForReconsiderationNotificationScheduledTaskTest {

    private static final Long CASE_ID = 123L;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @InjectMocks
    private RequestForReconsiderationNotificationScheduledTask task;

    @Test
    void shouldReturnCaseId() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();

        assertThat(task.getItemId(caseDetails)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishRequestForReconsiderationNotificationDeadlineEvent() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(new RequestForReconsiderationNotificationDeadlineEvent(CASE_ID));
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }
}
