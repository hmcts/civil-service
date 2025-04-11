package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantLipHelpWithFeesNotifierTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyClaimantLipHelpWithFeesNotifier notifier;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .claimantUserDetails(IdamUserDetails.builder().email("claimant@hmcts.net").build())
                .build();
    }

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(notifier.getTaskId()).isEqualTo("ClaimantLipHelpWithFeesNotifier");
    }

    @Test
    void shouldReturnCorrectPartiesToNotify() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFees()).thenReturn("template-id");

        Set<EmailDTO> partiesToNotify = notifier.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).hasSize(1);
        EmailDTO emailDTO = partiesToNotify.iterator().next();
        assertThat(emailDTO.getTargetEmail()).isEqualTo("claimant@hmcts.net");
        assertThat(emailDTO.getEmailTemplate()).isEqualTo("template-id");
        assertThat(emailDTO.getParameters()).containsEntry("claimReferenceNumber", "000DC001");
    }

    @Test
    void shouldReturnCorrectPartiesToNotifyWelsh() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh()).thenReturn("template-id-welsh");

        caseData = caseData.toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();

        Set<EmailDTO> partiesToNotify = notifier.getPartiesToNotify(caseData);

        assertThat(partiesToNotify).hasSize(1);
        EmailDTO emailDTO = partiesToNotify.iterator().next();
        assertThat(emailDTO.getTargetEmail()).isEqualTo("claimant@hmcts.net");
        assertThat(emailDTO.getEmailTemplate()).isEqualTo("template-id-welsh");
        assertThat(emailDTO.getParameters()).containsEntry("claimReferenceNumber", "000DC001");
    }
}
