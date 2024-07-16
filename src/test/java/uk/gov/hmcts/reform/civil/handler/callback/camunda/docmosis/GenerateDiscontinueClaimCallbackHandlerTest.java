package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDiscontinueClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class GenerateDiscontinueClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateDiscontinueClaimCallbackHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(GEN_NOTICE_OF_DISCONTINUANCE, GEN_NOTICE_OF_DISCONTINUANCE);
    }

    @Test
    void shouldReturnSpecCamundaTask_whenSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GEN_NOTICE_OF_DISCONTINUANCE").build()).build())).isEqualTo("GenerateNoticeOfDiscontinueClaim");
    }

    @Test
    void shouldReturnUnSpecCamundaTask_whenUnSpecEvent() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "GEN_NOTICE_OF_DISCONTINUANCE").build()).build())).isEqualTo("GenerateNoticeOfDiscontinueClaim");
    }
}
