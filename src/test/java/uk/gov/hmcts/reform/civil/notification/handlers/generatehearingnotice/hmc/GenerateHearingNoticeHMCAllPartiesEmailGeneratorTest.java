package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHMCAllPartiesEmailGeneratorTest {

    @Mock
    private GenerateHearingNoticeHMCAppSolEmailDTOGenerator appSolGen;

    @Mock
    private GenerateHearingNoticeHMCRespSolOneEmailDTOGenerator respSolOneGen;

    @Mock
    private GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator respSolTwoGen;

    @Mock
    private GenerateHearingNoticeHMCClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private GenerateHearingNoticeHMCDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Test
    void shouldConstructSuccessfully() {
        GenerateHearingNoticeHMCAllPartiesEmailGenerator emailGenerator =
            new GenerateHearingNoticeHMCAllPartiesEmailGenerator(
                appSolGen,
                respSolOneGen,
                respSolTwoGen,
                claimantEmailDTOGenerator,
                defendantEmailDTOGenerator
            );

        assertThat(emailGenerator).isNotNull();
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
