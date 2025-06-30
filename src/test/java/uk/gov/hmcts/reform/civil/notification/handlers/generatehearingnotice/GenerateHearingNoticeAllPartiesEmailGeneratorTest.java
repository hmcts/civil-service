package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeAllPartiesEmailGeneratorTest {

    @InjectMocks
    private GenerateHearingNoticeAllPartiesEmailGenerator emailGenerator;

    @Mock
    private GenerateHearingNoticeClaimantEmailDTOGenerator lipGen;

    @Mock
    private GenerateHearingNoticeAppSolOneEmailDTOGenerator appSolGen;

    @Mock
    private GenerateHearingNoticeDefendantEmailDTOGenerator defendantGen;

    @Mock
    private GenerateHearingNoticeRespSolOneEmailDTOGenerator respSolOneGen;

    @Mock
    private GenerateHearingNoticeRespSolOneEmailDTOGenerator respSolTwoGen;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
