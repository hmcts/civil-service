package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1;

@SpringBootTest(classes = {
    ClaimDiscontinuedEmptyHandler.class,
    JacksonAutoConfiguration.class
})
public class ClaimDiscontinuedEmptyHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ClaimDiscontinuedEmptyHandler handler;

    public static final String TASK_ID_DEFENDANT = "ClaimDiscontinuedEmptyHandler";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1.name()).build())
                                                 .build())).isEqualTo(TASK_ID_DEFENDANT);
    }

    @Test
    void shouldNotifyClaimDiscontinuedDefendant1() {
        // given

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent1(Party.builder().partyName("Respondent2 name").type(Party.Type.INDIVIDUAL).build())
            .addRespondent2(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1.name());

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
    }

}
