package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecAllPartiesEmailGeneratorTest {

    @Mock
    private InformAgreedExtensionDateSpecAppSolOneEmailDTOGenerator applicantGenerator;

    @Mock
    private InformAgreedExtensionDateSpecClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private InformAgreedExtensionDateSpecRespSolOneEmailDTOGenerator respondentOneGenerator;

    @Mock
    private InformAgreedExtensionDateSpecRespSolTwoEmailDTOGenerator respondentTwoGenerator;

    @InjectMocks
    private InformAgreedExtensionDateSpecAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
