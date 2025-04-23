package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Nested
class ClaimDismissedAllPartiesEmailGeneratorTest {

    @InjectMocks
    private ClaimDismissedAllPartiesEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldNotifyRespondents_whenStateIsClaimDismissedPastDeadline() {
        CaseData caseData = CaseData.builder()
            .claimDismissedDate()
            .build();
        caseData.
        boolean result = emailGenerator.shouldNotifyRespondents(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotNotifyRespondents_whenStateIsNotClaimDismissedPastDeadline() {
        CaseData caseData = mock(CaseData.class);
        boolean result = emailGenerator.shouldNotifyRespondents(caseData);

        assertThat(result).isFalse();
    }
}

