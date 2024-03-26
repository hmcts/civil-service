package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.event.DefendantResponseDeadlineCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@SpringBootTest(classes = {
    DefendantResponseDeadlineCheckEventHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    CoreCaseDataService.class
})
class DefendantResponseDeadlineCheckEventHandlerTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private DefendantResponseDeadlineCheckEventHandler handler;

    @Test
    void shouldTriggerResponseDeadlineCheckOnResponseDeadlineCheckEvent() {
        Long caseId = 1633357679902210L;
        DefendantResponseDeadlineCheckEvent event = new DefendantResponseDeadlineCheckEvent(caseId);

        handler.checkForDefendantResponseDeadline(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), DEFENDANT_RESPONSE_DEADLINE_CHECK);
    }

}
