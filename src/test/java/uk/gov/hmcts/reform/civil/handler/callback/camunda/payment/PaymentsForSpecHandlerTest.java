package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentsService;
import uk.gov.hmcts.reform.civil.service.Time;

@SpringBootTest(classes = {
    PaymentsForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class PaymentsForSpecHandlerTest {

    @Autowired
    private PaymentsForSpecCallbackHandler handler;

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
    }
}
