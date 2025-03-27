package uk.gov.hmcts.reform.civil.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExceptionRecord;
import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddCaseNoteMessageHandler implements CcdEventMessageHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final RoboticsNotifier roboticsNotifier;
    private final CaseDetailsConverter caseDetailsConverter;
    private final UserService userService;
    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @Override
    public boolean canHandle(String caseEvent) {
        return ADD_CASE_NOTE.name().equals(caseEvent);
    }

    @Override
    public Result handle(String caseId, List<String> actions) {
        log.info("Handling Add Case Note Message for case {}", caseId);

        try {
            var response = coreCaseDataService.startUpdate(caseId, NOTIFY_RPA_ON_CONTINUOUS_FEED);
            CaseData data = caseDetailsConverter.toCaseData(response.getCaseDetails());
            roboticsNotifier.notifyRobotics(data, getSystemUserToken());
        } catch (Exception e) {
            log.error("Failed to notify robotics for case {}", caseId);
            return new Result.Error(new ExceptionRecord(ADD_CASE_NOTE.name(), caseId));
        }

        return new Result.Success();
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(systemUpdateUserConfiguration.getUserName(),
                                          systemUpdateUserConfiguration.getPassword());
    }
}
