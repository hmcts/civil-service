package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingDateSearchService;

import java.util.Set;

@Slf4j
@Component
public class CvpJoinLinkSchedulerHandler extends BaseExternalTaskHandler {

    private final CaseHearingDateSearchService searchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FeatureToggleService featureToggleService;

    public CvpJoinLinkSchedulerHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CaseHearingDateSearchService searchService,
        ApplicationEventPublisher applicationEventPublisher,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.searchService = searchService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (!featureToggleService.isSpringSchedulerEnabled()) {
            Set<CaseDetails> cases = searchService.getCases();
            log.info("CVP Join Link Scheduler job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

            cases.forEach(caseDetails -> {
                try {
                    log.info("Publishing event for case id: '{}'", caseDetails.getId());
                    applicationEventPublisher.publishEvent(new CvpJoinLinkEvent(caseDetails.getId()));
                    throttle(cases.size());
                } catch (Exception e) {
                    log.error("Publishing 'CvpJoinLinkEvent' event for case id: '{}' failed", caseDetails.getId(), e);
                }
            });
        }
        return new ExternalTaskData();
    }

    @Override
    public int getMaxAttempts() {
        return 1;
    }
}
