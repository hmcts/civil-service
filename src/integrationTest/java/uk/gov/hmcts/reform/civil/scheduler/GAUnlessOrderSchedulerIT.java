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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.scheduler.gaunlessorder.GAUnlessOrderScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.gaunlessorder.GAUnlessOrderScheduler;
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
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=GAUnlessOrderSchedulerIT",
    "scheduler.lockAtLeastFor=PT0S"
})
public class GAUnlessOrderSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "GAUnlessOrderScheduler";

    @Autowired
    private GAUnlessOrderScheduler scheduler;

    @MockitoBean
    private TelemetryService telemetryService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private CaseStateSearchService searchService;

    @MockitoBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockitoBean
    private GAUnlessOrderScheduledTask gaUnlessOrderScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService, searchService, caseDetailsConverter, gaUnlessOrderScheduledTask);
        coreCaseDataApiMockHelper.resetMocks();
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(gaUnlessOrderScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(gaUnlessOrderScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteGAUnlessOrderScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        searchCase.setCaseTypeId(GENERALAPPLICATION_CASE_TYPE);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDateForUnlessOrder(LocalDate.now())
                                           .setIsOrderProcessedByUnlessScheduler(YesOrNo.NO))
            .build();
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(Set.of(searchCase));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any(CaseDetails.class))).thenReturn(caseData);

        coreCaseDataApiMockHelper.mockGetCaseAnyCase(searchCase);

        scheduler.runScheduledTask();

        verify(gaUnlessOrderScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("GAUnlessOrderSchedulerJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("GAUnlessOrderSchedulerCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("GAUnlessOrderSchedulerJobCompleted"), anyMap());
    }
}
