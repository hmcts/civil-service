package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStayedEvent;
import uk.gov.hmcts.reform.unspec.service.search.CaseStayedSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseStayedFinder implements Job {

    private final CaseStayedSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void execute(JobExecutionContext context) {
        final String jobName = context.getJobDetail().getKey().getName();
        log.info("Job {} started", jobName);

        List<CaseDetails> cases = caseSearchService.getCases();

        log.info("Job '{}' found {} case(s)", jobName, cases.size());

        cases.forEach(caseDetails -> applicationEventPublisher.publishEvent(
            new MoveCaseToStayedEvent(caseDetails.getId())));

        log.info("Job '{}' finished", jobName);
    }
}
