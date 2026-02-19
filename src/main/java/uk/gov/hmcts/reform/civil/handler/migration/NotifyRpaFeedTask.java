package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

@Slf4j
@Component
public class NotifyRpaFeedTask extends MigrationTask<CaseReference> {

    private final RoboticsNotifier roboticsNotifier;
    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;

    public NotifyRpaFeedTask(RoboticsNotifier roboticsNotifier,
                             SystemUpdateUserConfiguration userConfig,
                             UserService userService) {
        super(CaseReference.class);
        this.roboticsNotifier = roboticsNotifier;
        this.userConfig = userConfig;
        this.userService = userService;
    }

    @Override
    protected String getEventSummary() {
        return "NotifyRpaFeed via migration task";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference obj) {
        final String accessToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        roboticsNotifier.notifyRobotics(caseData, accessToken);
        log.info("Notified to RPA via migration task for caseId {}", obj.getCaseReference());
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
