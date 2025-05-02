package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.online.fulldefence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend.AddDefLitFriendAllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.DefendantResponseUnspecFullDefenceNotifyParties;

class DefendantResponseUnspecNotifierTest {

    private DefendantResponseUnspecNotifier notifier;

    @BeforeEach
    void setUp() {
        NotificationService notificationService = mock(NotificationService.class);
        CaseTaskTrackingService caseTaskTrackingService = mock(CaseTaskTrackingService.class);
        AddDefLitFriendAllLegalRepsEmailGenerator emailGenerator = mock(AddDefLitFriendAllLegalRepsEmailGenerator.class);

        notifier = new DefendantResponseUnspecNotifier(notificationService, caseTaskTrackingService, emailGenerator);
    }

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId())
            .isEqualTo(DefendantResponseUnspecFullDefenceNotifyParties.toString());
    }
}
