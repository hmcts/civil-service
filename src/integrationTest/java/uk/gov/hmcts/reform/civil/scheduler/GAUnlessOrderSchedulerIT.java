package uk.gov.hmcts.reform.civil.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
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
import uk.gov.hmcts.reform.civil.scheduler.gaunlessorder.GAUnlessOrderScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.gaunlessorder.GAUnlessOrderScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class}, properties = {
    "test.id=GAUnlessOrderSchedulerIT",
    "scheduler.ga-unless-order.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GAUnlessOrderSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "GAUnlessOrderScheduler";

    @Autowired
    private GAUnlessOrderScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CaseStateSearchService searchService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private GAUnlessOrderScheduledTask gaUnlessOrderScheduledTask;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(gaUnlessOrderScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(gaUnlessOrderScheduledTask.getItemId(any(GeneralApplicationCaseData.class))).thenAnswer(invocation ->
            invocation.<GeneralApplicationCaseData>getArgument(0).getCcdCaseReference());
    }

    @Test
    void shouldExecuteGAUnlessOrderScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdCaseReference(CASE_ID)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setJudgeApproveEditOptionDateForUnlessOrder(LocalDate.now()))
            .build();
        when(searchService.getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)).thenReturn(Set.of(searchCase));
        when(caseDetailsConverter.toGeneralApplicationCaseData(searchCase)).thenReturn(caseData);
        when(gaUnlessOrderScheduledTask.hasExpiredUnlessOrderDeadline(caseData)).thenReturn(true);

        scheduler.runScheduledTask();

        verify(gaUnlessOrderScheduledTask).accept(caseData);
        verify(telemetryService).trackEvent(eq("GAUnlessOrderSchedulerJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("GAUnlessOrderSchedulerCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("GAUnlessOrderSchedulerJobCompleted"), anyMap());
    }
}
