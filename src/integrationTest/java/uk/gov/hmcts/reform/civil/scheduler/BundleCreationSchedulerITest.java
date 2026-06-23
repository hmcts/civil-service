package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.bundlecreation.BundleCreationScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.NoCacheUserService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=BundleCreationSchedulerITest",
    "scheduler.bundleCreation.enabled=true",
    "stitch-bundle.wait-time-in-milliseconds=0"
})
public class BundleCreationSchedulerITest {

    private static final Long CASE_ID = 123L;
    private static final String ACCESS_TOKEN = "access-token";

    @Autowired
    private BundleCreationScheduler scheduler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NoCacheUserService noCacheUserService;

    @MockBean
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void shouldExecuteBundleCreationScheduler() {
        // Given
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        CaseData caseData = new CaseDataBuilder()
            .hearingDate(LocalDate.of(2026, Month.JULY, 1))
            .caseBundles(List.of())
            .build();
        CaseDetails latestCase = CaseDetailsBuilder.builder()
            .id(CASE_ID)
            .data(caseData)
            .build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        when(coreCaseDataService.searchCases(any(Query.class))).thenReturn(searchResult);
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(latestCase);
        when(noCacheUserService.getAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(coreCaseDataService, atLeastOnce()).searchCases(any(Query.class));
        verify(coreCaseDataService).getCase(CASE_ID);
        verify(applicationEventPublisher).publishEvent(new BundleCreationTriggerEvent(CASE_ID, ACCESS_TOKEN));
        verify(telemetryService).trackEvent(eq("BundleCreationJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("BundleCreationCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("BundleCreationJobCompleted"), anyMap());
    }
}
