package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.BreathingSpaceLiftPendingEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.search.breathingspace.BreathingSpaceLiftPendingSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

@Slf4j
@Component
public class BreathingSpaceLiftPendingHandler extends BaseExternalTaskHandler {

    private final BreathingSpaceLiftPendingSearchService searchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public BreathingSpaceLiftPendingHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        BreathingSpaceLiftPendingSearchService searchService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.searchService = searchService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        ElasticSearchResult result = searchService.getElasticSearchResult();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), result.totalResults());

        result.itemStream().forEach(this::publishLiftPendingEvent);

        return new ExternalTaskData();
    }

    private void publishLiftPendingEvent(CaseDetails caseDetails) {
        try {
            applicationEventPublisher.publishEvent(new BreathingSpaceLiftPendingEvent(caseDetails.getId()));
        } catch (Exception e) {
            log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
        }
    }
}
