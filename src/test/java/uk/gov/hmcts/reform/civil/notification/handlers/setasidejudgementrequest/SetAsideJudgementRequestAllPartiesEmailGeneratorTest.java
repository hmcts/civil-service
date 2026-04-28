package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementRequestAllPartiesEmailGeneratorTest {

    @Mock
    private SetAsideJudgementRequestAppSolOneEmailDTOGenerator appSolGenerator;

    @Mock
    private SetAsideJudgementRequestClaimantEmailDTOGenerator claimantGenerator;

    @Mock
    private SetAsideJudgementRequestDefendantEmailDTOGenerator defendantGenerator;

    @Mock
    private SetAsideJudgementRequestRespSolOneEmailDTOGenerator respSolOneGenerator;

    @Mock
    private SetAsideJudgementRequestRespSolTwoEmailDTOGenerator respSolTwoGenerator;

    @InjectMocks
    private SetAsideJudgementRequestAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
