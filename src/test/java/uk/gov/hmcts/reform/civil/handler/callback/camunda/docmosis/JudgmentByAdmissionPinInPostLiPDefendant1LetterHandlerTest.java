package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.JudgmentByAdmissionPiPLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER;

@ExtendWith(MockitoExtension.class)
public class JudgmentByAdmissionPinInPostLiPDefendant1LetterHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private JudgmentByAdmissionPinInPostLiPDefendant1LetterHandler handler;
    @Mock
    private JudgmentByAdmissionPiPLetterGenerator letterGenerator;

    public static final String TASK_ID = "SendJudgmentByAdmissionLiPLetterDef1";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER.name()).build())
                                                 .build())).isEqualTo(TASK_ID);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(Party.builder()
                             .individualFirstName("FirstName")
                             .individualLastName("LastName")
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("test")
                             .partyEmail("email").build())
            .respondent1Represented(YesOrNo.NO)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER.name());
        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(letterGenerator).generateAndPrintJudgmentByAdmissionLetter(
            caseData,
            params.getParams().get(BEARER_TOKEN).toString()
        );
    }
}
