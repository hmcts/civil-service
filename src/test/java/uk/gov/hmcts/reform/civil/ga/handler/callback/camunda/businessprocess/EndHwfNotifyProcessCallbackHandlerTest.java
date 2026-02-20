package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_GA_HWF_NOTIFY_PROCESS;

@ExtendWith(MockitoExtension.class)
public class EndHwfNotifyProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    EndHwfNotifyProcessCallbackHandler handler;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(END_GA_HWF_NOTIFY_PROCESS);
    }
}
