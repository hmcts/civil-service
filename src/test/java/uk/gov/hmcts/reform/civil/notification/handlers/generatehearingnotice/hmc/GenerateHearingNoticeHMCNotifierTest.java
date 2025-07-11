package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.HearingNoticeGeneratorHMCNotifier;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHMCNotifierTest {

    @InjectMocks
    private GenerateHearingNoticeHMCNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
                .isEqualTo(HearingNoticeGeneratorHMCNotifier.toString());
    }
}
