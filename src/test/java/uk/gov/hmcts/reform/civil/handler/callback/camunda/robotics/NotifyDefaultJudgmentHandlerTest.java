package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.notification.robotics.DefaultJudgmentRoboticsNotifier;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotifyDefaultJudgmentHandlerTest extends BaseCallbackHandlerTest {

    private static final String TOKEN = "1";

    @Mock
    private DefaultJudgmentRoboticsNotifier defaultJudgmentRoboticsNotifier;

    @InjectMocks
    private NotifyDefaultJudgmentHandler handler;

    @Test
    void whenAboutToSubmitCallbackInvoked_thenCallDefaultJudgementRoboticsNotifier() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParams.builder().caseData(caseData)
            .type(CallbackType.ABOUT_TO_SUBMIT).params(Map.of(
            CallbackParams.Params.BEARER_TOKEN, TOKEN)).build();

        handler.handle(params);

        verify(defaultJudgmentRoboticsNotifier).notifyRobotics(caseData, TOKEN);
    }
}
