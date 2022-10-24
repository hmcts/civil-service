package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.event.TrialReadyCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;

@SpringBootTest(classes = {
    TrialReadyCheckEventHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    CoreCaseDataService.class
})
class TrialReadyCheckEventHandlerTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private TrialReadyCheckEventHandler handler;

    @Test
    void shouldTakeCaseOfflineOnTrialReadyCheckEvent() {
        Long caseId = 1633357679902210L;
        TrialReadyCheckEvent event = new TrialReadyCheckEvent(caseId);

        handler.checkForTrialReady(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), TRIAL_READY_CHECK);
    }

}
