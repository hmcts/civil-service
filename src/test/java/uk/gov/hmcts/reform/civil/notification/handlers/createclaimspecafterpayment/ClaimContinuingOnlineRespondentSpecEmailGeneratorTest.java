
package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineRespondentSpecEmailGeneratorTest {

    @Mock
    private ClaimContinuingOnlineRespondentSpecRespSolOneEmailDTOGenerator respOneGenerator;

    @Mock
    private ClaimContinuingOnlineRespondentSpecRespSolTwoEmailDTOGenerator respTwoGenerator;

    @InjectMocks
    private ClaimContinuingOnlineRespondentSpecEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}