package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHECK_PAID_IN_FULL_SCHED_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus.ACTIVE;

@SpringBootTest(classes = {
    CheckCoscMarkPaidCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
@ExtendWith(MockitoExtension.class)
class CheckCoscMarkPaidCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CheckCoscMarkPaidCallbackHandler checkCoscMarkPaidCallbackHandler;

    @MockBean
    private Time time;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RuntimeService runTimeService;

    private final LocalDateTime nowMock = LocalDateTime.of(2024, 10, 8, 0, 0, 0);
    private final LocalDate expectedlocalDate = LocalDate.of(2024, 10, 8).plusDays(30);
    private static final String PROCESS_INSTANCE_ID = "process-instance-id";

    @Test
    void setCoscSchedulerDeadline_whenActiveJudgmentGetFullyPaymentDateIsNull() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .applicant1Represented(YesOrNo.YES)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();
        when(time.now()).thenReturn(nowMock);
        caseData.setActiveJudgment(JudgmentDetails.builder()
                                       .totalAmount("123")
                                             .build());

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) checkCoscMarkPaidCallbackHandler.handle(params);
        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(response.getErrors()).isNull();
        assertEquals(expectedlocalDate, responseCaseData.getCoscSchedulerDeadline());
        assertEquals(ACTIVE, responseCaseData.getCoSCApplicationStatus());
    }

    @Test
    void doNotSetCoscSchedulerDeadline_whenNoActiveJudgment() {
        when(time.now()).thenReturn(nowMock);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .applicant1Represented(YesOrNo.NO)
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();
        caseData.setActiveJudgment(null);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) checkCoscMarkPaidCallbackHandler.handle(params);
        CaseData data = getCaseData(response);

        assertThat(response.getErrors()).isNull();
        assertNull(data.getCoscSchedulerDeadline());
        assertNull(data.getCoSCApplicationStatus());
    }

    @Test
    void doNotSetCoscSchedulerDeadline_whenActiveJudgementwithDate() {
        when(time.now()).thenReturn(nowMock);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .businessProcess(BusinessProcess.builder().processInstanceId(PROCESS_INSTANCE_ID).build())
            .build();
        caseData.setActiveJudgment(JudgmentDetails.builder()
                                       .fullyPaymentMadeDate(LocalDate.of(2023, 1, 15))
                                             .build());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) checkCoscMarkPaidCallbackHandler.handle(params);
        CaseData data = getCaseData(response);

        assertThat(response.getErrors()).isNull();
        assertNull(data.getCoscSchedulerDeadline());
        assertNull(data.getCoSCApplicationStatus());
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(checkCoscMarkPaidCallbackHandler.handledEvents()).contains(CHECK_PAID_IN_FULL_SCHED_DEADLINE);
    }

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

}
