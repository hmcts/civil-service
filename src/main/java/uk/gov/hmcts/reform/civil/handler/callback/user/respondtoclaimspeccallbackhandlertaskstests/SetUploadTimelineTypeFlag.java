package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
public class SetUploadTimelineTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = initializeUpdatedData(caseData);
        Set<DefendantResponseShowTag> updatedShowConditions = getUpdatedShowConditions(caseData);

        updateShowConditionsForTimeline(caseData, updatedShowConditions);

        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder<?, ?> initializeUpdatedData(CaseData caseData) {
        return caseData.toBuilder();
    }

    private Set<DefendantResponseShowTag> getUpdatedShowConditions(CaseData caseData) {
        Set<DefendantResponseShowTag> updatedShowConditions = new HashSet<>(caseData.getShowConditionFlags());
        updatedShowConditions.removeIf(EnumSet.of(TIMELINE_UPLOAD, TIMELINE_MANUALLY)::contains);
        return updatedShowConditions;
    }

    private void updateShowConditionsForTimeline(CaseData caseData, Set<DefendantResponseShowTag> updatedShowConditions) {
        if (isTimelineUpload(caseData)) {
            updatedShowConditions.add(TIMELINE_UPLOAD);
        } else if (isTimelineManual(caseData)) {
            updatedShowConditions.add(TIMELINE_MANUALLY);
        }
    }

    private boolean isTimelineUpload(CaseData caseData) {
        return (YES.equals(caseData.getIsRespondent1()) && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.UPLOAD)
            || (YES.equals(caseData.getIsRespondent2()) && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.UPLOAD);
    }

    private boolean isTimelineManual(CaseData caseData) {
        return (YES.equals(caseData.getIsRespondent1()) && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.MANUAL)
            || (YES.equals(caseData.getIsRespondent2()) && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.MANUAL);
    }
}
