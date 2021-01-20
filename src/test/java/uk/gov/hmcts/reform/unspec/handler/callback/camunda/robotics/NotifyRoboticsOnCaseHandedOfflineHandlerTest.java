package uk.gov.hmcts.reform.unspec.handler.callback.camunda.robotics;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.robotics.RoboticsNotificationService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {NotifyRoboticsOnCaseHandedOfflineHandler.class})
class NotifyRoboticsOnCaseHandedOfflineHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private RoboticsNotificationService roboticsNotificationService;

    @Autowired
    private NotifyRoboticsOnCaseHandedOfflineHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRobotics_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(roboticsNotificationService).notifyRobotics(caseData);
        }
    }
}
