package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class NotifyClaimantHwFOutcomeRespSolTwoEmailDTOGeneratorTest {
    private NotifyClaimantHwFOutcomeRespSolTwoEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        NotificationsProperties notificationsProperties = mock(NotificationsProperties.class);
//        NotifyClaimantHwFOutcomeHelper helper = mock(NotifyClaimantHwFOutcomeHelper.class);
        generator = new NotifyClaimantHwFOutcomeRespSolTwoEmailDTOGenerator(notificationsProperties);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        // When
        String referenceTemplate = generator.getReferenceTemplate();

        // Then
        assertThat(referenceTemplate).isEqualTo("hwf-outcome-notification-%s");
    }
}
