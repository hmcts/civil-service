package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DjNonDivergentAllPartiesEmailGeneratorTest {

    @Mock
    private DjNonDivergentApplicantLREmailDTOGenerator applicantLRGenerator;

    @Mock
    private DjNonDivergentApplicantLipEmailDTOGenerator applicantLipGenerator;

    @Mock
    private DjNonDivergentDefendant1LREmailDTOGenerator defendant1LRGenerator;

    @Mock
    private DjNonDivergentDefendant1LipEmailDTOGenerator defendant1LipGenerator;

    @Mock
    private DjNonDivergentDefendant2LREmailDTOGenerator defendant2LRGenerator;

    @InjectMocks
    private DjNonDivergentAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
