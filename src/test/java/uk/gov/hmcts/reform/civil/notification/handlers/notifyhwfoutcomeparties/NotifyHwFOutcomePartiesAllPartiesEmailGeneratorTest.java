package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NotifyHwFOutcomePartiesAllPartiesEmailGeneratorTest {

    private NotifyHwFOutcomePartiesAllPartiesEmailGenerator generator;

    @BeforeEach
    void setUp() {
        NotifyHwFOutcomePartiesClaimantEmailDTOGenerator claimantEmailDTOGenerator = mock(NotifyHwFOutcomePartiesClaimantEmailDTOGenerator.class);
        generator = new NotifyHwFOutcomePartiesAllPartiesEmailGenerator(claimantEmailDTOGenerator);
    }

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
