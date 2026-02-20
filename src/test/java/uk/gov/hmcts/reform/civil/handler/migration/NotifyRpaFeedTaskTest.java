package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotifyRpaFeedCaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.DefaultJudgmentRoboticsNotifier;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotifyRpaFeedTaskTest {

    @Mock
    private RoboticsNotifier roboticsNotifier;
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private UserService userService;
    @Mock
    private DefaultJudgmentRoboticsNotifier defaultJudgmentRoboticsNotifier;

    private NotifyRpaFeedTask notifyRpaFeedTask;

    @BeforeEach
    void setUp() {
        notifyRpaFeedTask = new NotifyRpaFeedTask(roboticsNotifier, userConfig, userService, defaultJudgmentRoboticsNotifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOTIFY_RPA_ON_CONTINUOUS_FEED", "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE"})
    void shouldNotifyRobotics_whenEventIsNotDjSpecOrUnspec(String notifyEventId) {
        NotifyRpaFeedCaseReference caseReference = new NotifyRpaFeedCaseReference();
        caseReference.setCaseReference("1234567890123456");
        caseReference.setNotifyEventId(notifyEventId);
        String accessToken = "accessToken";
        String userName = "userName";
        String password = "password";

        given(userConfig.getUserName()).willReturn(userName);
        given(userConfig.getPassword()).willReturn(password);
        given(userService.getAccessToken(userName, password)).willReturn(accessToken);

        CaseData caseData = CaseData.builder().build();
        notifyRpaFeedTask.migrateCaseData(caseData, caseReference);

        verify(roboticsNotifier).notifyRobotics(caseData, accessToken);
        verifyNoInteractions(defaultJudgmentRoboticsNotifier);
    }

    @Test
    void shouldNotNotifyRobotics_whenEventIsNotUnknown() {
        NotifyRpaFeedCaseReference caseReference = new NotifyRpaFeedCaseReference();
        caseReference.setCaseReference("1234567890123456");
        caseReference.setNotifyEventId("UNKNOWN");
        String accessToken = "accessToken";
        String userName = "userName";
        String password = "password";

        given(userConfig.getUserName()).willReturn(userName);
        given(userConfig.getPassword()).willReturn(password);
        given(userService.getAccessToken(userName, password)).willReturn(accessToken);

        CaseData caseData = CaseData.builder().build();
        notifyRpaFeedTask.migrateCaseData(caseData, caseReference);

        verifyNoInteractions(roboticsNotifier);
        verifyNoInteractions(defaultJudgmentRoboticsNotifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOTIFY_RPA_DJ_SPEC", "NOTIFY_RPA_DJ_UNSPEC"})
    void shouldNotifyDefaultJudgmentRobotics_whenEventIsDjSpec(String notifyEventId) {
        NotifyRpaFeedCaseReference caseReference = new NotifyRpaFeedCaseReference();
        caseReference.setCaseReference("1234567890123456");
        caseReference.setNotifyEventId(notifyEventId);
        String accessToken = "accessToken";
        String userName = "userName";
        String password = "password";

        given(userConfig.getUserName()).willReturn(userName);
        given(userConfig.getPassword()).willReturn(password);
        given(userService.getAccessToken(userName, password)).willReturn(accessToken);

        CaseData caseData = CaseData.builder().build();
        notifyRpaFeedTask.migrateCaseData(caseData, caseReference);

        verify(defaultJudgmentRoboticsNotifier).notifyRobotics(caseData, accessToken);
        verifyNoInteractions(roboticsNotifier);
    }

    @Test
    void shouldReturnCorrectEventSummary() {
        assertThat(notifyRpaFeedTask.getEventSummary()).isEqualTo("NotifyRpaFeed via migration task");
    }

    @Test
    void shouldReturnCorrectEventDescription() {
        assertThat(notifyRpaFeedTask.getEventDescription()).isEqualTo("This task notifies to caseman via migration task");
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(notifyRpaFeedTask.getTaskName()).isEqualTo("NotifyRpaFeedTask");
    }
}
