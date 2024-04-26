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
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.RecordJudgmentDeterminationOfMeansPiPLetterGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.SetAsideJudgmentInErrorLiPLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_JO_DEFENDANT1_PIN_IN_LETTER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_SET_ASIDE_JUDGEMENT_IN_ERROR_LETTER_TO_LIP_DEFENDANT1;

@SpringBootTest(classes = {
    RecordJudgmentDeterminationOfMeansLiPDefendant1LetterHandler.class,
    JacksonAutoConfiguration.class
})
public class RecordJudgementDeterminationOfMeansLiPDefendant1LetterHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RecordJudgmentDeterminationOfMeansLiPDefendant1LetterHandler handler;
    @MockBean
    private RecordJudgmentDeterminationOfMeansPiPLetterGenerator lipLetterGenerator;

    public static final String TASK_ID_DEFENDANT = "SendRecordJudgmentDeterminationOfMeansLiPLetterDef1";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(POST_JO_DEFENDANT1_PIN_IN_LETTER);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                POST_JO_DEFENDANT1_PIN_IN_LETTER.name()).build())
                                                 .build())).isEqualTo(TASK_ID_DEFENDANT);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1Represented(YesOrNo.NO)
            .buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setJoJudgmentRecordReason(JudgmentRecordedReason.DETERMINATION_OF_MEANS);
        caseData.setJoSetAsideJudgmentErrorText("Some text");
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(POST_JO_DEFENDANT1_PIN_IN_LETTER.name());
        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(lipLetterGenerator).generateAndPrintRecordJudgmentDeterminationOfMeansLetter(
            caseData,
            params.getParams().get(BEARER_TOKEN).toString()
        );
    }

}
