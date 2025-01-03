package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CASE_FLAGS;

@ExtendWith(MockitoExtension.class)
class ManageCaseFlagsHandlerTest extends BaseCallbackHandlerTest {

    private ManageCaseFlagsHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        handler = new ManageCaseFlagsHandler(objectMapper);
    }

    @Test
    void shouldUpdateUrgentFlagWhenActiveAndCodeMatches() {
        //Given: Flag with urgent flag code and active status
        FlagDetail flagDetail = FlagDetail.builder().flagCode("CF0007").status("Active").build();
        Flags flags = Flags.builder().details(ElementUtils.wrapElements(flagDetail)).build();
        CaseData caseData = CaseData.builder().caseFlags(flags).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //Then: it should update urgent flag to Yes
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
        assertThat(handler.handledEvents()).contains(MANAGE_CASE_FLAGS);
    }
}
