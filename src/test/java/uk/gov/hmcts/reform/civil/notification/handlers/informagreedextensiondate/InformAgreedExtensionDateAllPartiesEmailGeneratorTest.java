package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateAllPartiesEmailGeneratorTest {

    @Mock
    private InformAgreedExtensionDateAppSolOneEmailDTOGenerator applicantGenerator;

    @Mock
    private InformAgreedExtensionDateRespSolOneEmailDTOGenerator respondentOneGenerator;

    @Mock
    private InformAgreedExtensionDateRespSolTwoEmailDTOGenerator respondentTwoGenerator;

    @InjectMocks
    private InformAgreedExtensionDateAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
