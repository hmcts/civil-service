package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GenerateOrderCOOAllPartiesEmailGeneratorTest {

    private GenerateOrderCOOAppSolEmailDTOGenerator appSolEmailDTOGenerator;
    private GenerateOrderCOOResp1EmailDTOGenerator resp1EmailDTOGenerator;
    private GenerateOrderCOOResp2EmailDTOGenerator resp2EmailDTOGenerator;
    private GenerateOrderCOOClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private GenerateOrderCOODefendantEmailDTOGenerator defendantEmailDTOGenerator;

    private static final String TASK_INFO = "GenerateOrderNotifyPartiesCourtOfficerOrder";

    @BeforeEach
    void setup() {
        appSolEmailDTOGenerator = mock(GenerateOrderCOOAppSolEmailDTOGenerator.class);
        resp1EmailDTOGenerator = mock(GenerateOrderCOOResp1EmailDTOGenerator.class);
        resp2EmailDTOGenerator = mock(GenerateOrderCOOResp2EmailDTOGenerator.class);
        claimantEmailDTOGenerator = mock(GenerateOrderCOOClaimantEmailDTOGenerator.class);
        defendantEmailDTOGenerator = mock(GenerateOrderCOODefendantEmailDTOGenerator.class);
    }

    @Test
    void shouldSetTaskInfoOnClaimantAndDefendantGenerators() {
        new GenerateOrderCOOAllPartiesEmailGenerator(
            appSolEmailDTOGenerator,
            resp1EmailDTOGenerator,
            resp2EmailDTOGenerator,
            claimantEmailDTOGenerator,
            defendantEmailDTOGenerator,
            TASK_INFO
        );

        verify(claimantEmailDTOGenerator).setTaskInfo(TASK_INFO);
        verify(defendantEmailDTOGenerator).setTaskInfo(TASK_INFO);
    }
}
