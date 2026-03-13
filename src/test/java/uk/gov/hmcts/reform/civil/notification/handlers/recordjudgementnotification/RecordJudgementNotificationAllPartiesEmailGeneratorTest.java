package uk.gov.hmcts.reform.civil.notification.handlers.recordjudgementnotification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RecordJudgementNotificationAllPartiesEmailGeneratorTest {

    @Mock
    private RecordJudgementNotificationAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;

    @Mock
    private RecordJudgementNotificationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Mock
    private RecordJudgementNotificationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    @Mock
    private RecordJudgementNotificationClaimantEmailDTOGenerator claimantEmailDTOGenerator;

    @Mock
    private RecordJudgementNotificationDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @InjectMocks
    private RecordJudgementNotificationAllPartiesEmailGenerator generator;

    @Test
    void shouldExtendAllPartiesEmailGenerator() {
        assertThat(generator).isInstanceOf(AllPartiesEmailGenerator.class);
    }
}
