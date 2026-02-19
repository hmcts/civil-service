package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifyRpaFeedTaskTest {

    @Mock
    private RoboticsNotifier roboticsNotifier;
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private UserService userService;

    private NotifyRpaFeedTask notifyRpaFeedTask;

    @BeforeEach
    void setUp() {
        notifyRpaFeedTask = new NotifyRpaFeedTask(roboticsNotifier, userConfig, userService);
    }

    @Test
    void shouldNotifyRobotics() {
        String accessToken = "accessToken";
        String userName = "userName";
        String password = "password";

        given(userConfig.getUserName()).willReturn(userName);
        given(userConfig.getPassword()).willReturn(password);
        given(userService.getAccessToken(userName, password)).willReturn(accessToken);

        CaseData caseData = CaseData.builder().build();
        notifyRpaFeedTask.migrateCaseData(caseData, new CaseReference("1234567890123456"));

        verify(roboticsNotifier).notifyRobotics(caseData, accessToken);
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
