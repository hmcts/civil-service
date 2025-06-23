package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.online.fulldefence;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseUnspecFullDefenceNotifyParties;

class DefendantResponseUnspecNotifierTest extends NotifierTestBase {

    @InjectMocks
    private DefendantResponseUnspecNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
            .isEqualTo(DefendantResponseUnspecFullDefenceNotifyParties.toString());
    }
}
