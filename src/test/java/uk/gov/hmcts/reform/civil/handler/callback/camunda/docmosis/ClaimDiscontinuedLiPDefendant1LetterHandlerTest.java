package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1;

@SpringBootTest(classes = {
    ClaimDiscontinuedLiPDefendant1LetterHandler.class,
    JacksonAutoConfiguration.class
})
public class ClaimDiscontinuedLiPDefendant1LetterHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ClaimDiscontinuedLiPDefendant1LetterHandler handler;

    public static final String TASK_ID_DEFENDANT = "ClaimDiscontinuedLiPLetterDef1";

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

}
