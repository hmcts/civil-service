package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline.GAResponseDeadlineScheduler;
import uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline.GAResponseDeadlineTask;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=GAResponseDeadlineSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
public class GAResponseDeadlineSchedulerIT {

    private static final Long CASE_ID_1 = 123L;
    private static final Long CASE_ID_2 = 456L;
    private static final String SCHEDULER_NAME = "GAResponseDeadline";

    @Autowired
    private GAResponseDeadlineScheduler scheduler;

    @MockitoBean
    private TelemetryService telemetryService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private CaseStateSearchService searchService;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private GAResponseDeadlineTask gaResponseDeadlineTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService, searchService, caseDetailsConverter, gaResponseDeadlineTask);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(gaResponseDeadlineTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(gaResponseDeadlineTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteGAResponseDeadlineScheduler() {
        CaseDetails searchCase1 = CaseDetailsBuilder.builder().id(CASE_ID_1).build();
        searchCase1.setCaseTypeId(GENERALAPPLICATION_CASE_TYPE);
        CaseDetails searchCase2 = CaseDetailsBuilder.builder().id(CASE_ID_2).build();
        searchCase2.setCaseTypeId(GENERALAPPLICATION_CASE_TYPE);

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppNotificationDeadlineDate(LocalDateTime.now().minusDays(1));

        when(searchService.getGeneralApplications(AWAITING_RESPONDENT_RESPONSE)).thenReturn(Set.of(searchCase1));
        when(searchService.getUncheckedResponseDeadlineApplications(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)).thenReturn(Set.of(searchCase2));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);

        coreCaseDataApiMockHelper.mockGetCaseAnyCase(searchCase1);
        coreCaseDataApiMockHelper.mockGetCaseAnyCase(searchCase2);

        scheduler.runScheduledTask();

        verify(gaResponseDeadlineTask).accept(searchCase1);
        verify(gaResponseDeadlineTask).accept(searchCase2);
        verify(telemetryService).trackEvent(eq("SchedulerJobStarted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
        verify(telemetryService).trackEvent(eq("SchedulerJobCompleted"), argThat(props -> SCHEDULER_NAME.equals(props.get("schedulerName"))));
    }
}
