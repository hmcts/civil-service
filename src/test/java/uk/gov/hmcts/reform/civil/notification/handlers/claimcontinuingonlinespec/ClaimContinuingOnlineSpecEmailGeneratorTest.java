package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecEmailGeneratorTest {

    @Mock
    private ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen;

    @Mock
    private ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen;

    @Mock
    private ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen;

    @Mock
    private ClaimContinuingOnlineSpecClaimantEmailDTOGenerator claimantGen;

    @Mock
    private ClaimContinuingOnlineSpecDefendantEmailDTOGenerator defendantGen;

    @InjectMocks
    private ClaimContinuingOnlineSpecAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldInitializeParentClassWithCorrectArguments() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}