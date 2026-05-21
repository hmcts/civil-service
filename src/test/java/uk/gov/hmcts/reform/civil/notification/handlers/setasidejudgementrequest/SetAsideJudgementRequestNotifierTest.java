package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.SetAsideJudgementRequestNotifier;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementRequestNotifierTest {

    @InjectMocks
    private SetAsideJudgementRequestNotifier notifier;

    @Test
    void shouldReturnTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo(SetAsideJudgementRequestNotifier.toString());
    }
}
