package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotifyRpaFeedCaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
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

    @Test
    void shouldNotifyRobotics_whenEventIsNotDjSpecOrUnspec() {
        NotifyRpaFeedCaseReference caseReference = new NotifyRpaFeedCaseReference();
        caseReference.setCaseReference("1234567890123456");
        caseReference.setNotifyEventId("OTHER_EVENT");
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
    void shouldNotifyDefaultJudgmentRobotics_whenEventIsDjSpec() {
        NotifyRpaFeedCaseReference caseReference = new NotifyRpaFeedCaseReference();
        caseReference.setCaseReference("1234567890123456");
        caseReference.setNotifyEventId("NOTIFY_RPA_DJ_SPEC");
        String accessToken = "accessToken";
        String userName = "userName";
        String password = "password";

        given(userConfig.getUserName()).willReturn(userName);
        given(userConfig.getPassword()).willReturn(password);
        given(userService.getAccessToken(userName, password)).willReturn(accessToken);

        CaseData caseData = CaseData.builder().build();
        notifyRpaFeedTask.migrateCaseData(caseData, caseReference);

        verify(defaultJudgmentRoboticsNotifier).sendNotifications(caseData, MultiPartyScenario.isMultiPartyScenario(caseData), accessToken);
        verifyNoInteractions(roboticsNotifier);
    }

    @Test
    void shouldNotifyDefaultJudgmentRobotics_whenEventIsDjUnspec() {
        NotifyRpaFeedCaseReference caseReference = new NotifyRpaFeedCaseReference();
        caseReference.setCaseReference("1234567890123456");
        caseReference.setNotifyEventId("NOTIFY_RPA_DJ_UNSPEC");
        String accessToken = "accessToken";
        String userName = "userName";
        String password = "password";

        given(userConfig.getUserName()).willReturn(userName);
        given(userConfig.getPassword()).willReturn(password);
        given(userService.getAccessToken(userName, password)).willReturn(accessToken);

        CaseData caseData = CaseData.builder().build();
        notifyRpaFeedTask.migrateCaseData(caseData, caseReference);

        verify(defaultJudgmentRoboticsNotifier).sendNotifications(caseData, MultiPartyScenario.isMultiPartyScenario(caseData), accessToken);
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
