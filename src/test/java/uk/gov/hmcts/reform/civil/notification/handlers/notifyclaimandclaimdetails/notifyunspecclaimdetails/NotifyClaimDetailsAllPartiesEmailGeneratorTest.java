package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.notifyunspecclaimdetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyClaimDetailsAllPartiesEmailGeneratorTest {

    @Mock
    private NotifyClaimDetailsAppSolEmailDTOGenerator applicantGenerator;
    @Mock
    private NotifyClaimDetailsRespOneSolEmailDTOGenerator respondentOneGenerator;
    @Mock
    private NotifyClaimDetailsRespSolTwoEmailDTOGenerator respondentTwoGenerator;

    private NotifyClaimDetailsAllPartiesEmailGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new NotifyClaimDetailsAllPartiesEmailGenerator(
            applicantGenerator, respondentOneGenerator, respondentTwoGenerator);
    }

    @Test
    void shouldSkipAllRecipientsWhenResponseDeadlineIsMissing() {
        assertThat(generator.getPartiesToNotify(CaseData.builder().build(), "task-id")).isEmpty();

        verifyNoInteractions(applicantGenerator, respondentOneGenerator, respondentTwoGenerator);
    }

    @Test
    void shouldCompleteNotifierWithoutSendingEmailWhenResponseDeadlineIsMissing() {
        NotificationService notificationService = mock(NotificationService.class);
        CaseTaskTrackingService trackingService = mock(CaseTaskTrackingService.class);
        NotifyClaimDetailsAllPartiesNotifier notifier = new NotifyClaimDetailsAllPartiesNotifier(
            notificationService, trackingService, generator);

        String result = notifier.notifyParties(
            CaseData.builder().ccdCaseReference(1730888435901627L).build(), "NOTIFY_EVENT", notifier.getTaskId());

        assertThat(result).isEqualTo("Attempted:  || Errors: ");
        verifyNoInteractions(notificationService, trackingService,
                             applicantGenerator, respondentOneGenerator, respondentTwoGenerator);
    }

    @Test
    void shouldGenerateEmailForEligibleRecipientWhenResponseDeadlineIsPresent() {
        CaseData caseData = CaseData.builder()
            .respondent1ResponseDeadline(LocalDateTime.of(2026, 10, 19, 16, 0))
            .build();
        EmailDTO email = mock(EmailDTO.class);
        when(applicantGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(applicantGenerator.buildEmailDTO(caseData, "task-id")).thenReturn(email);

        assertThat(generator.getPartiesToNotify(caseData, "task-id")).containsExactly(email);
    }
}
