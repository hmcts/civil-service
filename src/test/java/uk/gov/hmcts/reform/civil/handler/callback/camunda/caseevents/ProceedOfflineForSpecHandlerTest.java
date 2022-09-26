package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    ProceedOfflineForSpecCallbackHandler.class
})
public class ProceedOfflineForSpecHandlerTest {

    @Autowired
    private ProceedOfflineForSpecCallbackHandler handler;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }
}
