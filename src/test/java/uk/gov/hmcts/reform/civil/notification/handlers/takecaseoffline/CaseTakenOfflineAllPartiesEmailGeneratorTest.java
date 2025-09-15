package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineAllPartiesEmailGeneratorTest {

    @Mock
    private CaseTakenOfflineAppSolOneEmailDTOGenerator appSolOneGenerator;

    @Mock
    private CaseTakenOfflineRespSolOneEmailDTOGenerator respSolOneGenerator;

    @Mock
    private CaseTakenOfflineRespSolTwoEmailDTOGenerator respSolTwoGenerator;

    @Mock
    private CaseTakenOfflineAppLipSolOneEmailDTOGenerator appSolLipOneGenerator;

    @Mock
    private CaseTakenOfflineRespLipSolOneEmailDTOGenerator respLipSolOneEmailDTOGenerator;

    @InjectMocks
    private CaseTakenOfflineAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
