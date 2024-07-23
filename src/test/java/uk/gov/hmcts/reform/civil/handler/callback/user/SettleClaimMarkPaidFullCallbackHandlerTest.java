package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARKED_PAID_IN_FULL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM_MARK_PAID_FULL;

@ExtendWith(MockitoExtension.class)
class SettleClaimMarkPaidFullCallbackHandlerTest extends BaseCallbackHandlerTest {

    private SettleClaimMarkPaidFullCallbackHandler handler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new SettleClaimMarkPaidFullCallbackHandler(objectMapper);
    }

    public static final String REQUEST_BEING_REVIEWED_NEXT_STEPS = """
            ### Next step

             The case will now proceed offline and your online account will not be updated for this claim. Any updates will be sent by post.""";
    public static final String CLOSED_NEXT_STEPS = """
            ### Next step

             Any hearing listed will be vacated.\s

             The defendants will be notified.""";
    public static final String REQUEST_BEING_REVIEWED_HEADER = "### Request is being reviewed";
    public static final String CLOSED_HEADER = "### The claim has been marked as paid in full";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(SETTLE_CLAIM_MARK_PAID_FULL);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void should_not_return_error_and_populate_claimant_list_if_2v1_case() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted2v1RespondentRegistered().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().get("claimantWhoIsSettling")).isNotNull();
        }

        @Test
        void should_return_error_if_error_not_null() {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            //When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            //Then
            assertThat(response.getErrors()).isNotNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void should_move_case_to_closed_2_claimants_paid_full() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(SETTLE_CLAIM_MARKED_PAID_IN_FULL.name());
            assertThat(response.getState()).isEqualTo(CaseState.CLOSED.name());
        }

        @Test
        void should_move_case_to_closed_for_1vX_case() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(null);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(SETTLE_CLAIM_MARKED_PAID_IN_FULL.name());
            assertThat(response.getState()).isEqualTo(CaseState.CLOSED.name());
        }

        @Test
        void should_return_empty_state_if_only_1_claimant_paid_full() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.NO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            //When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            //Then
            assertThat(response.getState()).isNull();
        }

    }

    @Nested
    class SubmittedCallback {
        @Test
        void whenSubmitted_show_closed_header_if_2_claimants_paid_full() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.YES);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            //When
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertTrue(response.getConfirmationHeader().contains(CLOSED_HEADER));
            Assertions.assertTrue(response.getConfirmationBody().contains(CLOSED_NEXT_STEPS));
        }

        @Test
        void whenSubmitted_show_closed_header_if_1vX_case() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(null);
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            //When
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertTrue(response.getConfirmationHeader().contains(CLOSED_HEADER));
            Assertions.assertTrue(response.getConfirmationBody().contains(CLOSED_NEXT_STEPS));
        }

        @Test
        void whenSubmitted_show_request_being_reviewed_header_if_only_1_claimant_paid_full() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            caseData.setMarkPaidForAllClaimants(YesOrNo.NO);
            caseData.setClaimantWhoIsSettling(DynamicList.builder().value(DynamicListElement.builder()
                                                                              .label("Claim 2")
                                                                              .build()).build());
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            //When
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            //Then
            Assertions.assertTrue(response.getConfirmationHeader().contains(REQUEST_BEING_REVIEWED_HEADER));
            Assertions.assertTrue(response.getConfirmationBody().contains(REQUEST_BEING_REVIEWED_NEXT_STEPS));
        }
    }
}
