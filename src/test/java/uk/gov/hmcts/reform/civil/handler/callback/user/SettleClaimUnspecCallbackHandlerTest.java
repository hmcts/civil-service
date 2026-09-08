package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_UNSPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;

@ExtendWith(MockitoExtension.class)
class SettleClaimUnspecCallbackHandlerTest extends BaseCallbackHandlerTest {

    private SettleClaimUnspecCallbackHandler handler;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = new SettleClaimUnspecCallbackHandler(objectMapper, userService);
    }

    @Test
    void handledEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).containsOnly(SETTLE_CLAIM_UNSPEC);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void should_move_case_to_case_settled_when_triggered_by_caseworker() {
            when(userService.getUserInfo(anyString()))
                .thenReturn(UserInfo.builder().roles(List.of("caseworker-civil-admin")).build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getState()).isEqualTo(CASE_SETTLED.name());
        }

        @Test
        void should_move_case_to_case_stayed_when_triggered_by_claimant_solicitor() {
            when(userService.getUserInfo(anyString()))
                .thenReturn(UserInfo.builder().roles(List.of("caseworker-civil-solicitor")).build());
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(response.getState()).isEqualTo(CASE_STAYED.name());
            assertThat(updatedData.getBusinessProcess().getCamundaEvent())
                .isEqualTo(SETTLE_CLAIM_MARKED_PAID_IN_FULL.name());
            assertThat(updatedData.getPreStayState()).isEqualTo(caseData.getCcdState().toString());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void should_include_header_with_no_next_steps_text() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            Assertions.assertTrue(response.getConfirmationHeader().contains("This claim has been marked as settled"));
            Assertions.assertTrue(response.getConfirmationBody().contains("<br />"));
        }
    }
}