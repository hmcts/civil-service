package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@ExtendWith(MockitoExtension.class)
class TakeCaseOfflineForSpecPartiesEmailGeneratorTest {

    @Mock
    private TakeCaseOfflineForSpecAppSolOneEmailDTOGenerator appGen;

    @InjectMocks
    private TakeCaseOfflineForSpecPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        AssertionsForClassTypes.assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }

}