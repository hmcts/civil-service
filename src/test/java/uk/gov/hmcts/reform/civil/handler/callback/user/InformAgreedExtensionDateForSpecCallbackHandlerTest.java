package uk.gov.hmcts.reform.civil.handler.callback.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    InformAgreedExtensionDateForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class InformAgreedExtensionDateForSpecCallbackHandlerTest {

    @Autowired
    private InformAgreedExtensionDateForSpecCallbackHandler handler;

    @MockBean
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private DeadlineExtensionValidator validator;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private Time time;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private StateFlowEngine stateFlowEngine;

    @MockBean
    private UserService userService;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    void ldBlock() {
        when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        assertTrue(handler.handledEvents().isEmpty());
        assertFalse(handler.handledEvents().isEmpty());
    }

    @Test
    void shouldContainExtendResponseDeadlineEvent_whenPinAndPostEnabled(){
        given(toggleService.isPinInPostEnabled()).willReturn(true);
        assertTrue(handler.handledEvents().contains(CaseEvent.EXTEND_RESPONSE_DEADLINE));
    }

    @Test
    void shouldNotContaineExendResponseDeadlineEvent_whenPinAndPostDisabled() {
        given(toggleService.isPinInPostEnabled()).willReturn(false);
        assertFalse(handler.handledEvents().contains(CaseEvent.EXTEND_RESPONSE_DEADLINE));
    }
}
