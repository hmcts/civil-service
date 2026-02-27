package uk.gov.hmcts.reform.civil.notification.handlers.notifydefendantclaimantsettletheclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotifyDefendantClaimantSettleTheClaimAllPartiesEmailGeneratorTest {

    @Mock
    private NotifyDefendantClaimantSettleTheClaimDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Mock
    private NotifyDefendantClaimantSettleTheClaimRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @InjectMocks
    private NotifyDefendantClaimantSettleTheClaimAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
