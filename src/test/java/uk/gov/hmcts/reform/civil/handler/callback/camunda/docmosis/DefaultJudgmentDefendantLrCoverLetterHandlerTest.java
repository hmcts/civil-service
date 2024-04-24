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
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.DefaultJudgmentCoverLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_COVER_LETTER_DEFENDANT_LR;

@SpringBootTest(classes = {
    DefaultJudgmentDefendantLrCoverLetterHandler.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentDefendantLrCoverLetterHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgmentDefendantLrCoverLetterHandler handler;
    @MockBean
    private DefaultJudgmentCoverLetterGenerator coverLetterGenerator;

    public static final String TASK_ID = "SendCoverLetterToDefendantLR";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_COVER_LETTER_DEFENDANT_LR);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                SEND_COVER_LETTER_DEFENDANT_LR.name()).build())
                                                 .build())).isEqualTo(TASK_ID);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent1Represented(YesOrNo.YES)
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_COVER_LETTER_DEFENDANT_LR.name());
        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(coverLetterGenerator).generateAndPrintDjCoverLetter(
            caseData,
            params.getParams().get(BEARER_TOKEN).toString()
        );
    }

}
