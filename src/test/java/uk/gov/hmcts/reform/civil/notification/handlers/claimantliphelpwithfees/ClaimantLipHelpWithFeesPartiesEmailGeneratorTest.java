package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClaimantLipHelpWithFeesPartiesEmailGeneratorTest {

    @InjectMocks
    private ClaimantLipHelpWithFeesPartiesEmailGenerator emailGenerator;

    @Test
    void shouldInitializeParentClassWithCorrectArguments() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

    @Test
    void shouldAlwaysReturnFalseForShouldNotifyRespondents() {
        CaseData caseData = CaseData.builder().build();
        boolean result = emailGenerator.shouldNotifyRespondents(caseData);
        assertThat(result).isFalse();
    }
}
