package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.SetAsideJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_ASIDE_JUDGMENT;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgmentCallbackHandlerTest extends BaseCallbackHandlerTest {

    private SetAsideJudgmentCallbackHandler handler;

    private SetAsideJudgmentOnlineMapper setAsideJudgmentOnlineMapper;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        setAsideJudgmentOnlineMapper = new SetAsideJudgmentOnlineMapper();
        handler = new SetAsideJudgmentCallbackHandler(objectMapper, setAsideJudgmentOnlineMapper, deadlinesCalculator);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(SET_ASIDE_JUDGMENT);
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime timeExtensionDate;
        LocalDate respondent1ResponseDeadline;

        @BeforeEach
        void setup() {
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            handler = new SetAsideJudgmentCallbackHandler(objectMapper, setAsideJudgmentOnlineMapper, deadlinesCalculator);
            timeExtensionDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            respondent1ResponseDeadline = now().plusDays(28);
        }

        @Test
        void shouldPopulateSetAsideDate() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideOrderDate", "2022-12-12");
            assertThat(response.getData().get("activeJudgment")).isNull();
            assertThat(response.getData().get("historicJudgment")).isNotNull();
            JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
            assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
            assertEquals(caseData.getJoSetAsideOrderDate(), historicJudgment.getSetAsideDate());
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(SET_ASIDE_JUDGMENT.name(), "READY");
        }

        @Test
        void shouldPopulateDefenceReceivedDate() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
            caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2022, 12, 12));
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideDefenceReceivedDate", "2022-12-12");
            assertThat(response.getData().get("activeJudgment")).isNull();
            assertThat(response.getData().get("historicJudgment")).isNotNull();
            JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
            assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
            assertEquals(caseData.getJoSetAsideDefenceReceivedDate(), historicJudgment.getSetAsideDate());
            //and the caseState should not be updated
            assertEquals(response.getState(), CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(SET_ASIDE_JUDGMENT.name(), "READY");
        }

        @Test
        void shouldPopulateSetAsideJudgmentErrorText() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
            caseData.setJoSetAsideJudgmentErrorText("Some text");
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE_ERROR).build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideJudgmentErrorText", "Some text");
            assertThat(response.getData().get("activeJudgment")).isNull();
            assertThat(response.getData().get("historicJudgment")).isNotNull();
            JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
            assertEquals(JudgmentState.SET_ASIDE_ERROR, historicJudgment.getState());
            assertEquals(LocalDate.now(), historicJudgment.getSetAsideDate());
            //and the caseState should not be updated
            assertEquals(response.getState(), CaseState.All_FINAL_ORDERS_ISSUED.name());
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(SET_ASIDE_JUDGMENT.name(), "READY");
        }

        @Test
        void shouldPopulateOrderType() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            LocalDateTime nextDeadline = respondent1ResponseDeadline.atTime(16, 0);
            when(deadlinesCalculator.plus28DaysAt4pmDeadline(now().atStartOfDay()))
                .thenReturn(nextDeadline);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setActiveJudgment(JudgmentDetails.builder().build());
            caseData.setJoSetAsideOrderDate(now());
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("joSetAsideOrderType").isNotNull();
            //and the caseState should be updated
            assertEquals(response.getState(), CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name());
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(SET_ASIDE_JUDGMENT.name(), "READY");
        }

        @Test
        void testSetAsideForDefaultJudgment() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
            caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2022, 12, 12));
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideDefenceReceivedDate", "2022-12-12");
            assertThat(response.getData().get("activeJudgment")).isNull();
            assertThat(response.getData().get("historicJudgment")).isNotNull();
            JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
            assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
            assertEquals(caseData.getJoSetAsideDefenceReceivedDate(), historicJudgment.getSetAsideDate());
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(SET_ASIDE_JUDGMENT.name(), "READY");
        }
    }

    @Nested
    class MidCallback {
        @Test
        void shouldValidateSetAsideDate() {

            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setJoSetAsideOrderDate(LocalDate.now().plusDays(5));

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-set-aside-dates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Date must be in the past");
        }

        @Test
        void shouldValidateDefenceReceivedState() {

            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
            caseData.setJoSetAsideDefenceReceivedDate(LocalDate.now().plusDays(5));

            CallbackParams params = callbackParamsOf(caseData, MID, "validate-set-aside-dates");
            //When: handler is called with MID event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("Date must be in the past");
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void whenSubmitted_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains("# Judgment set aside"));
            Assertions.assertTrue(response.getConfirmationBody().contains("The judgment has been set aside"));
        }
    }
}
