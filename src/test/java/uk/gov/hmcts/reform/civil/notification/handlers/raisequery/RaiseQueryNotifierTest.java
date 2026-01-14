package uk.gov.hmcts.reform.civil.notification.handlers.raisequery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.RaiseQueryNotifier;

@ExtendWith(MockitoExtension.class)
public class RaiseQueryNotifierTest {

    @InjectMocks
    private RaiseQueryNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo(RaiseQueryNotifier.toString());
    }
}
