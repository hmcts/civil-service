package uk.gov.hmcts.reform.civil.handler.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;

@ExtendWith(MockitoExtension.class)
class AddCaseNoteMessageHandlerTest {

    private static final String CASE_ID = "1234567891234567";
    public static final String ACCESS_TOKEN = "AccessToken";

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private RoboticsNotifier roboticsNotifier;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private UserService userService;
    @Mock
    private SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @InjectMocks
    private AddCaseNoteMessageHandler addCaseNoteMessageHandler;

    @Test
    public void shouldHandleCorrectEvent() {
        assertThat(addCaseNoteMessageHandler.canHandle(ADD_CASE_NOTE.name())).isTrue();
    }

    @Test
    public void shouldNotHandleOtherEvents() {
        assertThat(addCaseNoteMessageHandler.canHandle(NOTIFY_RPA_ON_CONTINUOUS_FEED.name())).isFalse();
    }

    @Test
    public void shouldPublishEventWhenReceivingHandleableMessage() {
        CaseDetails details = CaseDetails.builder().id(Long.parseLong(CASE_ID)).build();
        CaseData data = CaseData.builder().applicant1(Party.builder().individualFirstName("Sam").build()).build();
        when(coreCaseDataService.startUpdate(CASE_ID, NOTIFY_RPA_ON_CONTINUOUS_FEED)).thenReturn(startEventResponse(details));
        when(caseDetailsConverter.toCaseData(eq(details))).thenReturn(data);
        when(userService.getAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);

        Result result = addCaseNoteMessageHandler.handle(CASE_ID, emptyList());

        verify(roboticsNotifier).notifyRobotics(data, ACCESS_TOKEN);
        assertThat(result).isInstanceOf(Result.Success.class);
    }

    @Test
    public void shouldPublishCustomEventObjectToAppInsights_onFailure() {
        CaseDetails details = CaseDetails.builder().id(Long.parseLong(CASE_ID)).build();
        CaseData data = CaseData.builder().applicant1(Party.builder().individualFirstName("Sam").build()).build();
        when(coreCaseDataService.startUpdate(CASE_ID, NOTIFY_RPA_ON_CONTINUOUS_FEED)).thenReturn(startEventResponse(details));
        when(caseDetailsConverter.toCaseData(eq(details))).thenReturn(data);
        when(userService.getAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);

        doThrow(new NotificationException(new RuntimeException("error")))
            .when(roboticsNotifier).notifyRobotics(data, ACCESS_TOKEN);

        Result result = addCaseNoteMessageHandler.handle(CASE_ID, emptyList());

        assertThat(result).isInstanceOf(Result.Error.class);
        Result.Error error = (Result.Error) result;
        assertThat(error.exceptionRecord().taskId()).isEqualTo(ADD_CASE_NOTE.name());
        assertThat(error.exceptionRecord().successfulActions()).isEmpty();
    }

    private StartEventResponse startEventResponse(CaseDetails caseDetails) {
        return StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();
    }

}
