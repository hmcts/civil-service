package uk.gov.hmcts.reform.unspec.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.time.LocalTime.MIDNIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ClaimIssueCallbackHandler.class,
    JacksonAutoConfiguration.class,
    DeadlinesCalculator.class
})
class ClaimIssueCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @Autowired
    private ClaimIssueCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final LocalDateTime deadline = now().atTime(MIDNIGHT);

    @BeforeEach
    void setup() {
        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
    }

    @Test
    void shouldAddClaimNotificationDeadline_whenClaimIsIssued() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getClaimNotificationDeadline()).isEqualTo(deadline);
    }

}
