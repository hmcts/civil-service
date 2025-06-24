package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GenerateHearingNoticeHMCAllPartiesEmailGeneratorTest {

    @InjectMocks
    private GenerateHearingNoticeHMCAllPartiesEmailGenerator emailGenerator;

    @Mock
    private GenerateHearingNoticeHMCAppSolEmailDTOGenerator appSolGen;

    @Mock
    private GenerateHearingNoticeHMCRespSolOneEmailDTOGenerator respSolOneGen;

    @Mock
    private GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator respSolTwoGen;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
