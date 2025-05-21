package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineApplicantSpecEmailGeneratorTest {

    @Mock
    private ClaimContinuingOnlineApplicantSpecAppSolOneEmailDTOGenerator appGen;

    @Mock
    private ClaimContinuingOnlineApplicantSpecRespSolOneEmailDTOGenerator respOneGen;

    @Mock
    private ClaimContinuingOnlineApplicantSpecRespSolTwoEmailDTOGenerator respTwoGen;

    @InjectMocks
    private ClaimContinuingOnlineApplicantSpecEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}