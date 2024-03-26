package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.event.RespondentResponseDeadlineCheckEvent;
import uk.gov.hmcts.reform.civil.event.TrialReadyCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT_RESPONSE_DEADLINE_CHECK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_CHECK;

@SpringBootTest(classes = {
    RespondentResponseDeadlineCheckEventHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    CoreCaseDataService.class
})
class RespondentResponseDeadlineCheckEventHandlerTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private RespondentResponseDeadlineCheckEventHandler handler;

    @Test
    void shouldTriggerResponseDeadlineCheckOnResponseDeadlineCheckEvent() {
        Long caseId = 1633357679902210L;
        RespondentResponseDeadlineCheckEvent event = new RespondentResponseDeadlineCheckEvent(caseId);

        handler.checkForRespondentResponseDeadline(event);

        verify(coreCaseDataService).triggerEvent(event.getCaseId(), RESPONDENT_RESPONSE_DEADLINE_CHECK);
    }

}
