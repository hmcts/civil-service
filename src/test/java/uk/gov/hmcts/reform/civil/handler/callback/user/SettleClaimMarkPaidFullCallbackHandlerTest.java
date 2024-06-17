package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARK_PAID_FULL;

@SpringBootTest(classes = {
    SettleClaimMarkPaidFullCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class SettleClaimMarkPaidFullCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private SettleClaimMarkPaidFullCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;
    public static final String PROCEED_HERITAGE_SYSTEM_NEXT_STEPS = "### Next step\n\n The case will now proceed offline " +
        "and your online account will not be updated for this claim. Any updates will be sent by post.";
    public static final String CLOSED_NEXT_STEPS = "### Next step\n\n Any hearing listed will be vacated. " +
        "\n\n The defendants will be notified.";
    public static final String PROCEED_HERITAGE_SYSTEM_HEADER = "### Request is being reviewed";
    public static final String CLOSED_HEADER = "### The claim has been marked as paid in full";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(SETTLE_CLAIM_MARK_PAID_FULL);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturn_error_when_case_in_all_final_orders_issued_state() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotNull();
        }

        @SuppressWarnings("unchecked")
        @Test
        void should_not_return_error_when_case_in_any_other_state_than_all_final_orders_issued_state() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            List<String> claimantNames = new ArrayList<>();
            claimantNames.add(caseData.getApplicant1().getPartyName());
            claimantNames.add(caseData.getApplicant2().getPartyName());
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().get("claimantWhoIsSettling")).isNotNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void should_move_case_to_closed() {

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getState()).isEqualTo(CaseState.CLOSED.name());
        }

        @Test
        void should_move_case_to_proceed_heritage_system() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.NO);
            caseData.setClaimantWhoIsSettling(DynamicList.builder().value(DynamicListElement.builder()
                                                                              .label("Claim 2")
                                                                              .build()).build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getState()).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        public void whenSubmitted_show_closed_header() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains(CLOSED_HEADER));
            Assertions.assertTrue(response.getConfirmationBody().contains(CLOSED_NEXT_STEPS));
        }

        @Test
        public void whenSubmitted_show_proceed_heritage_system_header() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.NO);
            caseData.setClaimantWhoIsSettling(DynamicList.builder().value(DynamicListElement.builder()
                                                                              .label("Claim 2")
                                                                              .build()).build());
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains(PROCEED_HERITAGE_SYSTEM_HEADER));
            Assertions.assertTrue(response.getConfirmationBody().contains(PROCEED_HERITAGE_SYSTEM_NEXT_STEPS));
        }
    }
}
