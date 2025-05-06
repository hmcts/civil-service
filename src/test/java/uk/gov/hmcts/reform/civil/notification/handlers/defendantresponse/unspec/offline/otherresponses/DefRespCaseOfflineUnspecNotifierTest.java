package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseUnspecCaseHandedOfflineNotifyParties;

class DefRespCaseOfflineUnspecNotifierTest extends NotifierTestBase {

    @InjectMocks
    private DefRespCaseOfflineUnspecNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
            .isEqualTo(DefendantResponseUnspecCaseHandedOfflineNotifyParties.toString());
    }
}
