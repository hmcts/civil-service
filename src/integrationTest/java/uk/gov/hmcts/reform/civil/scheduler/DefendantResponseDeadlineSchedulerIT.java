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
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.defendantresponse.DefendantResponseDeadlineScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_DEADLINE_CHECK;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=DefendantResponseDeadlineSchedulerIT",
    "scheduler.defendant-response.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class DefendantResponseDeadlineSchedulerIT {

    private static final Long CASE_ID = 123L;

    @Autowired
    private DefendantResponseDeadlineScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(DefendantResponseDeadlineScheduler.SCHEDULER_NAME))
            .thenReturn(true);
    }

    @Test
    void shouldExecuteDefendantResponseDeadlineScheduler() {
        // Given
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateJudgmentRequested().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockStartEvent(
            caseIdString,
            startEventResponse,
            DEFENDANT_RESPONSE_DEADLINE_CHECK.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("DefendantResponseDeadlineJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(1);
    }
}
