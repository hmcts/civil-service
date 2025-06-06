package uk.gov.hmcts.reform.civil.notification.handlers.hearingfeeunpaid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HearingFeeUnpaidAllPartiesEmailGeneratorTest {

    @Mock
    private HearingFeeUnpaidClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private HearingFeeUnpaidAppSolEmailDTOGenerator appSolGenerator;

    @Mock
    private HearingFeeUnpaidDefendantEmailDTOGenerator defendantGenerator;

    @Mock
    private HearingFeeUnpaidRespSolOneEmailDTOGenerator respSolOneGenerator;

    @Mock
    private HearingFeeUnpaidRespSolTwoEmailDTOGenerator respSolTwoGenerator;

    @InjectMocks
    private HearingFeeUnpaidAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
