package uk.gov.hmcts.reform.civil.service.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.message.CcdEventMessageHandler;
import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.dashboard.repositories.ExceptionRecordRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CcdEventMessageReceiverService {

    private static final String CCD_SERVICE_BUS_TASK = "ccd service bus task";
    private final ObjectMapper objectMapper;
    private final List<CcdEventMessageHandler> messageHandlers;
    private final CaseTaskTrackingService caseTaskTrackingService;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final ExceptionRecordRepository exceptionRecordRepository;

    public void handleCcdCaseEventAsbMessage(String messageId,
                                             String sessionId,
                                             String message) throws JsonProcessingException {
        log.info("Received CCD Case Events ASB message with id '{}' and session {}", messageId, sessionId);
        CcdEventMessage caseEventMessage = objectMapper.readValue(message, CcdEventMessage.class);
        String idempotencyKey = idempotencyKeyGenerator.generateIdempotencyKey(messageId, caseEventMessage.getEventId());
        handleMessage(caseEventMessage, messageId);
    }

    private void handleMessage(CcdEventMessage caseEventMessage,
                               String messageId) {
        Result result = null;

        for (var handler : messageHandlers) {
            if (handler.canHandle(caseEventMessage.getEventId())) {
                result = handler.handle(caseEventMessage);
                break;
            }
        }

        if (result instanceof Result.Error error) {
            caseTaskTrackingService.trackCaseTask(caseEventMessage.getCaseId(),
                                                  CCD_SERVICE_BUS_TASK,
                                                  error.eventName(),
                                                  error.messageProps());
        }
    }

}
