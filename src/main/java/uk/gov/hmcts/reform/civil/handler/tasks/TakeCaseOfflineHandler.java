package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.TakeCaseOfflineEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.TakeCaseOfflineSearchService;

import java.util.Set;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

@Slf4j
@Component
public class TakeCaseOfflineHandler extends BaseExternalTaskHandler {

    private final TakeCaseOfflineSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TakeCaseOfflineHandler(
        EventProperties eventProperties,
        TakeCaseOfflineSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        super(eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                log.debug("Started Taking case offline event caseId '{}' status '{}'",
                          caseDetails.getId(), caseDetails.getState());
                applicationEventPublisher.publishEvent(new TakeCaseOfflineEvent(caseDetails.getId()));
                log.debug("Finished Taking case offline caseId '{}'", caseDetails.getId());
            } catch (Exception e) {
                //Continue for other cases if there is some error in some cases, as we don't want
                // to stop processing other valid cases because error happened in some.
                //We log the error to leave a trace that something needs to be looked into for failed cases
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });

        return new ExternalTaskData();
    }

}
