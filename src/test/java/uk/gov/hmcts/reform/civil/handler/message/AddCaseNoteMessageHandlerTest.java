package uk.gov.hmcts.reform.civil.handler.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.exceptions.CaseDataUpdateException;
import uk.gov.hmcts.reform.civil.model.message.CcdEventMessage;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RPA_ON_CONTINUOUS_FEED;

@ExtendWith(MockitoExtension.class)
class AddCaseNoteMessageHandlerTest {

    private static final long CASE_ID = 1234567891234567L;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @InjectMocks
    private AddCaseNoteMessageHandler addCaseNoteMessageHandler;

    @Test
    public void shouldHandleCorrectEvent() {
        assertThat(addCaseNoteMessageHandler.canHandle(ADD_CASE_NOTE.name())).isTrue();
    }

    @Test
    public void shouldNotHandleOtherEvents() {
        assertThat(addCaseNoteMessageHandler.canHandle(NOTIFY_RPA_ON_CONTINUOUS_FEED.name())).isFalse();
    }

    @Test
    public void shouldPublishEventWhenReceivingHandleableMessage() {

        CcdEventMessage ccdEventMessage = CcdEventMessage.builder()
            .caseId(String.valueOf(CASE_ID))
            .eventId(ADD_CASE_NOTE.name())
            .jurisdictionId("Civil")
            .caseTypeId("CIVIL")
            .build();

        addCaseNoteMessageHandler.handle(ccdEventMessage);

        verify(coreCaseDataService).triggerEvent(CASE_ID, NOTIFY_RPA_ON_CONTINUOUS_FEED);
    }

    @Test
    public void shouldPublishCustomEventObjectToAppInsights_onFailure() {
        doThrow(new CaseDataUpdateException()).when(coreCaseDataService).triggerEvent(CASE_ID, NOTIFY_RPA_ON_CONTINUOUS_FEED);

        CcdEventMessage ccdEventMessage = CcdEventMessage.builder()
            .caseId(String.valueOf(CASE_ID))
            .eventId(ADD_CASE_NOTE.name())
            .jurisdictionId("Civil")
            .caseTypeId("CIVIL")
            .build();

        addCaseNoteMessageHandler.handle(ccdEventMessage);

        verify(caseTaskTrackingService).trackCaseTask(eq(String.valueOf(CASE_ID)),
                                                      eq("service bus message"),
                                                      eq(NOTIFY_RPA_ON_CONTINUOUS_FEED.name()),
                                                      any());
    }

}
