package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.InformAgreedExtensionDateNotifier;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateNotifierTest {

    @InjectMocks
    private InformAgreedExtensionDateNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo(InformAgreedExtensionDateNotifier.toString());
    }
}
