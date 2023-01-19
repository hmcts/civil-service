package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {
    ProceedOfflineForSpecCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class ProceedOfflineForSpecHandlerTest {

    @Autowired
    private ProceedOfflineForSpecCallbackHandler handler;


    @Test
    void ldBlock() {
        Assertions.assertTrue(handler.handledEvents().isEmpty());
        Assertions.assertFalse(handler.handledEvents().isEmpty());
    }
}
