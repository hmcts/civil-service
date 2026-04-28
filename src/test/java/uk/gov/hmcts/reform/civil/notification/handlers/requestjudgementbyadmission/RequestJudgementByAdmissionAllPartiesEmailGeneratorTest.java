package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RequestJudgementByAdmissionAllPartiesEmailGeneratorTest {

    @Mock
    private RequestJudgementByAdmissionApplicantEmailDTOGenerator applicantEmailDTOGenerator;

    @Mock
    private RequestJudgementByAdmissionAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private RequestJudgementByAdmissionLipRespondentEmailDTOGenerator lipRespondentEmailDTOGenerator;

    @Mock
    private RequestJudgementByAdmissionRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private RequestJudgementByAdmissionDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private RequestJudgementByAdmissionAllPartiesEmailGenerator emailGenerator;

    @Test
    void shouldExtendAllPartiesEmailGeneratorWithCorrectDependencies() {
        assertThat(emailGenerator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
