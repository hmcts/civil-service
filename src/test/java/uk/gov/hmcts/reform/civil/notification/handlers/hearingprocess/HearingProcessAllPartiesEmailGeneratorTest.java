package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HearingProcessAllPartiesEmailGeneratorTest {

    @Mock
    private HearingProcessAppSolEmailDTOGenerator appSolEmailGenerator;

    @Mock
    private HearingProcessRespSolOneEmailDTOGenerator respOneEmailGenerator;

    @Mock
    private HearingProcessRespSolTwoEmailDTOGenerator respTwoEmailGenerator;

    @Mock
    private HearingProcessClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private HearingProcessDefendantEmailDTOGenerator defendantGenerator;

    @InjectMocks
    private HearingProcessAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
