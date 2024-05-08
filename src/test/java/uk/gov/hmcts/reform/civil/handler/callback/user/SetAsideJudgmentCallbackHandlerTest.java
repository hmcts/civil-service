package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SET_ASIDE_JUDGMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SetAsideJudgmentCallbackHandler.class,
    JacksonAutoConfiguration.class,
    DeadlinesCalculator.class
})
class SetAsideJudgmentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private SetAsideJudgmentCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private Time time;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(SET_ASIDE_JUDGMENT);
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime timeExtensionDate;
        LocalDate extensionDateRespondent1;

        @BeforeEach
        void setup() {
            timeExtensionDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(timeExtensionDate);
            extensionDateRespondent1 = now().plusDays(28);
            when(deadlinesCalculator.calculateFirstWorkingDay(extensionDateRespondent1))
                .thenReturn(extensionDateRespondent1);

        }

        @Test
        void shouldPopulateSetAsideDate() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            LocalDateTime nextDeadline = extensionDateRespondent1.atTime(16,0);
            when(deadlinesCalculator.plus28DaysAt4pmDeadline(now().atStartOfDay()))
                .thenReturn(nextDeadline);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideOrderDate", "2022-12-12");
        }

        @Test
        void shouldPopulateDefenceReceivedDate() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            LocalDateTime nextDeadline = extensionDateRespondent1.atTime(16,0);
            when(deadlinesCalculator.plus28DaysAt4pmDeadline(now().atStartOfDay()))
                .thenReturn(nextDeadline);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
            caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2022, 12, 12));
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideDefenceReceivedDate", "2022-12-12");
        }

        @Test
        void shouldPopulateSetAsideJudgmentErrorText() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            LocalDateTime nextDeadline = extensionDateRespondent1.atTime(16,0);
            when(deadlinesCalculator.plus28DaysAt4pmDeadline(now().atStartOfDay()))
                .thenReturn(nextDeadline);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
            caseData.setJoSetAsideJudgmentErrorText("Some text");
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).containsEntry("joSetAsideJudgmentErrorText", "Some text");
        }

        @Test
        void shouldPopulateOrderType() {
            //Given : Casedata in All_FINAL_ORDERS_ISSUED State
            LocalDateTime nextDeadline = extensionDateRespondent1.atTime(16,0);
            when(deadlinesCalculator.plus28DaysAt4pmDeadline(now().atStartOfDay()))
                .thenReturn(nextDeadline);
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setJoSetAsideOrderDate(now());
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("joSetAsideOrderType").isNotNull();
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
        public void whenSubmitted_thenIncludeHeader() {
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
