package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RespondToQueryAllPartiesEmailGeneratorTest {

    @Mock
    private RespondToQueryAppSolEmailDTOGenerator appSolGenerator;

    @Mock
    private RespondToQueryRespSolOneEmailDTOGenerator respSolOneGenerator;

    @Mock
    private RespondToQueryRespSolTwoEmailDTOGenerator respSolTwoGenerator;

    @Mock
    private RespondToQueryClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private RespondToQueryDefendantEmailDTOGenerator defendantGenerator;

    @InjectMocks
    private RespondToQueryAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
