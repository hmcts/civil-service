package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_LISTING_COMPLETED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@Service
@RequiredArgsConstructor
public class ConfirmListingCompletedCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CONFIRM_LISTING_COMPLETED);
    private final ObjectMapper objectMapper;
    public static final  String errorMessage = "Tick the box to confirm you have listed the required hearings";
    private static final DynamicList INTERMEDIATE_LIST = DynamicList.builder().listItems(List.of(
        DynamicListElement.builder()
            .code("CASE_MANAGEMENT_CONFERENCE")
            .label("Case Management Conference (CMC)")
            .build(),
        DynamicListElement.builder()
            .code("PRE_TRIAL_REVIEW")
            .label("Pre Trial Review (PTR)")
            .build(),
        DynamicListElement.builder()
            .code("TRIAL")
            .label("Trial")
            .build(),
        DynamicListElement.builder()
            .code("OTHER")
            .label("Other")
            .build())).build();
    private static final DynamicList MULTI_LIST = DynamicList.builder().listItems(List.of(
        DynamicListElement.builder()
            .code("CASE_MANAGEMENT_CONFERENCE")
            .label("Case Management Conference (CMC)")
            .build(),
        DynamicListElement.builder()
            .code("COSTS_CASE_MANAGEMENT_CONFERENCE")
            .label("Costs and Case Management Conference (CCMC)")
            .build(),
        DynamicListElement.builder()
            .code("PRE_TRIAL_REVIEW")
            .label("Pre Trial Review (PTR)")
            .build(),
        DynamicListElement.builder()
            .code("TRIAL")
            .label("Trial")
            .build(),
        DynamicListElement.builder()
            .code("OTHER")
            .label("Other")
            .build())).build();

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_START), this::populateDynamicList,
                callbackKey(MID, "validate-confirmed"), this::validateConfirmed,
                callbackKey(ABOUT_TO_SUBMIT), this::submitConfirmation,
                callbackKey(SUBMITTED), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateDynamicList(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataUpdated = caseData.toBuilder();

        String claimTrack = getClaimTrack(caseData);
        if (nonNull(claimTrack) && claimTrack.equals("INTERMEDIATE_CLAIM")) {
            caseDataUpdated.hearingListedDynamicList(INTERMEDIATE_LIST);
        } else if (nonNull(claimTrack) && claimTrack.equals("MULTI_CLAIM")) {
            caseDataUpdated.hearingListedDynamicList(MULTI_LIST);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateConfirmed(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getConfirmListingTickBox() == null) {
            errors.add(errorMessage);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse submitConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataUpdated = caseData.toBuilder();
        caseDataUpdated.confirmListingTickBox(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private String getClaimTrack(CaseData caseData) {
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            return caseData.getAllocatedTrack().name();
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            return caseData.getResponseClaimTrack();
        }
        return null;
    }

}
