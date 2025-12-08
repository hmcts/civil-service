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
    private static final DynamicList INTERMEDIATE_LIST;
    private static final DynamicList MULTI_LIST;

    static {
        DynamicListElement element1 = new DynamicListElement();
        element1.setCode("CASE_MANAGEMENT_CONFERENCE");
        element1.setLabel("Case Management Conference (CMC)");
        DynamicListElement element2 = new DynamicListElement();
        element2.setCode("PRE_TRIAL_REVIEW");
        element2.setLabel("Pre Trial Review (PTR)");
        DynamicListElement element3 = new DynamicListElement();
        element3.setCode("TRIAL");
        element3.setLabel("Trial");
        DynamicListElement element4 = new DynamicListElement();
        element4.setCode("OTHER");
        element4.setLabel("Other");
        
        DynamicList intermediateList = new DynamicList();
        intermediateList.setListItems(List.of(element1, element2, element3, element4));
        INTERMEDIATE_LIST = intermediateList;

        DynamicListElement element5 = new DynamicListElement();
        element5.setCode("CASE_MANAGEMENT_CONFERENCE");
        element5.setLabel("Case Management Conference (CMC)");
        DynamicListElement element6 = new DynamicListElement();
        element6.setCode("COSTS_CASE_MANAGEMENT_CONFERENCE");
        element6.setLabel("Costs and Case Management Conference (CCMC)");
        DynamicListElement element7 = new DynamicListElement();
        element7.setCode("PRE_TRIAL_REVIEW");
        element7.setLabel("Pre Trial Review (PTR)");
        DynamicListElement element8 = new DynamicListElement();
        element8.setCode("TRIAL");
        element8.setLabel("Trial");
        DynamicListElement element9 = new DynamicListElement();
        element9.setCode("OTHER");
        element9.setLabel("Other");
        
        DynamicList multiList = new DynamicList();
        multiList.setListItems(List.of(element5, element6, element7, element8, element9));
        MULTI_LIST = multiList;
    }

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

        String claimTrack = getClaimTrack(caseData);
        if (nonNull(claimTrack) && claimTrack.equals("INTERMEDIATE_CLAIM")) {
            caseData.setHearingListedDynamicList(INTERMEDIATE_LIST);
        } else if (nonNull(claimTrack) && claimTrack.equals("MULTI_CLAIM")) {
            caseData.setHearingListedDynamicList(MULTI_LIST);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
        caseData.setConfirmListingTickBox(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
