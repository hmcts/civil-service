package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.event.DeleteExpiredResponseRespondentNotificationsEvent;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.ga.service.search.DeleteExpiredResponseRespondentNotificationSearchService;

import java.util.Set;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class DeleteExpiredResponseRespondentNotificationsHandler extends BaseExternalTaskHandler {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeleteExpiredResponseRespondentNotificationSearchService caseSearchService;

    public DeleteExpiredResponseRespondentNotificationsHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        ApplicationEventPublisher applicationEventPublisher,
        DeleteExpiredResponseRespondentNotificationSearchService caseSearchService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.applicationEventPublisher = applicationEventPublisher;
        this.caseSearchService = caseSearchService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getApplications();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails ->
            applicationEventPublisher.publishEvent(new DeleteExpiredResponseRespondentNotificationsEvent(caseDetails.getId()))
        );

        return new ExternalTaskData();
    }

}
