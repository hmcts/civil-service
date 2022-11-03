package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_NOTIFICATION;

@ExtendWith(SpringExtension.class)
class EvidenceUploadNotificationEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private EvidenceUploadNotificationEventHandler handler;

    @Test
    void shouldCallTriggerEventWithExpectedParams_WhenEvidenceUploadEvent() {
        EvidenceUploadNotificationEvent event = new EvidenceUploadNotificationEvent(1L);
        handler.sendEvidenceUploadNotification(event);
        verify(coreCaseDataService).triggerEvent(event.getCaseId(), EVIDENCE_UPLOAD_NOTIFICATION);
    }

}
