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
import uk.gov.hmcts.reform.civil.enums.nexthearingdate.UpdateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateCamundaService;
import uk.gov.hmcts.reform.civil.service.nexthearingdate.NextHearingDateVariables;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@Service
@RequiredArgsConstructor
public class UpdateNextHearingDetailsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_NEXT_HEARING_DETAILS);
    private final HearingsService hearingsService;
    private final NextHearingDateCamundaService camundaService;
    private final DateUtils dateUtils;

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
        NextHearingDetails nextHearingDetails = variables.getUpdateType() == null
                ? buildNextHearingDetailsFromHmc(callbackParams) : buildNextHearingDetailsFromVariables(variables);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toBuilder().nextHearingDetails(nextHearingDetails).build().toMap(objectMapper))
                .build();
    }

    private NextHearingDetails buildNextHearingDetailsFromHmc(CallbackParams callbackParams) {
        HearingsResponse hearingsResponse = hearingsService.getHearings(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                callbackParams.getRequest().getCaseDetails().getId(),
                LISTED.name()
        );
        return HearingUtils.getNextHearingDetails(hearingsResponse, dateUtils.now());
    }

    private NextHearingDetails buildNextHearingDetailsFromVariables(NextHearingDateVariables variables) {
        UpdateType updateType = variables.getUpdateType();
        switch (updateType) {
            case UPDATE: {
                return NextHearingDetails.builder()
                        .hearingID(variables.hearingId)
                        .hearingDateTime(variables.nextHearingDate)
                        .build();
            }
            case DELETE: {
                return null;
            }
            default: {
                throw new CallbackException("An invalid 'updateType' was provided in the process variables.");
            }
        }
    }
}
