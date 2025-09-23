package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_MANUALLY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_UPLOAD;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetUploadTimelineTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing callback task for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        Set<DefendantResponseShowTag> updatedShowConditions = new HashSet<>(caseData.getShowConditionFlags());

        removeExistingTimelineFlags(updatedShowConditions);

        if (shouldAddTimelineUploadFlag(caseData)) {
            updatedShowConditions.add(TIMELINE_UPLOAD);
            log.debug("CaseId {}: Timeline upload flag added", caseData.getCcdCaseReference());
        } else if (shouldAddTimelineManualFlag(caseData)) {
            updatedShowConditions.add(TIMELINE_MANUALLY);
            log.debug("CaseId {}: Timeline manual flag added", caseData.getCcdCaseReference());
        }

        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
    }

    private void removeExistingTimelineFlags(Set<DefendantResponseShowTag> updatedShowConditions) {
        updatedShowConditions.removeIf(EnumSet.of(
                TIMELINE_UPLOAD,
                TIMELINE_MANUALLY
        )::contains);
    }

    private boolean shouldAddTimelineUploadFlag(CaseData caseData) {
        log.debug("Checking if timeline upload flag should be added for caseId: {}", caseData.getCcdCaseReference());
        return (YES.equals(caseData.getIsRespondent1())
                && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.UPLOAD)
                || (YES.equals(caseData.getIsRespondent2())
                && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.UPLOAD);
    }

    private boolean shouldAddTimelineManualFlag(CaseData caseData) {
        log.debug("Checking if timeline manual flag should be added for caseId: {}", caseData.getCcdCaseReference());
        return (YES.equals(caseData.getIsRespondent1())
                && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.MANUAL)
                || (YES.equals(caseData.getIsRespondent2())
                && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.MANUAL);
    }
}
