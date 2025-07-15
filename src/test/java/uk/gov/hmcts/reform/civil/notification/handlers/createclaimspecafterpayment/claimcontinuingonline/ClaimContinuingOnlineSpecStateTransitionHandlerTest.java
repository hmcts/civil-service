package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClaimContinuingOnlineSpecStateTransitionHandlerTest {

    @Mock
    private Time time;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ClaimContinuingOnlineSpecStateTransitionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClaimContinuingOnlineSpecStateTransitionHandler(objectMapper, time);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId() {
        CallbackParams params = CallbackParamsBuilder.builder().build();
        String activityId = handler.camundaActivityId(params);
        assertThat(activityId).isEqualTo(ClaimContinuingOnlineSpecStateTransitionHandler.TASK_ID);
    }

    @Test
    void shouldReturnHandledEvents() {
        List<CaseEvent> events = handler.handledEvents();
        assertThat(events).containsExactlyInAnyOrder(CaseEvent.CLAIM_CONTINUING_ONLINE_SPEC_STATE_TRANSITION);
    }

    @Test
    void shouldStampClaimNotificationDateAndSetAwaitingRespondentAcknowledgement() {
        LocalDateTime fixedNow = LocalDateTime.of(2025, 7, 15, 9, 0);
        when(time.now()).thenReturn(fixedNow);

        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParamsBuilder.builder()
                .of(CallbackType.ABOUT_TO_SUBMIT, caseData)
                .build();

        AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getState())
                .isEqualTo("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        Map<String, Object> data = response.getData();
        String stamp = (String) data.get("claimNotificationDate");
        LocalDateTime parsed = LocalDateTime.parse(stamp);
        assertThat(parsed).isEqualTo(fixedNow);
    }
}
