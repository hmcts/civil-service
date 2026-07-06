package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.ManageStayWATaskEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.ManageStayUpdateRequestedSearchService;

import java.util.Set;

@Slf4j
@Component
public class ManageStayWATaskSchedulerHandler extends BaseExternalTaskHandler {

    private static final String SCHEDULER_NAME = "ManageStayWATask";

    private final ManageStayUpdateRequestedSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FeatureToggleService featureToggleService;

    public ManageStayWATaskSchedulerHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        ManageStayUpdateRequestedSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {

        if (featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return new ExternalTaskData();
        }

        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new ManageStayWATaskEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error(
                    "Manage Stay WA Task scheduler failed to process case with id: '{}",
                    caseDetails.getId(),
                    e
                );
            }
        });
        return new ExternalTaskData();
    }

}
