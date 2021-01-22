package uk.gov.hmcts.reform.unspec.handler.callback.camunda.robotics;

import com.networknt.schema.ValidationMessage;
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
import uk.gov.hmcts.reform.unspec.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.unspec.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.unspec.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    NotifyRoboticsOnCaseHandedOfflineHandler.class,
    JsonSchemaValidationService.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class
})
class NotifyRoboticsOnCaseHandedOfflineHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    RoboticsNotificationService roboticsNotificationService;

    @Nested
    class ValidJsonPayload {

        @Autowired
        private NotifyRoboticsOnCaseHandedOfflineHandler handler;

        @Test
        void shouldNotifyRobotics_whenNoSchemaErrors() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(roboticsNotificationService).notifyRobotics(caseData);
        }
    }

    @Nested
    class InValidJsonPayload {

        @MockBean
        private JsonSchemaValidationService validationService;
        @Autowired
        private NotifyRoboticsOnCaseHandedOfflineHandler handler;

        @Test
        void shouldThrowJsonSchemaValidationException_whenSchemaErrors() {
            when(validationService.validate(anyString())).thenReturn(Set.of(new ValidationMessage.Builder().build()));
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThrows(
                JsonSchemaValidationException.class,
                () -> handler.handle(params)
            );
            verifyNoInteractions(roboticsNotificationService);
        }

    }
}
