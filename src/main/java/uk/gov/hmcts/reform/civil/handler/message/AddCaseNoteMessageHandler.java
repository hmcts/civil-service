package uk.gov.hmcts.reform.civil.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;

import java.util.HashMap;
import java.util.Map;

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
    public Result handle(CcdEventMessage message) {
        log.info("Handling Add Case Note Message for case {}", message.getCaseId());

        try {
            CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(message.getCaseId()));
            CaseData data = caseDetailsConverter.toCaseData(caseDetails);
            roboticsNotifier.notifyRobotics(data, getSystemUserToken());
        } catch (Exception e) {
            log.error("Failed to trigger robotics for case {}", message.getCaseId());
            Map<String, String> messageProps = new HashMap<>();
            messageProps.put("exceptionMessage", e.getMessage());
            messageProps.put("userId", message.getUserId());

            return new Result.Error(NOTIFY_RPA_ON_CONTINUOUS_FEED.name(), messageProps);
        }

        return new Result.Success();
    }

    private String getSystemUserToken() {
        return userService.getAccessToken(systemUpdateUserConfiguration.getUserName(),
                                          systemUpdateUserConfiguration.getPassword());
    }
}
