package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.judgementbuffer.JudgementBufferScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=JudgementBufferSchedulerITest",
    "scheduler.judgementBuffer.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class JudgementBufferSchedulerITest {

    private static final Long CASE_ID = 123L;

    @Autowired
    private JudgementBufferScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isJudgmentBufferEnabled()).thenReturn(true);
        coreCaseDataApiMockHelper.setupIdamClient();
    }

    @Test
    void shouldExecuteJudgementBufferScheduler() {
        // Given
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateJudgmentRequested().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockStartEvent(caseIdString, startEventResponse);
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("JudgementBufferJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("JudgementBufferJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent();
    }
}
