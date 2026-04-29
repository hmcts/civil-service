package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.OtherPartyQueryResponseNotifier;

@ExtendWith(MockitoExtension.class)
class OtherPartyQueryResponseNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    private OtherPartyQueryResponseAllPartiesEmailGenerator allPartiesEmailGenerator;

    @InjectMocks
    private OtherPartyQueryResponseNotifier notifier;

    @Test
    void shouldReturnTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo(OtherPartyQueryResponseNotifier.toString());
    }
}
