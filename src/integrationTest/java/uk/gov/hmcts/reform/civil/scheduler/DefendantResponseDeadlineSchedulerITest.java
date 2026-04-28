package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.defendantresponse.DefendantResponseDeadlineScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = "test.id=DefendantResponseDeadlineSchedulerITest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class DefendantResponseDeadlineSchedulerITest {

    @Autowired
    private DefendantResponseDeadlineScheduler scheduler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldExecuteDefendantResponseDeadlineScheduler() {
        // Given
        ReflectionTestUtils.setField(scheduler, "isSchedulerEnabled", true);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        CaseDetails case1 = CaseDetailsBuilder.builder().id(1L).build();
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
