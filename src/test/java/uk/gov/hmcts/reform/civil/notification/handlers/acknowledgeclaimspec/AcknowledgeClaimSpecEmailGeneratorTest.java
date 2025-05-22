package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimSpecEmailGeneratorTest {

    @Mock
    private AcknowledgeClaimSpecAppSolOneEmailDTOGenerator appSolOne;

    @Mock
    private AcknowledgeClaimSpecRespSolOneEmailDTOGenerator respSolOne;

    @Mock
    private AcknowledgeClaimSpecRespSolTwoEmailDTOGenerator respSolTwo;

    @InjectMocks
    private AcknowledgeClaimSpecEmailGenerator emailGenerator;

    @Test
    void shouldInitializeParentClassWithCorrectArguments() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
