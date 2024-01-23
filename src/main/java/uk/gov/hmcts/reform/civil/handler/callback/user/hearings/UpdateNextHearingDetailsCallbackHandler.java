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
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UpdateNextHearingInfo;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@Service
@RequiredArgsConstructor
public class UpdateNextHearingDetailsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        UPDATE_NEXT_HEARING_DETAILS,
        UpdateNextHearingInfo
    );

    private static final List<String> UPDATE_HEARING_DATE_STATUSES = List.of(LISTED.name(), AWAITING_ACTUALS.name());
    private static final List<String> CLEAR_HEARING_DATE_STATUSES = List.of(COMPLETED.name(), CANCELLED.name(), ADJOURNED.name());

    private final HearingsService hearingService;
    private final Time datetime;
    private final ObjectMapper objectMapper;

    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        Long caseId = callbackParams.getRequest().getCaseDetails().getId();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        HearingsResponse hearingsResponse = hearingService.getHearings(authToken, caseId, null);

        CaseHearing latestHearing = getLatestHearing(hearingsResponse);

        if (UPDATE_HEARING_DATE_STATUSES.contains(latestHearing.getHmcStatus())) {
            LocalDateTime nextHearingDate = getNextHearingDate(latestHearing);
            caseDataBuilder.nextHearingDetails(
                    nextHearingDate != null
                        ? NextHearingDetails.builder()
                            .hearingID(latestHearing.getHearingId().toString())
                            .hearingDateTime(nextHearingDate)
                            .build() : null)
                .build();
        }

        if (CLEAR_HEARING_DATE_STATUSES.contains(latestHearing.getHmcStatus())) {
            caseDataBuilder.nextHearingDetails(null);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    public CaseHearing getLatestHearing(HearingsResponse hearingsResponse) {
        return hearingsResponse.getCaseHearings().stream()
            .max(Comparator.comparing(CaseHearing::getHearingRequestDateTime)).orElse(null);
    }

    public LocalDateTime getNextHearingDate(CaseHearing hearing) {
        return hearing.getHearingDaySchedule()
            .stream()
            .filter(day -> day.getHearingStartDateTime().isAfter(datetime.now().withHour(0).withMinute(0).withSecond(0)))
            .min(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime))
            .orElse(HearingDaySchedule.builder().build())
            .getHearingStartDateTime();
    }
}
