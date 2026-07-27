package uk.gov.hmcts.reform.civil.handler.tasks;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.search.ClaimDetailsNotificationDeadlineSearchService;

@Component
public class ClaimDetailsNotificationDeadlineHandler extends AbstractDismissClaimDeadlineHandler {

    public ClaimDetailsNotificationDeadlineHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        ClaimDetailsNotificationDeadlineSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        super(externalTaskCompletionService, eventProperties, caseSearchService, applicationEventPublisher);
    }
}
