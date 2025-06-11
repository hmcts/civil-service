package uk.gov.hmcts.reform.civil.notification.handlers.dismisscase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)

public class DismissCaseAllPartiesEmailGeneratorTest {

    @Mock
    private DismissCaseAppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private DismissCaseRespSolOneEmailDTOGenerator respSolOneEmailGenerator;

    @Mock
    private DismissCaseRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator;

    @Mock
    private DismissCaseClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private DismissCaseDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private DismissCaseAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
