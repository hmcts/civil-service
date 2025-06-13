package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.TakeCaseOfflineNotifier;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineNotifierTest {

    @InjectMocks
    private CaseTakenOfflineNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo(TakeCaseOfflineNotifier.toString());
    }
}
