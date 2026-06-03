package uk.gov.hmcts.reform.civil.handler.tasks;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.search.ClaimDetailsNotificationDeadlineSearchService;

@Component
public class ClaimDetailsNotificationDeadlineHandler extends AbstractDismissClaimDeadlineHandler {

    public ClaimDetailsNotificationDeadlineHandler(ClaimDetailsNotificationDeadlineSearchService caseSearchService,
                                                   ApplicationEventPublisher applicationEventPublisher) {
        super(caseSearchService, applicationEventPublisher);
    }
}
