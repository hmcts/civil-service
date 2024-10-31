package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

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
        CaseData caseData = callbackParams.getCaseData();
        Long caseId = caseData.getCcdCaseReference();
        log.info("Executing SetUploadTimelineTypeFlag for case ID: {}", caseId);

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        Set<DefendantResponseShowTag> updatedShowConditions = getUpdatedShowConditions(caseData);

        updateShowConditions(caseData, updatedShowConditions);

        updatedData.showConditionFlags(updatedShowConditions);

        log.info("Updated show conditions for case ID {}: {}", caseId, updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private Set<DefendantResponseShowTag> getUpdatedShowConditions(CaseData caseData) {
        Set<DefendantResponseShowTag> updatedShowConditions = new HashSet<>(caseData.getShowConditionFlags());
        updatedShowConditions.removeIf(EnumSet.of(
            TIMELINE_UPLOAD,
            TIMELINE_MANUALLY
        )::contains);
        return updatedShowConditions;
    }

    private void updateShowConditions(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        if (shouldAddTimelineUpload(caseData)) {
            log.info("Adding TIMELINE_UPLOAD flag for case ID: {}", caseData.getCcdCaseReference());
            updatedShowConditions.add(TIMELINE_UPLOAD);
        } else if (shouldAddTimelineManually(caseData)) {
            log.info("Adding TIMELINE_MANUALLY flag for case ID: {}", caseData.getCcdCaseReference());
            updatedShowConditions.add(TIMELINE_MANUALLY);
        }
    }

    private boolean shouldAddTimelineUpload(CaseData caseData) {
        boolean result = (YES.equals(caseData.getIsRespondent1())
            && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.UPLOAD)
            || (YES.equals(caseData.getIsRespondent2())
            && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.UPLOAD);
        log.info("Checking shouldAddTimelineUpload for case ID {}: {}", caseData.getCcdCaseReference(), result);
        return result;
    }

    private boolean shouldAddTimelineManually(CaseData caseData) {
        boolean result = (YES.equals(caseData.getIsRespondent1())
            && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.MANUAL)
            || (YES.equals(caseData.getIsRespondent2())
            && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.MANUAL);
        log.info("Checking shouldAddTimelineManually for case ID {}: {}", caseData.getCcdCaseReference(), result);
        return result;
    }
}
