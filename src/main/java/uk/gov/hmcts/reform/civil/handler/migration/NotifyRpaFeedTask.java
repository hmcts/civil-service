package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.NotifyRpaFeedCaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.DefaultJudgmentRoboticsNotifier;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

@Slf4j
@Component
public class NotifyRpaFeedTask extends MigrationTask<NotifyRpaFeedCaseReference> {

    private final RoboticsNotifier roboticsNotifier;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;
    private final DefaultJudgmentRoboticsNotifier defaultJudgmentRoboticsNotifier;

    public NotifyRpaFeedTask(RoboticsNotifier roboticsNotifier,
                             SystemUpdateUserConfiguration userConfig,
                             UserService userService,
                             DefaultJudgmentRoboticsNotifier defaultJudgmentRoboticsNotifier) {
        super(NotifyRpaFeedCaseReference.class);
        this.roboticsNotifier = roboticsNotifier;
        this.userConfig = userConfig;
        this.userService = userService;
        this.defaultJudgmentRoboticsNotifier = defaultJudgmentRoboticsNotifier;
    }

    @Override
    protected String getEventSummary() {
        return "NotifyRpaFeed via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, NotifyRpaFeedCaseReference obj) {
        final String accessToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        if ("NOTIFY_RPA_DJ_SPEC".equals(obj.getNotifyEventId()) || "NOTIFY_RPA_DJ_UNSPEC".equals(obj.getNotifyEventId())) {
            defaultJudgmentRoboticsNotifier.sendNotifications(caseData, MultiPartyScenario.isMultiPartyScenario(caseData), accessToken);
            log.info("Notified to RPA via migration task for caseId {} for event type {}", obj.getCaseReference(),
                     obj.getNotifyEventId());
        } else {
            roboticsNotifier.notifyRobotics(caseData, accessToken);
            log.info("Notified to RPA via migration task for caseId {}", obj.getCaseReference());
        }
        return caseData;
    }

    @Override
    protected String getEventDescription() {
        return "This task notifies to caseman via migration task";
    }

    @Override
    protected String getTaskName() {
        return "NotifyRpaFeedTask";
    }
}
