package uk.gov.hmcts.reform.civil.service.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseEventMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseEventMessageMapper {

    private final ObjectMapper objectMapper;

    public CaseEventMessage mapToCaseEventMessage(CaseEventMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CaseEventMessage(
            entity.getMessageId(),
            entity.getSequence(),
            entity.getCaseId(),
            entity.getEventTimestamp(),
            entity.getFromDlq(),
            entity.getState(),
            entity.getMessageProperties(),
            entity.getMessageContent(),
            entity.getReceived(),
            entity.getDeliveryCount(),
            entity.getHoldUntil(),
            entity.getRetryCount());
    }

    @SuppressWarnings("PMD.ConfusingTernary")
    private String getCaseTypeId(CaseEventMessageEntity entity) {
        String caseTypeId = null;
        if (entity.getMessageContent() != null && !entity.getMessageContent().isBlank()) {
            try {
                JsonNode jsonNodeMessageContent = objectMapper.readTree(entity.getMessageContent());
                JsonNode jsonNodeCaseTypeId = jsonNodeMessageContent.get("CaseTypeId");
                caseTypeId = jsonNodeCaseTypeId.asText();
            } catch (JsonProcessingException jsonProcessingException) {
                log.info("Error extracting CaseTypeId from message", jsonProcessingException);
            }
        } else {
            log.warn("messageContent is null or empty for messageId: {}", entity.getMessageId());
        }
        return caseTypeId;
    }

//    public ProblemMessage mapToProblemMessage(CaseEventMessageEntity entity) {
//        if (entity == null) {
//            return null;
//        }
//
//        String caseTypeId = getCaseTypeId(entity);
//
//        return new ProblemMessage(
//            entity.getMessageId(),
//            entity.getCaseId(),
//            caseTypeId,
//            entity.getEventTimestamp(),
//            entity.getFromDlq(),
//            entity.getState());
//    }
}
