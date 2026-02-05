package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderParticipantService;

import java.util.Collections;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;

@Component
@RequiredArgsConstructor
@Slf4j
public class DjPrePopulateTask implements DirectionsOrderCallbackTask {

    private final DirectionsOrderParticipantService participantService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        log.info("DJ pre-populate task for caseId {}", caseData.getCcdCaseReference());
        CaseData updated = caseData.toBuilder()
            .applicantVRespondentText(participantService.buildApplicantVRespondentText(caseData))
            .build();
        return new DirectionsOrderTaskResult(updated, Collections.emptyList(), null);
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.PRE_POPULATE == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, STANDARD_DIRECTION_ORDER_DJ);
    }
}
