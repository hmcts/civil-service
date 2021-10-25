package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;

@ExtendWith(MockitoExtension.class)
class RespondToClaimSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RespondToClaimSpecCallbackHandler handler;

    @Mock
    private PaymentDateValidator validator;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Nested
    class DefendAllOfClaimTests {

        @Test
        public void testNotSpecDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track");
            when(validator.validate(any())).thenReturn(List.of());

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();
            assertThat(response.getData()).isNotNull();
        }

        @Test
        public void testSpecDefendantResponseValidationError() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");
            when(validator.validate(any())).thenReturn(List.of("Validation error"));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getData()).isNull();
            assertThat(response.getErrors()).isNotNull();
            assertEquals(1, response.getErrors().size());
            assertEquals("Validation error", response.getErrors().get(0));
        }

        @Test
        public void testSpecDefendantResponseFastTrack() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "track", "DEFENDANT_RESPONSE_SPEC");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response).isNotNull();
            assertThat(response.getErrors()).isNull();

            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().get("responseClaimTrack")).isEqualTo(AllocatedTrack.FAST_CLAIM.name());
        }
    }
}
