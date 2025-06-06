package uk.gov.hmcts.reform.civil.notification.handlers.createclaimafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentOfflineAllPartiesEmailGeneratorTest {

    @Mock
    private CreateClaimAfterPaymentOfflineAppSolOneEmailDTOGenerator appSolOneGenerator;

    @Mock
    private CreateClaimAfterPaymentOfflineClaimantEmailDTOGenerator claimantEmailGenerator;

    @InjectMocks
    private CreateClaimAfterPaymentOfflineAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

}
