package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    ValidateFeeForSpecCallbackHandler.class
})
public class ValidateFeeForSpecHandlerTest {

    @Autowired
    private ValidateFeeForSpecCallbackHandler handler;

    @MockBean
    private FeesService feesService;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }
}
