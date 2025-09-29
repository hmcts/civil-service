package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimsubmission;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@ExtendWith(MockitoExtension.class)
class ClaimSubmissionAllPartiesEmailGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    @Mock
    private ClaimSubmissionClaimantEmailDTOGenerator claimantGenerator;

    @InjectMocks
    private ClaimSubmissionAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        AssertionsForClassTypes.assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

}