package uk.gov.hmcts.reform.civil.scheduler.evidenceupload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadSchedulerTaskTest {

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private EvidenceUploadSchedulerTask task;

    @Test
    void shouldPublishEvent() {
        Long caseId = 123L;
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();
        EvidenceUploadNotificationEvent evidenceUploadNotificationEvent = new EvidenceUploadNotificationEvent(caseId);

        task.accept(caseDetails);

        verify(applicationEventPublisher).publishEvent(evidenceUploadNotificationEvent);
    }
}
