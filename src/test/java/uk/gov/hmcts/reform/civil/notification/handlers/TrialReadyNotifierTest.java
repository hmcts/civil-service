package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class TrialReadyNotifierTest {

    public static final Long CASE_ID = 1594901956117591L;
    @Mock
    NotificationService notificationService;

    @Test
    void shouldReturnNoPartiesWithNoMatchingTaskId() {
        final TrialReadyNotifier trialReadyNotifier = new TrialReadyNotifier(notificationService, null, null, null, null) {
            @Override
            protected String getTaskId() {
                return "";
            }
        };

        final CaseData caseData = mock(CaseData.class);

        assertThat(trialReadyNotifier.getPartiesToNotify(caseData)).isEmpty();
    }
}
