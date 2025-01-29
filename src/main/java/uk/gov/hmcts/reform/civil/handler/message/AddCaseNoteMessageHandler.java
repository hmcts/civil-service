package uk.gov.hmcts.reform.civil.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddCaseNoteMessageHandler implements CcdEventMessageHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseTaskTrackingService caseTaskTrackingService;

    @Override
    public boolean canHandle(String caseEvent) {
        return ADD_CASE_NOTE.name().equals(caseEvent);
    }

    @Override
    public void handle(CcdEventMessage message) {
        log.info("Handling Add Case Note Message for case {}", message.getCaseId());

        try {
            coreCaseDataService.triggerEvent(Long.parseLong(message.getCaseId()), NOTIFY_RPA_ON_CONTINUOUS_FEED);
        } catch (Exception e) {
            log.error("Failed to trigger robotics for case {}", message.getCaseId());
            Map<String, String> messageProps = new HashMap<>();
            messageProps.put("exceptionMessage", e.getMessage());
            messageProps.put("userId", message.getUserId());
            caseTaskTrackingService.trackCaseTask(message.getCaseId(),
                                                  "service bus message",
                                                  NOTIFY_RPA_ON_CONTINUOUS_FEED.name(),
                                                  messageProps);
        }
    }
}
