package uk.gov.hmcts.reform.civil.scheduler;

import net.javacrumbs.shedlock.core.LockProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.scheduler.issuejudgement.JudgementBufferScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JudgementBufferSchedulerITest extends BaseIntegrationTest {

    @Autowired
    private JudgementBufferScheduler scheduler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private TelemetryService telemetryService;

    @SpyBean
    private LockProvider lockProvider;

    @Test
    void shouldExecuteJudgementBufferScheduler() {
        // Given
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(case1))
            .build();

        when(coreCaseDataService.searchCases(any(Query.class))).thenReturn(searchResult);

        // When
        scheduler.issueJudgement();

        // Then
        verify(coreCaseDataService, atLeastOnce()).searchCases(any(Query.class));
        verify(telemetryService).trackEvent(eq("JudgementBufferJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("JudgementBufferCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("JudgementBufferJobCompleted"), anyMap());
    }

    @Test
    void shouldAcquireLockWhenJudgementBufferSchedulerRuns() throws Throwable {
        // Given
        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .cases(List.of())
            .build();
        when(coreCaseDataService.searchCases(any(Query.class))).thenReturn(searchResult);

        // When
        scheduler.issueJudgement();

        // Then
        // Verify that the lock provider was called to acquire a lock
        verify(lockProvider, atLeastOnce()).lock(any());
    }
}
