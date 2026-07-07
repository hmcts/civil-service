package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=GAOrderMadeSchedulerIT",
    "scheduler.ga-order-made.enabled=true",
    "scheduler.lockAtLeastFor=PT0S"
})
public class GAOrderMadeSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "GAOrderMadeScheduler";

    @Autowired
    private GAOrderMadeScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CaseStateSearchService searchService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private GAOrderMadeScheduledTask gaOrderMadeScheduledTask;

    @BeforeEach
    void setUp() {
        reset(telemetryService, featureToggleService, searchService, caseDetailsConverter, gaOrderMadeScheduledTask);
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(gaOrderMadeScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(gaOrderMadeScheduledTask.getItemId(any(GeneralApplicationCaseData.class))).thenAnswer(invocation ->
            invocation.<GeneralApplicationCaseData>getArgument(0).getCcdCaseReference());
    }

    @Test
    void shouldExecuteGAOrderMadeScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDate(LocalDate.now()))
            .build();
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)).thenReturn(Set.of(searchCase));
        when(caseDetailsConverter.toGeneralApplicationCaseData(searchCase)).thenReturn(caseData);
        when(gaOrderMadeScheduledTask.hasExpiredStayDeadline(caseData)).thenReturn(true);

        scheduler.runScheduledTask();

        verify(gaOrderMadeScheduledTask).accept(caseData);
        verify(telemetryService).trackEvent(eq("GAOrderMadeSchedulerJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("GAOrderMadeSchedulerCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("GAOrderMadeSchedulerJobCompleted"), anyMap());
    }
}
