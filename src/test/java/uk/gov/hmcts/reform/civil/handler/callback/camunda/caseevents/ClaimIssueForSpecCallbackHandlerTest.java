package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.time.LocalTime.MIDNIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCESS_CLAIM_ISSUE_SPEC;

@ExtendWith(MockitoExtension.class)
class ClaimIssueForSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    private ClaimIssueForSpecCallbackHandler handler;

    private ObjectMapper mapper;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    private final LocalDateTime deadline = now().atTime(MIDNIGHT);

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        handler = new ClaimIssueForSpecCallbackHandler(mapper, deadlinesCalculator);
    }

    @Test
    void shouldAddClaimNotificationDeadline_whenClaimIsIssued() {
        // Given
        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertThat(updatedData.getClaimNotificationDeadline()).isEqualTo(deadline);
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("IssueClaimForSpec");
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(PROCESS_CLAIM_ISSUE_SPEC);
    }
}
