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
import uk.gov.hmcts.reform.civil.handler.event.EvidenceUploadNotificationEventHandler;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadRespondentNotificationHandler;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.evidenceupload.EvidenceUploadScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_CHECK;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=EvidenceUploadSchedulerIT",
    "scheduler.defendant-response.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class EvidenceUploadSchedulerIT {

    private static final Long CASE_ID = 123L;

    @Autowired
    private EvidenceUploadScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private EvidenceUploadNotificationEventHandler evidenceUploadNotificationEventHandler;

    @MockBean
    private EvidenceUploadApplicantNotificationHandler evidenceUploadApplicantNotificationHandler;

    @MockBean
    private EvidenceUploadRespondentNotificationHandler evidenceUploadRespondentNotificationHandler;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(EvidenceUploadScheduler.SCHEDULER_NAME))
            .thenReturn(true);
    }

    @Test
    void shouldExecuteEvidenceUploadScheduler() {
        // Given
        String caseIdString = CASE_ID.toString();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateJudgmentRequested().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(caseIdString).caseDetails(
            caseDetails).build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);
        coreCaseDataApiMockHelper.mockGetCase(caseIdString, caseDetails);
        coreCaseDataApiMockHelper.mockStartEvent(
            caseIdString,
            startEventResponse,
            EVIDENCE_UPLOAD_CHECK.name()
        );
        coreCaseDataApiMockHelper.mockSubmitEvent(caseIdString, caseDetails);

        // When
        scheduler.runScheduledTask();

        // Then
        verify(telemetryService).trackEvent(eq("EvidenceUploadJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("EvidenceUploadJobCompleted"), anyMap());
        coreCaseDataApiMockHelper.verifySubmitEvent(1);
    }
}
