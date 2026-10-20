package uk.gov.hmcts.reform.civil.scheduler.evidenceupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadSchedulerTaskTest {

    private static final long CASE_ID = 123L;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @InjectMocks
    private EvidenceUploadSchedulerTask task;

    @Test
    void shouldReturnCaseId() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        assertThat(task.getItemId(caseDetails)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldPublishEvent() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();
        EvidenceUploadNotificationEvent evidenceUploadNotificationEvent = new EvidenceUploadNotificationEvent(caseId);

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(evidenceUploadNotificationEvent);
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }
}
