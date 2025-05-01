package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED_RETRIGGER;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@Service
@RequiredArgsConstructor
public class RequestAHearingCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(HEARING_SCHEDULED_RETRIGGER);
    private final ObjectMapper objectMapper;
    public static final String LISTING_REQUESTED = "## Listing Requested";
    public static final String LISTING_REQUESTED_TASKS = "A work allocation task will be created for a listing officer to list the hearing.";
    private static final DynamicList INTERMEDIATE_LIST = DynamicList.builder().listItems(List.of(DynamicListElement.builder()
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
    private static final DynamicList MULTI_LIST = DynamicList.builder().listItems(List.of(DynamicListElement.builder()
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
            callbackKey(ABOUT_TO_START), this::clearFieldsAndPopulateHearingTypeList,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse clearFieldsAndPopulateHearingTypeList(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataUpdated = caseData.toBuilder();

        String claimTrack = getClaimTrack(caseData);
        if (nonNull(claimTrack) && claimTrack.equals("INTERMEDIATE_CLAIM")) {
            caseDataUpdated.requestHearingNoticeDynamic(INTERMEDIATE_LIST);
        } else if (nonNull(claimTrack) && claimTrack.equals("MULTI_CLAIM")) {
            caseDataUpdated.requestHearingNoticeDynamic(MULTI_LIST);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody())
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(LISTING_REQUESTED);
    }

    private String getBody() {
        return format(LISTING_REQUESTED_TASKS);
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
