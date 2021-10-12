package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.civil.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@SpringBootTest(classes = {
    TakeCaseOfflineEventHandler.class,
    JacksonAutoConfiguration.class,
    CoreCaseDataService.class
})
class TakeCaseOfflineEventHandlerTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @Autowired
    private TakeCaseOfflineEventHandler takeCaseOfflineEventHandler;

    @Test
    void shouldTakeCaseOfflineOnTakeCaseOfflineEvent() {
        Long caseId = 1633357679902210L;
        CaseDataContent caseDataContent = CaseDataContent.builder().build();

        TakeCaseOfflineEvent event = new TakeCaseOfflineEvent(caseId);
        takeCaseOfflineEventHandler.takeCaseOffline(event);

        verify(coreCaseDataService).startUpdate(caseId.toString(), TAKE_CASE_OFFLINE);
        verify(coreCaseDataService).submitUpdate(caseId.toString(), caseDataContent)
    }

}
