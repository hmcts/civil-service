package uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SettleClaimPaidInFullNotificationAllPartiesEmailGeneratorTest {

    @Mock
    private SettleClaimPaidInFullNotificationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private SettleClaimPaidInFullNotificationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @InjectMocks
    private SettleClaimPaidInFullNotificationAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
