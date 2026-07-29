package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.search.ElasticSearchService;

import java.util.Set;

@Slf4j
abstract class AbstractDismissClaimDeadlineHandler extends BaseExternalTaskHandler {

    private final ElasticSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    protected AbstractDismissClaimDeadlineHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        ElasticSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        for (CaseDetails caseDetails : cases) {
            publishDismissClaimEvent(caseDetails);
            throttle(cases.size());
        }
        return new ExternalTaskData();
    }

    private void publishDismissClaimEvent(CaseDetails caseDetails) {
        try {
            applicationEventPublisher.publishEvent(new DismissClaimEvent(caseDetails.getId()));
        } catch (Exception e) {
            // Continue processing the remaining cases even if one case update fails.
            log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
        }
    }
}
