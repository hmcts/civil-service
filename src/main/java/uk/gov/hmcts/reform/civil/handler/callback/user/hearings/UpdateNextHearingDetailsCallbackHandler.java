package uk.gov.hmcts.reform.civil.handler.callback.user.hearings;

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
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UpdateNextHearingInfo;

@Service
@RequiredArgsConstructor
public class UpdateNextHearingDetailsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =  List.of(
        UpdateNextHearingInfo,
        UPDATE_NEXT_HEARING_DETAILS
    );

    private final ObjectMapper objectMapper;

    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_START), this::updateNextHearingDetails);

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
        CaseData updateData = caseData.toBuilder()
            .nextHearingDetails(
                NextHearingDetails.builder()
                    .hearingID("HER12345")
                    .hearingDateTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
                    .build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updateData.toMap(objectMapper))
            .build();
    }
}
