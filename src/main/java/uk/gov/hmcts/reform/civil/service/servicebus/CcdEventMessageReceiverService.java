package uk.gov.hmcts.reform.civil.service.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.message.CcdEventMessageHandler;
import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;
import uk.gov.hmcts.reform.dashboard.entities.ExceptionRecordEntity;
import uk.gov.hmcts.reform.dashboard.repositories.ExceptionRecordRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CcdEventMessageReceiverService {

    private final ObjectMapper objectMapper;
    private final List<CcdEventMessageHandler> messageHandlers;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final ExceptionRecordRepository exceptionRecordRepository;

    public void handleCcdCaseEventAsbMessage(String messageId,
                                             String sessionId,
                                             String message) throws JsonProcessingException {
        log.info("Received CCD Case Events ASB message with id '{}' and session {}", messageId, sessionId);
        CcdEventMessage caseEventMessage = objectMapper.readValue(message, CcdEventMessage.class);
        String idempotencyKey = idempotencyKeyGenerator.generateIdempotencyKey(messageId, caseEventMessage.getEventId());
        handleMessage(caseEventMessage, idempotencyKey);
    }

    private void handleMessage(CcdEventMessage caseEventMessage,
                               String idempotencyKey) {
        Result result = null;

        for (var handler : messageHandlers) {
            if (handler.canHandle(caseEventMessage.getEventId())) {
                result = handler.handle(caseEventMessage);
                break;
            }
        }

        Optional<ExceptionRecordEntity> existingRecord = exceptionRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (result instanceof Result.Error error) {
            existingRecord.ifPresentOrElse(
                record -> updateExistingExceptionRecord(record, error),
                () -> saveNewExceptionRecord(error, idempotencyKey)
            );
        } else if (existingRecord.isPresent() && result instanceof Result.Success) {
            exceptionRecordRepository.deleteByIdempotencyKey(idempotencyKey);
        }
    }

    private void updateExistingExceptionRecord(ExceptionRecordEntity existingRecord,
                                                                Result.Error error) {

        if (existingRecord.getSuccessfulActions().size() == error.exceptionRecord().successfulActions().size()
            && existingRecord.getSuccessfulActions().containsAll(error.exceptionRecord().successfulActions())
        && error.exceptionRecord().successfulActions().containsAll(existingRecord.getSuccessfulActions())) {
            return;
        }

        exceptionRecordRepository.save(
            existingRecord.toBuilder()
                .successfulActions(error.exceptionRecord().successfulActions())
                .updatedOn(OffsetDateTime.now())
                .build()
        );
    }

    private void saveNewExceptionRecord(Result.Error error,
                                        String idempotencyKey) {
        exceptionRecordRepository.save(
            ExceptionRecordEntity.builder()
                .idempotencyKey(idempotencyKey)
                .reference(error.exceptionRecord().caseReference())
                .taskId(error.exceptionRecord().taskId())
                .successfulActions(error.exceptionRecord().successfulActions())
                .createdAt(OffsetDateTime.now())
                .updatedOn(OffsetDateTime.now())
                .build()
        );
    }

}
