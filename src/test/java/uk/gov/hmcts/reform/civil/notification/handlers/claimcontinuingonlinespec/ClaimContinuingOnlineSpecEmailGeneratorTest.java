package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecEmailGeneratorTest {

    @InjectMocks
    private ClaimContinuingOnlineSpecEmailGenerator emailGenerator;

    @Test
    void shouldInitializeParentClassWithCorrectArguments() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}