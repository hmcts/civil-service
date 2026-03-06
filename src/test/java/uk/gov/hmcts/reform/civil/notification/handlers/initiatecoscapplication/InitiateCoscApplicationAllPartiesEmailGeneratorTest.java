package uk.gov.hmcts.reform.civil.notification.handlers.initiatecoscapplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InitiateCoscApplicationAllPartiesEmailGeneratorTest {

    @Mock
    private InitiateCoscApplicationAppSolEmailDTOGenerator appSolEmailDTOGenerator;

    @InjectMocks
    private InitiateCoscApplicationAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
