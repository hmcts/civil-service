package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    InformAgreedExtensionDateForSpecCallbackHandler.class
})
public class InformAgreedExtensionDateForSpecCallbackHandlerTest {

    @Autowired
    private InformAgreedExtensionDateForSpecCallbackHandler handler;

    @MockBean
    private ExitSurveyContentService exitSurveyContentService;

    @MockBean
    private DeadlineExtensionValidator validator;

    @MockBean
    private ObjectMapper objectMapper;

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
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }
}
