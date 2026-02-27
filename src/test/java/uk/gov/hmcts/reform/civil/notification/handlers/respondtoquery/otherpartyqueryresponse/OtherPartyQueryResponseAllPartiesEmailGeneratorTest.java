package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OtherPartyQueryResponseAllPartiesEmailGeneratorTest {

    @Mock
    private OtherPartyQueryResponseAppSolEmailDTOGenerator appSolGenerator;

    @Mock
    private OtherPartyQueryResponseRespSolOneEmailDTOGenerator respSolOneGenerator;

    @Mock
    private OtherPartyQueryResponseRespSolTwoEmailDTOGenerator respSolTwoGenerator;

    @Mock
    private OtherPartyQueryResponseClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private OtherPartyQueryResponseDefendantEmailDTOGenerator defendantGenerator;

    @InjectMocks
    private OtherPartyQueryResponseAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
