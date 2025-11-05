package uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoPrePopulateService;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.ERROR_MINTI_DISPOSAL_NOT_ALLOWED;

@Component
@RequiredArgsConstructor
public class SdoPrePopulateTask implements SdoCallbackTask {

    private final SdoFeatureToggleService sdoFeatureToggleService;
    private final SdoPrePopulateService sdoPrePopulateService;

    @Override
    public SdoTaskResult execute(SdoTaskContext context) {
        CaseData caseData = context.caseData();

        if (isMultiOrIntermediateTrack(caseData)) {
            return SdoTaskResult.withErrors(caseData, List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED));
        }

        CaseData updatedCaseData = sdoPrePopulateService.prePopulate(context);

        return new SdoTaskResult(updatedCaseData, Collections.emptyList(), null);
    }

    @Override
    public boolean supports(SdoLifecycleStage stage) {
        return SdoLifecycleStage.PRE_POPULATE == stage;
    }

    private boolean isMultiOrIntermediateTrack(CaseData caseData) {
        if (!sdoFeatureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            return false;
        }

        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        String responseClaimTrack = caseData.getResponseClaimTrack();

        boolean isIntermediateTrack = AllocatedTrack.INTERMEDIATE_CLAIM.equals(allocatedTrack)
            || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(responseClaimTrack);
        boolean isMultiTrack = AllocatedTrack.MULTI_CLAIM.equals(allocatedTrack)
            || AllocatedTrack.MULTI_CLAIM.name().equals(responseClaimTrack);

        return isIntermediateTrack || isMultiTrack;
    }
}
