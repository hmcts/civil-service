package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;

class GaNotificationHistoryServiceTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRecordRecipientsAndFailuresWithoutChangingBusinessProcessData(boolean failed) {
        var ccd = mock(GaCoreCaseDataService.class);
        Map<String, Object> process = Map.of("activityId", "existing-task", "processInstanceId", "existing-process");
        var data = new HashMap<String, Object>(Map.of("businessProcess", process, "notificationSummary", "old-summary"));
        when(ccd.startGaUpdate("2000", RECORD_NOTIFICATIONS)).thenReturn(StartEventResponse.builder()
            .eventId(RECORD_NOTIFICATIONS.name()).token("event-token")
            .caseDetails(CaseDetails.builder().data(data).build()).build());
        var history = new GaNotificationHistoryService(ccd);
        history.recordNotifications("2000", "Attempted: a@example.com : ref : template || Errors: " + (failed ? "send failed" : ""));
        var content = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(ccd).submitGaUpdate(eq("2000"), content.capture());
        assertThat(content.getValue().getEvent().getSummary()).isEqualTo("a@example.com : ref : template");
        assertThat(content.getValue().getEvent().getDescription()).isEqualTo(failed ? "Errors: send failed" : null);
        assertThat(content.getValue().getEvent().getId()).isEqualTo(RECORD_NOTIFICATIONS.name());
        assertThat(content.getValue().getEventToken()).isEqualTo("event-token");
        @SuppressWarnings("unchecked")
        Map<String, Object> saved = (Map<String, Object>) content.getValue().getData();
        assertThat(saved).containsEntry("businessProcess", process).containsEntry("notificationSummary", null);
    }
}
