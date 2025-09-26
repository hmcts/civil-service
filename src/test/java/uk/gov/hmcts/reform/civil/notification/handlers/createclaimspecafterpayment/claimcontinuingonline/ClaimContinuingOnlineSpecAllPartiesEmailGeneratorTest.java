package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecAllPartiesEmailGeneratorTest {

    @Mock
    private ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen;

    @Mock
    private ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen;

    @Mock
    private ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen;

    @Mock
    private ClaimContinuingOnlineSpecClaimantEmailDTOGenerator claimantGen;

    @Mock
    private ClaimContinuingOnlineSpecDefendantEmailDTOGenerator defendantGe;

    @InjectMocks
    private ClaimContinuingOnlineSpecAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}