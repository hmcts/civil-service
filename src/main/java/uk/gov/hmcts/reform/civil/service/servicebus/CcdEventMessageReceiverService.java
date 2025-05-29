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
import java.util.ArrayList;
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

    public void retryHandleMessage(ExceptionRecordEntity exceptionRecordEntity) {
        Result result = null;

        for (var handler : messageHandlers) {
            if (handler.canHandle(exceptionRecordEntity.getTaskId())) {
                result = handler.handle(exceptionRecordEntity.getReference(), exceptionRecordEntity.getSuccessfulActions());
                break;
            }
        }

        Optional<ExceptionRecordEntity> existingRecord = exceptionRecordRepository.findByIdempotencyKey(exceptionRecordEntity.getIdempotencyKey());
        validateResult(exceptionRecordEntity.getIdempotencyKey(), result, existingRecord);
    }

    private void handleMessage(CcdEventMessage caseEventMessage, String idempotencyKey) {
        Result result = null;

        for (var handler : messageHandlers) {
            if (handler.canHandle(caseEventMessage.getEventId())) {
                result = handler.handle(caseEventMessage.getCaseId(), new ArrayList<>());
                break;
            }
        }

        validateResult(idempotencyKey, result, Optional.empty());
    }

    private void validateResult(String idempotencyKey, Result result, Optional<ExceptionRecordEntity> existingRecord) {

        if (result instanceof Result.Error error) {
            log.info("Handling error for record {} with result {}", existingRecord, result);
            existingRecord.ifPresentOrElse(
                record -> updateExistingExceptionRecord(record, error),
                () -> saveNewExceptionRecord(error, idempotencyKey)
            );
        } else if (existingRecord.isPresent() && result instanceof Result.Success) {
            exceptionRecordRepository.deleteByIdempotencyKey(idempotencyKey);
        }
    }

    private void updateExistingExceptionRecord(ExceptionRecordEntity existingRecord, Result.Error error) {

        log.info("Updating existing record {} with result {}", existingRecord, error);

        int remainingRetries = existingRecord.getRemainingRetries() == 0
            ? 0
            : existingRecord.getRemainingRetries() - 1;

        exceptionRecordRepository.save(
            existingRecord.toBuilder()
                .successfulActions(error.exceptionRecord().successfulActions())
                .updatedOn(OffsetDateTime.now())
                .remainingRetries(remainingRetries)
                .build()
        );
    }

    private void saveNewExceptionRecord(Result.Error error, String idempotencyKey) {
        log.info("Creating new record for key {} with result {}", idempotencyKey, error);

        exceptionRecordRepository.save(
            ExceptionRecordEntity.builder()
                .idempotencyKey(idempotencyKey)
                .reference(error.exceptionRecord().caseReference())
                .taskId(error.exceptionRecord().taskId())
                .successfulActions(error.exceptionRecord().successfulActions())
                .createdAt(OffsetDateTime.now())
                .updatedOn(OffsetDateTime.now())
                .remainingRetries(3)
                .build()
        );
    }
}
