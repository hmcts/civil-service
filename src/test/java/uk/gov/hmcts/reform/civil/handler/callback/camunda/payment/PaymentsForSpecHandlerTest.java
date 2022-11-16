package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

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
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    PaymentsForSpecCallbackHandler.class
})
public class PaymentsForSpecHandlerTest {

    @Autowired
    private PaymentsForSpecCallbackHandler handler;

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }
}
