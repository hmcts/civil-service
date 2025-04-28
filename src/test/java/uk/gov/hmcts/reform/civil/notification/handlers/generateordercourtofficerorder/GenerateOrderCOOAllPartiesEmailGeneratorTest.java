package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class GenerateOrderCOOAllPartiesEmailGeneratorTest {

    @Mock
    private GenerateOrderCOOAppSolEmailDTOGenerator appSolEmailDTOGenerator;
    @Mock
    private GenerateOrderCOOResp1EmailDTOGenerator resp1EmailDTOGenerator;
    @Mock
    private GenerateOrderCOOResp2EmailDTOGenerator resp2EmailDTOGenerator;
    @Mock
    private GenerateOrderCOOClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    @Mock
    private GenerateOrderCOODefendantEmailDTOGenerator defendantEmailDTOGenerator;

    private static final String TASK_INFO = "test-task-info";

    private GenerateOrderCOOAllPartiesEmailGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new GenerateOrderCOOAllPartiesEmailGenerator(
            appSolEmailDTOGenerator,
            resp1EmailDTOGenerator,
            resp2EmailDTOGenerator,
            claimantEmailDTOGenerator,
            defendantEmailDTOGenerator
        );
    }

    @Test
    void shouldSetTaskInfoOnClaimantAndDefendantGenerators_whenInitCalled() {
        generator.setTaskInfo(TASK_INFO);
        generator.init();

        verify(claimantEmailDTOGenerator).setTaskInfo(TASK_INFO);
        verify(defendantEmailDTOGenerator).setTaskInfo(TASK_INFO);
    }

    @Test
    void shouldNotSetTaskInfo_whenTaskInfoIsNull() {
        generator.setTaskInfo(null);
        generator.init();

        verify(claimantEmailDTOGenerator, never()).setTaskInfo(null);
        verify(defendantEmailDTOGenerator, never()).setTaskInfo(null);
    }
}
