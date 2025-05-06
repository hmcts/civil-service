package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class AddDefendantLitigationFriendNotifierTest extends NotifierTestBase {

    @InjectMocks
    private AddDefendantLitigationFriendNotifier notifier;

    @Test
    void shouldReturnCorrectTaskId() {
        String taskId = notifier.getTaskId();

        assertThat(taskId).isEqualTo("LitigationFriendAddedNotifier");
    }
}

