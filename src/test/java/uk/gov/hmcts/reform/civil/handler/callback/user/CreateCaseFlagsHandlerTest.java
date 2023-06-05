package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CASE_FLAGS;

@SpringBootTest(classes = {
    CreateCaseFlagsHandler.class,
    JacksonAutoConfiguration.class
})
@ExtendWith(SpringExtension.class)
class CreateCaseFlagsHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    FeatureToggleService featureToggleService;

    @Autowired
    CreateCaseFlagsHandler handler;

    @Test
    void shouldSetErrorsWhenLocationIsNotWhiteListed() {
        when(featureToggleService.isLocationWhiteListedForCaseProgression("000000")).thenReturn(false);
        CaseData caseData = CaseData.builder().caseManagementLocation(
            CaseLocationCivil.builder().baseLocation("000000").build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        Assertions.assertEquals(List.of("Case location is not whitelisted for this feature."), response.getErrors());
    }

    @Test
    void shouldNotSetErrorsWhenLocationIsNotWhiteListed() {
        when(featureToggleService.isLocationWhiteListedForCaseProgression("000000")).thenReturn(true);
        CaseData caseData = CaseData.builder().caseManagementLocation(
            CaseLocationCivil.builder().baseLocation("000000").build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        Assertions.assertNull(response.getErrors());
    }

    @Test
    void shouldHandleAboutToStartEventAndSetErrorsWhenLocationIsNotWhiteListed() {
        when(featureToggleService.isLocationWhiteListedForCaseProgression("000000")).thenReturn(false);
        CaseData caseData = CaseData.builder().caseManagementLocation(
            CaseLocationCivil.builder().baseLocation("000000").build()).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        Assertions.assertEquals(List.of("Case location is not whitelisted for this feature."), response.getErrors());
    }

    @Test
    void shouldUpdateUrgentFlagWhenActiveAndCodeMatches() {
        when(featureToggleService.isLocationWhiteListedForCaseProgression(anyString())).thenReturn(true);
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
        when(featureToggleService.isLocationWhiteListedForCaseProgression(anyString())).thenReturn(true);
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
        when(featureToggleService.isLocationWhiteListedForCaseProgression(anyString())).thenReturn(true);
        assertThat(handler.handledEvents()).contains(CREATE_CASE_FLAGS);
    }
}
