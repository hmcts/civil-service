package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JudgmentByAdmissionAllPartiesEmailGeneratorTest {

    @Mock
    private JudgmentByAdmissionAppSolOneEmailDTOGenerator appSolOneEmailGenerator;

    @Mock
    private JudgmentByAdmissionDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private JudgmentByAdmissionAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldDoJudgmentByAdmissionAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
