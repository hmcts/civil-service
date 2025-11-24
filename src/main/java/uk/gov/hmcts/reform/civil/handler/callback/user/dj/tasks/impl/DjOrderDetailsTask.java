package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.service.dj.DjOrderDetailsService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;

@Component
@RequiredArgsConstructor
@Slf4j
public class DjOrderDetailsTask implements DirectionsOrderCallbackTask {

    private static final String TRIAL_DISPOSAL_PAGE_ID = "trial-disposal-screen";
    private static final String CREATE_ORDER_PAGE_ID = "create-order";

    private final DjOrderDetailsService djOrderDetailsService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        String pageId = context.callbackParams().getPageId();
        log.info("DJ order details task for caseId {} on page {}", context.caseData().getCcdCaseReference(), pageId);

        if (TRIAL_DISPOSAL_PAGE_ID.equals(pageId)) {
            return new DirectionsOrderTaskResult(
                djOrderDetailsService.populateTrialDisposalScreen(context),
                null,
                null
            );
        }

        if (CREATE_ORDER_PAGE_ID.equals(pageId)) {
            return new DirectionsOrderTaskResult(
                djOrderDetailsService.applyHearingSelections(
                    context.caseData(),
                    context.callbackParams().getVersion()
                ),
                null,
                null
            );
        }

        return DirectionsOrderTaskResult.empty(context.caseData());
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.ORDER_DETAILS == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, STANDARD_DIRECTION_ORDER_DJ);
    }
}
