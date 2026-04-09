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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        FlagDetail flagDetail = new FlagDetail();
        flagDetail.setFlagCode("CF0007");
        flagDetail.setStatus("Active");
        Flags flags = new Flags();
        flags.setDetails(ElementUtils.wrapElements(flagDetail));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseFlags(flags);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When: handler is called
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        //Then: it should update urgent flag to Yes
        Assertions.assertEquals("Yes", response.getData().get("urgentFlag"));
    }

    @Test
    void shouldNotUpdateUrgentFlagWhenActiveAndCodeMatches() {
        FlagDetail flagDetail = new FlagDetail();
        flagDetail.setFlagCode("CF0008");
        flagDetail.setStatus("Active");
        Flags flags = new Flags();
        flags.setDetails(ElementUtils.wrapElements(flagDetail));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseFlags(flags);
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
