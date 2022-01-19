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
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationHandler extends CallbackHandler {

    private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INITIATE_GENERAL_APPLICATION);
    private final InitiateGeneralApplicationService initiateGeneralApplicationService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
            callbackKey(MID, VALIDATE_URGENCY_DATE_PAGE), this::gaValidateUrgencyDate,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse gaValidateUrgencyDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAUrgencyRequirement generalAppUrgencyRequirement = caseData.getGeneralAppUrgencyRequirement();
        List<String> errors = new ArrayList<>();
        if (generalAppUrgencyRequirement.getGeneralAppUrgency() == YES
                && generalAppUrgencyRequirement.getUrgentAppConsiderationDate() == null) {
            errors.add("Details of urgency consideration date required.");
        }
        if (generalAppUrgencyRequirement.getGeneralAppUrgency() == NO
                && generalAppUrgencyRequirement.getUrgentAppConsiderationDate() != null) {
            errors.add("Urgency consideration date should not be provided for a non-urgent application.");
        }

        if (generalAppUrgencyRequirement.getGeneralAppUrgency() == YES
                && generalAppUrgencyRequirement.getUrgentAppConsiderationDate() != null) {
            LocalDate urgencyDate = generalAppUrgencyRequirement.getUrgentAppConsiderationDate();
            if (LocalDate.now().isAfter(urgencyDate)) {
                errors.add("The date entered cannot be in the past.");
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        return caseData.toBuilder();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(initiateGeneralApplicationService.buildCaseData(dataBuilder, caseData).toMap(objectMapper))
            .build();
    }

    /**
     * To be used to return empty callback response, will be used in overriding classes.
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty callback response
     */
    protected CallbackResponse emptyCallbackResponse(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    /**
     * Returns empty submitted callback response. Used by events that set business process to ready, but doesn't have
     * any submitted callback logic (making callback is still required to trigger EventEmitterAspect)
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty submitted callback response
     */
    protected CallbackResponse emptySubmittedCallbackResponse(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder().build();
    }
}
