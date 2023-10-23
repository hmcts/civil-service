package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateCamundaService;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateVariables;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.DELETE;
import static uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType.UPDATE;

@Service
@RequiredArgsConstructor
public class UpdateNextHearingDateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_NEXT_HEARING_DETAILS);
    private final HearingsService hearingsService;
    private final NextHearingDateCamundaService camundaService;

    private final ObjectMapper objectMapper;

    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateNextHearingDetails);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateNextHearingDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        NextHearingDateVariables variables = camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        if (variables.getUpdateType() == null) {
            //Process was triggered via hmc next hearing date scheduler so call hmc to work out the next hearing date
            HearingsResponse hearingsResponse = hearingsService.getHearings(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                callbackParams.getRequest().getCaseDetails().getId(),
                "LISTED"
            );
            updatedData.nextHearingDetails(HearingUtils.getNextHearingDetails(hearingsResponse));
        } else if (variables.getUpdateType().equals(UPDATE)) {
            // Process was triggered via the hmc message bus with updateType UPDATE so next hearing details are provided.
            updatedData.nextHearingDetails(
                NextHearingDetails.builder()
                    .hearingID(variables.hearingId)
                    .hearingDateTime(variables.nextHearingDate)
                    .build());
        } else if (variables.getUpdateType().equals(DELETE)) {
            // Process was triggered via the hmc message bus with updateType DELETE so next hearing details will be cleared.
            updatedData.nextHearingDetails(null);
        } else {
            throw new CallbackException("An invalid 'updateType' was provided in the process variables.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();

    }
}
