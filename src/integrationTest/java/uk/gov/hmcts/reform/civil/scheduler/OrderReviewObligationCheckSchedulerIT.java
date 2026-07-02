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
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.TestIdamConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation.OrderReviewObligationCheckScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation.OrderReviewObligationCheckScheduler;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.test.config.CoreCaseDataApiMockHelperConfiguration;
import uk.gov.hmcts.test.helper.CoreCaseDataApiMockHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = {Application.class, TestIdamConfiguration.class, CoreCaseDataApiMockHelperConfiguration.class}, properties = {
    "test.id=OrderReviewObligationCheckSchedulerIT",
    "scheduler.bundle-creation.enabled=false",
    "scheduler.hearing-cvp-link.enabled=false",
    "scheduler.polling-event-emitter.enabled=false",
    "scheduler.automated-hearing-notice.enabled=false",
    "scheduler.mediation-file-transfer.enabled=false",
    "scheduler.take-case-offline.enabled=false",
    "scheduler.trial-ready-notification.enabled=false",
    "scheduler.order-review-obligation-check.enabled=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class OrderReviewObligationCheckSchedulerIT {

    private static final Long CASE_ID = 123L;
    private static final String SCHEDULER_NAME = "OrderReviewObligationCheck";

    @Autowired
    private OrderReviewObligationCheckScheduler scheduler;

    @MockBean
    private TelemetryService telemetryService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private OrderReviewObligationCheckScheduledTask orderReviewObligationCheckScheduledTask;

    @Autowired
    private CoreCaseDataApiMockHelper coreCaseDataApiMockHelper;

    @BeforeEach
    void setUp() {
        coreCaseDataApiMockHelper.setupIdamClient();
        when(featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)).thenReturn(true);
        when(orderReviewObligationCheckScheduledTask.maxCasesPerRun()).thenReturn(Long.MAX_VALUE);
        when(orderReviewObligationCheckScheduledTask.getItemId(any(CaseDetails.class))).thenAnswer(invocation ->
            invocation.<CaseDetails>getArgument(0).getId());
    }

    @Test
    void shouldExecuteOrderReviewObligationCheckScheduler() {
        CaseDetails searchCase = CaseDetailsBuilder.builder().id(CASE_ID).build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(searchCase))
            .build();

        coreCaseDataApiMockHelper.mockElasticSearchResult(searchResult);

        scheduler.runScheduledTask();

        verify(orderReviewObligationCheckScheduledTask).accept(searchCase);
        verify(telemetryService).trackEvent(eq("OrderReviewObligationCheckJobStarted"), anyMap());
        verify(telemetryService).trackEvent(eq("OrderReviewObligationCheckCaseProcessed"), anyMap());
        verify(telemetryService).trackEvent(eq("OrderReviewObligationCheckJobCompleted"), anyMap());
    }
}
