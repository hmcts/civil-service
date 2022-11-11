package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.aos.AcknowledgementOfClaimGeneratorForSpec;

@SpringBootTest(classes = {
    GenerateAcknowledgementOfClaimForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class GenerateAcknowledgementOfClaimForSpecHandlerTest {

    @Autowired
    private GenerateAcknowledgementOfClaimForSpecCallbackHandler handler;

    @MockBean
    private AcknowledgementOfClaimGeneratorForSpec acknowledgementOfClaimGenerator;

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
    }
}
