package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;

@ExtendWith(SpringExtension.class)
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
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
    }
}
