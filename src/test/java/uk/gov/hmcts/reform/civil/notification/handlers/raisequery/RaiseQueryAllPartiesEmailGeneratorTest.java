package uk.gov.hmcts.reform.civil.notification.handlers.raisequery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RaiseQueryAllPartiesEmailGeneratorTest {

    @Mock
    private RaiseQueryDefendantEmailDTOGenerator defendantGenerator;

    @Mock
    private RaiseQueryClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private RaiseQueryAppSolOneEmailDTOGenerator appSolOneGenerator;

    @Mock
    private RaiseQueryRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @InjectMocks
    private RaiseQueryAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
