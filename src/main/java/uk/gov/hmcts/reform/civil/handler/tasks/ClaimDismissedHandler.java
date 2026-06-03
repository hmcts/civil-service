package uk.gov.hmcts.reform.civil.handler.tasks;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.search.CaseDismissedSearchService;

@Component
public class ClaimDismissedHandler extends AbstractDismissClaimDeadlineHandler {

    public ClaimDismissedHandler(CaseDismissedSearchService caseSearchService,
                                 ApplicationEventPublisher applicationEventPublisher) {
        super(caseSearchService, applicationEventPublisher);
    }
}
