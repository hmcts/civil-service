package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;

@ExtendWith(MockitoExtension.class)
public class StitchingCompleteCallbackHandlerTest extends BaseCallbackHandlerTest {

    private StitchingCompleteCallbackHandler handler;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        handler = new StitchingCompleteCallbackHandler(mapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(asyncStitchingComplete);
    }

    @Test
    void shouldSetCategoryId() {
        List<IdValue<Bundle>> caseBundles = new ArrayList<>();
        caseBundles.add(new IdValue<>("1", Bundle.builder().id("1")
                .title("Trial Bundle")
                .stitchStatus(Optional.of("NEW")).description("Trial Bundle")
                .stitchedDocument(Optional.of(Document.builder()
                                .documentUrl("url")
                                .documentFileName("name")
                        .build()))
                .build()));
        CaseData caseData = CaseData.builder().caseBundles(caseBundles).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getErrors()).isNull();
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getCaseBundles().get(0)
                .getValue().getStitchedDocument().get().getCategoryID()).isEqualTo("bundles");
    }
}
