package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

@SpringBootTest(classes = {
    ClaimIssueForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class ClaimIssueForSpecHandlerTest {

    @Autowired
    private ClaimIssueForSpecCallbackHandler handler;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
    }
}
