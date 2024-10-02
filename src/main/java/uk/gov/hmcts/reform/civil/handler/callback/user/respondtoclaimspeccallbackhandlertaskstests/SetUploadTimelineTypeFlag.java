package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
        log.info("Executing SetUploadTimelineTypeFlag task");
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedData = initializeUpdatedData(caseData);
        Set<DefendantResponseShowTag> updatedShowConditions = getUpdatedShowConditions(caseData);

        updateShowConditionsForTimeline(caseData, updatedShowConditions);

        updatedData.showConditionFlags(updatedShowConditions);
        log.debug("Updated show conditions: {}", updatedShowConditions);

        CallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
        log.info("SetUploadTimelineTypeFlag task completed");
        return response;
    }

    private CaseData.CaseDataBuilder<?, ?> initializeUpdatedData(CaseData caseData) {
        log.debug("Initializing updated data for CaseData");
        return caseData.toBuilder();
    }

    private Set<DefendantResponseShowTag> getUpdatedShowConditions(CaseData caseData) {
        log.debug("Getting updated show conditions for CaseData");
        Set<DefendantResponseShowTag> updatedShowConditions = new HashSet<>(caseData.getShowConditionFlags());
        updatedShowConditions.removeIf(EnumSet.of(TIMELINE_UPLOAD, TIMELINE_MANUALLY)::contains);
        return updatedShowConditions;
    }

    private void updateShowConditionsForTimeline(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        log.debug("Updating show conditions for timeline");
        if (isTimelineUpload(caseData)) {
            log.info("Timeline upload condition met");
            updatedShowConditions.add(TIMELINE_UPLOAD);
        } else if (isTimelineManual(caseData)) {
            log.info("Timeline manual condition met");
            updatedShowConditions.add(TIMELINE_MANUALLY);
        }
    }

    private boolean isTimelineUpload(CaseData caseData) {
        boolean result = (YES.equals(caseData.getIsRespondent1()) && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.UPLOAD)
            || (YES.equals(caseData.getIsRespondent2()) && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.UPLOAD);
        log.debug("isTimelineUpload: {}", result);
        return result;
    }

    private boolean isTimelineManual(CaseData caseData) {
        boolean result = (YES.equals(caseData.getIsRespondent1()) && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.MANUAL)
            || (YES.equals(caseData.getIsRespondent2()) && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.MANUAL);
        log.debug("isTimelineManual: {}", result);
        return result;
    }
}
