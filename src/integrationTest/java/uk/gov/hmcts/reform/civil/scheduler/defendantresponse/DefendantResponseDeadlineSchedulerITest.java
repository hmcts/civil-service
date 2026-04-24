package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

public class DefendantResponseDeadlineSchedulerITest extends BaseIntegrationTest {

    @Autowired
    private DefendantResponseDeadlineScheduler scheduler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private TelemetryService telemetryService;

    @Test
    void shouldExecuteDefendantResponseDeadlineScheduler() {
        // Given
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(case1))
            .build();

        when(coreCaseDataService.searchCases(any(Query.class))).thenReturn(searchResult);

        // When
        scheduler.deadlineCheck();

        // Then
        verify(coreCaseDataService, atLeastOnce()).searchCases(any(Query.class));
        verify(coreCaseDataService, atLeastOnce()).triggerEvent(1L, DEFENDANT_RESPONSE_DEADLINE_CHECK);
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobCompleted"), anyMap());
    }
}
