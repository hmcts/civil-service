/*
package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue.SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1;

@SpringBootTest(classes = {
    SettleClaimMarkedPaidInFullLiPDefendant1LetterHandler.class,
    JacksonAutoConfiguration.class
})
public class SettleClaimMarkedPaidInFullLiPDefendant1LetterHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private SettleClaimMarkedPaidInFullLiPDefendant1LetterHandler handler;
    @MockBean
    private SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator lipLetterGenerator;

    public static final String TASK_ID = "SendSettleClaimPaidInFullLetterLipDef";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1.name()).build())
                                                 .build())).isEqualTo(TASK_ID);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1.name());

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(lipLetterGenerator).generateAndPrintSettleClaimPaidInFullLetter(
            caseData,
            params.getParams().get(BEARER_TOKEN).toString()
        );
    }

}

 */
