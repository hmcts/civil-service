package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.event.StitchingCompleteCallbackHandler;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    StitchingCompleteCallbackHandler.class, JacksonAutoConfiguration.class
})
public class StitchingCompleteCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    StitchingCompleteCallbackHandler handler;
    @Autowired
    private ObjectMapper mapper;
    private List<IdValue<Bundle>> caseBundles;
    private CaseData caseData;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(asyncStitchingComplete);
    }

    @Test
    void shouldSetCategoryId() {
        caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
                .title("Trial Bundle")
                .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
                .stitchedDocument(Optional.of(Document.builder()
                                .documentUrl("url")
                                .documentFileName("name")
                        .build()))
                .build()));
        caseData = CaseData.builder().caseBundles(caseBundles).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getCaseBundles().get(0)
                .getValue().getStitchedDocument().get().getCategoryID()).isEqualTo("bundles");
    }
}
