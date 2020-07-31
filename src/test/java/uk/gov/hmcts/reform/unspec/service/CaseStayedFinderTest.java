package uk.gov.hmcts.reform.unspec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStayedEvent;
import uk.gov.hmcts.reform.unspec.service.search.CaseStayedSearchService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CaseStayedFinderTest {

    @Mock
    private JobKey jobKey;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private CaseStayedSearchService searchService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CaseStayedFinder caseStayedFinder;

    @BeforeEach
    void init() {
        when(jobKey.getName()).thenReturn("testName");
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
    }

    @Test
    void shouldEmitMoveCaseToStayedEvent_WhenCasesFound() {
        long caseId = 1L;
        Map<String, Object> data = Map.of("data", "some data");
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder()
                                                    .id(caseId)
                                                    .data(data)
                                                    .build());

        when(searchService.getCases()).thenReturn(caseDetails);

        caseStayedFinder.execute(jobExecutionContext);

        verify(applicationEventPublisher).publishEvent(new MoveCaseToStayedEvent(caseId));
    }

    @Test
    void shouldNotEmitMoveCaseToStayedEvent_WhenNoCasesFound() {
        when(searchService.getCases()).thenReturn(List.of());

        caseStayedFinder.execute(jobExecutionContext);

        verifyNoInteractions(applicationEventPublisher);
    }
}
