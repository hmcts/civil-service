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
import uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue.ClaimSettledDefendantLiPLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;

@SpringBootTest(classes = {
    ClaimSettledLipDefendant1LetterHandler.class,
    JacksonAutoConfiguration.class
})
public class ClaimSettledLipDefendant1LetterHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ClaimSettledLipDefendant1LetterHandler handler;
    @MockBean
    private ClaimSettledDefendantLiPLetterGenerator lipLetterGenerator;

    public static final String TASK_ID = "SendClaimSettledLetterLipDef";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC.name()).build())
                                                 .build())).isEqualTo(TASK_ID);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO).build();
        caseData.setPreStayState(AWAITING_CASE_DETAILS_NOTIFICATION.name());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC.name());

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(lipLetterGenerator).generateAndPrintClaimSettledLetter(
            caseData,
            params.getParams().get(BEARER_TOKEN).toString()
        );
    }

    @Test
    void shouldNotGenerateLetter_whenDefendantIsRepresented() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.YES).build();
        caseData.setPreStayState(AWAITING_CASE_DETAILS_NOTIFICATION.name());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC.name());

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(lipLetterGenerator, never()).generateAndPrintClaimSettledLetter(any(), anyString());
    }

    @Test
    void shouldNotGenerateLetter_whenPreStayStateIsAwaitingClaimNotification() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO).build();
        caseData.setPreStayState(CASE_ISSUED.name());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC.name());

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(lipLetterGenerator, never()).generateAndPrintClaimSettledLetter(any(), anyString());
    }

    @Test
    void shouldNotGenerateLetter_whenPreStayStateIsNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_CLAIM_SETTLED_LETTER_TO_LIP_DEFENDANT1_UNSPEC.name());

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(lipLetterGenerator, never()).generateAndPrintClaimSettledLetter(any(), anyString());
    }
}
