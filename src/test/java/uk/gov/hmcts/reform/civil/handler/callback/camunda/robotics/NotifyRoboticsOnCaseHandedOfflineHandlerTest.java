package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;

import java.time.LocalDateTime;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    NotifyRoboticsOnCaseHandedOfflineHandler.class,
    JsonSchemaValidationService.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
    EventHistoryMapper.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    OrganisationService.class
})
@ExtendWith(SpringExtension.class)
class NotifyRoboticsOnCaseHandedOfflineHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @MockBean
    private RoboticsNotificationService roboticsNotificationService;

    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    IdamClient idamClient;
    @MockBean
    PrdAdminUserConfiguration userConfig;

    @Nested
    class ValidJsonPayload {

        private final LocalDateTime takenOfflineDate = now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(takenOfflineDate);
        }

        @Autowired
        private NotifyRoboticsOnCaseHandedOfflineHandler handler;

        @Test
        void shouldNotifyRobotics_whenNoSchemaErrors() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
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
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThrows(
                JsonSchemaValidationException.class,
                () -> handler.handle(params)
            );
            verifyNoInteractions(roboticsNotificationService);
        }

    }
}
