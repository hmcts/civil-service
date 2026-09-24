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
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.scheduler.gaordermade.GAOrderMadeScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.gaordermade.GAOrderMadeScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=GAOrderMadeSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
public class GAOrderMadeSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "GAOrderMadeScheduler";

    @Autowired
    private GAOrderMadeScheduler scheduler;

    @MockitoBean
    private TelemetryService telemetryService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private CaseStateSearchService searchService;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private GAOrderMadeScheduledTask gaOrderMadeScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService, searchService, caseDetailsConverter, gaOrderMadeScheduledTask);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(gaOrderMadeScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(gaOrderMadeScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteGAOrderMadeScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        searchCase.setCaseTypeId(GENERALAPPLICATION_CASE_TYPE);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(LocalDate.now()))
            .build();
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(Set.of(searchCase));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);

        coreCaseDataApiMockHelper.mockGetCaseAnyCase(searchCase);

        scheduler.runScheduledTask();

        verify(gaOrderMadeScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("GAOrderMadeSchedulerJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("GAOrderMadeSchedulerCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("GAOrderMadeSchedulerJobCompleted"), anyMap());
    }
}
