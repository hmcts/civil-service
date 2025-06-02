package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansAllPartiesEmailGeneratorTest {

    @Mock
    private JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator claimantGen;

    @Mock
    private JudgmentVariedDeterminationOfMeansAppSolOneEmailDTOGenerator appSolGen;

    @Mock
    private JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGenerator sol1Gen;

    @Mock
    private JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator sol2Gen;

    @Mock
    private JudgmentVariedDeterminationOfMeansLipDefendantEmailDTOGenerator lipGen;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
