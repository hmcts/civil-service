package uk.gov.hmcts.reform.civil.notification.handlers.raisequery.otherpartyqueryraised;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OtherPartyQueryRaisedAllPartiesEmailGeneratorTest {

    @Mock
    private OtherPartyQueryRaisedDefendantEmailDTOGenerator defendantGenerator;

    @Mock
    private OtherPartyQueryRaisedClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private OtherPartyQueryRaisedAppSolOneEmailDTOGenerator appSolOneGenerator;

    @Mock
    private OtherPartyQueryRaisedRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @InjectMocks
    private OtherPartyQueryRaisedAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
