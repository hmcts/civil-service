package uk.gov.hmcts.reform.civil.service.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.message.CcdEventMessageHandler;
import uk.gov.hmcts.reform.civil.model.ExceptionRecord;
import uk.gov.hmcts.reform.civil.model.Result;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.dashboard.entities.ExceptionRecordEntity;
import uk.gov.hmcts.reform.dashboard.repositories.ExceptionRecordRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdEventMessageReceiverServiceTest {

    @Mock
    private CcdEventMessageHandler messageHandler1;

    @Mock
    private CcdEventMessageHandler messageHandler2;

    @Mock
    private IdempotencyKeyGenerator idempotencyKeyGenerator;

    @Mock
    private ExceptionRecordRepository exceptionRecordRepository;

    private CcdEventMessageReceiverService ccdEventMessageReceiverService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void setUp() {
        ccdEventMessageReceiverService = new CcdEventMessageReceiverService(
            objectMapper,
            Arrays.asList(messageHandler1, messageHandler2),
            idempotencyKeyGenerator,
            exceptionRecordRepository
        );
    }

    @SneakyThrows
    @Test
    public void messageHandler2ShouldHandleMessage() {
        String caseEvent = "TEST_CASE_EVENT";
        when(messageHandler1.canHandle(caseEvent)).thenReturn(false);
        when(messageHandler2.canHandle(caseEvent)).thenReturn(true);

        String message = "{\"JurisdictionId\":\"civil\",\"CaseTypeId\":\"CIVIL\",\"EventId\":\"TEST_CASE_EVENT\"}";

        ccdEventMessageReceiverService.handleCcdCaseEventAsbMessage("1", "1", message);

        verify(messageHandler2, times(1)).handle(any());
        verify(messageHandler1, times(0)).handle(any());

        verifyNoInteractions(exceptionRecordRepository.save(any()));
    }

    @SneakyThrows
    @Test
    public void messageHandler1ShouldReturnErrorAndSaveToDb() {
        String caseEvent = "TEST_CASE_EVENT";

        Result.Error error = new Result.Error(new ExceptionRecord(caseEvent, "1234567890", List.of("email1","robotics")));
        when(messageHandler1.canHandle(caseEvent)).thenReturn(true);
        when(messageHandler1.handle(any())).thenReturn(error);

        String message = "{\"JurisdictionId\":\"civil\",\"CaseTypeId\":\"CIVIL\",\"caseId\":\"1234567890\",\"EventId\":\"TEST_CASE_EVENT\"}";

        ccdEventMessageReceiverService.handleCcdCaseEventAsbMessage("1", "1", message);

        verify(messageHandler1, times(1)).handle(any());
        verify(messageHandler2, times(0)).handle(any());
        verify(exceptionRecordRepository).save(any());
    }

    @SneakyThrows
    @Test
    public void messageHandler1ShouldReturnSuccessAfterRetryAndDeleteFromDb() {
        String caseEvent = "TEST_CASE_EVENT";
        final String idempotencyKey = "idempotencyKey";

        when(messageHandler1.canHandle(caseEvent)).thenReturn(true);
        when(messageHandler1.handle(any())).thenReturn(new Result.Success());
        when(idempotencyKeyGenerator.generateIdempotencyKey(any(), any())).thenReturn(idempotencyKey);
        when(exceptionRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(
            Optional.of(ExceptionRecordEntity.builder()
                            .reference("1234567890")
                            .successfulActions(List.of(""))
                            .build())
        );

        String message = "{\"JurisdictionId\":\"civil\",\"CaseTypeId\":\"CIVIL\",\"caseId\":\"1234567890\",\"EventId\":\"TEST_CASE_EVENT\"}";

        ccdEventMessageReceiverService.handleCcdCaseEventAsbMessage("1", "1", message);

        verify(messageHandler1, times(1)).handle(any());
        verify(messageHandler2, times(0)).handle(any());
        verify(exceptionRecordRepository).deleteByIdempotencyKey(idempotencyKey);
    }

}
