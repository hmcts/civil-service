package uk.gov.hmcts.reform.civil.service.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.message.CcdEventMessageHandler;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;
import uk.gov.hmcts.reform.civil.model.message.CcdServiceBusMessage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CcdEventMessageReceiverService {

    protected static final String MESSAGE_PROPERTIES = "MessageProperties";

    private final ObjectMapper objectMapper;
    private final List<CcdEventMessageHandler> messageHandlers;

    public void handleAsbMessage(String messageId,
                                 String sessionId,
                                 String message) throws JsonProcessingException {
        log.info("Received ASB message with id '{}'", messageId);
        handleMessage(messageId, sessionId, message, false);
    }

    public void handleCcdCaseEventAsbMessage(String messageId,
                                             String sessionId,
                                             String message) throws JsonProcessingException {
        log.debug("Received CCD Case Events ASB message with id '{}'", messageId);
        CcdServiceBusMessage serviceBusMessage = objectMapper.readValue(message, CcdServiceBusMessage.class);
        handleMessage(messageId, sessionId, message, false);
    }

    private void handleMessage(String messageId,
                               String sessionId,
                               String message,
                               Boolean fromDlq) throws JsonProcessingException {
        CcdEventMessage caseEventMessage = objectMapper.readValue(message, CcdEventMessage.class);

        for (var handler : messageHandlers) {
            if (handler.canHandle(caseEventMessage.getEventId())) {
                handler.handle(caseEventMessage);
                break;
            }
        }
    }

}
