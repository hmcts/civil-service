package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsepartadmitpayimmediately;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponsePartAdmitPayImmediatelyAllPartiesEmailGeneratorTest {

    @Mock
    private ClaimantResponsePartAdmitPayImmediatelyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private ClaimantResponsePartAdmitPayImmediatelyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @InjectMocks
    private ClaimantResponsePartAdmitPayImmediatelyAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
