package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.RespondentResponseDeadlineCallbackHandler.TASK_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondentResponseDeadlineCallbackHandler.class,
    DeadlinesCalculator.class,
    JacksonAutoConfiguration.class
})
public class RespondentResponseDeadlineCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondentResponseDeadlineCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @Test
    void shouldUpdateRespondent1ResponseDeadlineTo28days_whenClaimIssueTimeIsBefore4pm() {

        LocalDateTime localDateTime = LocalDateTime.of(2023, 10, 30, 12, 0, 0);
        when(deadlinesCalculator.plus28DaysAt4pmDeadline(any())).thenReturn(localDateTime);

        CaseData caseData = CaseDataBuilder.builder()
            .issueDate(LocalDate.of(2023, 10, 2))
            .respondent1ResponseDeadline(LocalDateTime.of(2023, 10, 2, 12, 0, 0))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(localDateTime);

    }

    @Test
    void shouldUpdateRespondent1ResponseDeadlineTo28days_whenClaimIssueTimeIsAfter4pm() {

        LocalDateTime localDateTime = LocalDateTime.of(2023, 10, 31, 17, 0, 0);
        when(deadlinesCalculator.plus28DaysAt4pmDeadline(any())).thenReturn(localDateTime);

        CaseData caseData = CaseDataBuilder.builder()
            .issueDate(LocalDate.of(2023, 10, 2))
            .respondent1ResponseDeadline(LocalDateTime.of(2023, 10, 2, 17, 0, 0))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertThat(updatedData.getRespondent1ResponseDeadline()).isEqualTo(localDateTime);
    }

    @Test
    void shouldUpdateRespondent1ResponseDeadlineToNull_whenClaimIssueDateIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .issueDate(null)
            .respondent1ResponseDeadline(null)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        // Then
        assertNull(updatedData.getRespondent1ResponseDeadline());
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "SET_LIP_RESPONDENT_RESPONSE_DEADLINE").build()).build())).isEqualTo(TASK_ID);
    }
}
