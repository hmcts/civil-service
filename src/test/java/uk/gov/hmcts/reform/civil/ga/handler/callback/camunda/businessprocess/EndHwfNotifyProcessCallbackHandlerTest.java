package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_GA_HWF_NOTIFY_PROCESS;

@SpringBootTest(classes = {
    EndHwfNotifyProcessCallbackHandler.class,
    ObjectMapper.class
})
public class EndHwfNotifyProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired EndHwfNotifyProcessCallbackHandler handler;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(END_GA_HWF_NOTIFY_PROCESS);
    }
}
