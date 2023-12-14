package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CASE_FLAGS;

@SpringBootTest(classes = {
    CreateCaseFlagsHandler.class,
    JacksonAutoConfiguration.class
})
@ExtendWith(SpringExtension.class)
class CreateCaseFlagsHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    CreateCaseFlagsHandler handler;

    @Test
    void shouldUpdateUrgentFlagWhenActiveAndCodeMatches() {
        FlagDetail flagDetail = FlagDetail.builder().flagCode("CF0007").status("Active").build();
        Flags flags = Flags.builder().details(ElementUtils.wrapElements(flagDetail)).build();
        CaseData caseData = CaseData.builder().caseFlags(flags).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        Assertions.assertEquals("Yes", response.getData().get("urgentFlag"));
    }

    @Test
    void shouldNotUpdateUrgentFlagWhenActiveAndCodeMatches() {
        FlagDetail flagDetail = FlagDetail.builder().flagCode("CF0008").status("Active").build();
        Flags flags = Flags.builder().details(ElementUtils.wrapElements(flagDetail)).build();
        CaseData caseData = CaseData.builder().caseFlags(flags).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        Assertions.assertEquals("No", response.getData().get("urgentFlag"));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CASE_FLAGS);
    }
}
