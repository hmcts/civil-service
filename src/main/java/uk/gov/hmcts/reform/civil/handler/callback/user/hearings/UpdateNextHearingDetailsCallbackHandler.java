package uk.gov.hmcts.reform.civil.handler.callback.user.hearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UpdateNextHearingInfo;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.AWAITING_ACTUALS;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateNextHearingDetailsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        UpdateNextHearingInfo,
        UPDATE_NEXT_HEARING_DETAILS
    );

    private static final List<String> UPDATE_HEARING_DATE_STATUSES = List.of(LISTED.name(), AWAITING_ACTUALS.name());
    private static final List<String> CLEAR_HEARING_DATE_STATUSES = List.of(COMPLETED.name(), CANCELLED.name(), ADJOURNED.name());

    private final SystemUpdateUserConfiguration userConfig;
    private final UserService userService;
    private final HearingsService hearingService;
    private final Time datetime;
    private final ObjectMapper objectMapper;

    private Map<String, Callback> callbackMap = new ImmutableMap.Builder<String, Callback>()
        .put(callbackKey(ABOUT_TO_START), this::updateNextHearingDetails)
        .put(callbackKey(ABOUT_TO_SUBMIT), this::updateNextHearingDetails)
        .build();

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateNextHearingDetails(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        Long caseId = callbackParams.getRequest().getCaseDetails().getId();

        //Testing purposes ===================================
        if (caseId  == Long.parseLong("1714058217963373")) {

            HearingsResponse hearingsResponse = getHearings(caseId);
            log.info("(GL) Hearing response: " + hearingsResponse);
            var data = caseDataBuilder.nextHearingDetails(null)
                .build().toMap(objectMapper);
            data.put("nextHearingDetails", null);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data).build();
        }
        //=====================================================

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.nextHearingDetails(null)
                      .build().toMap(objectMapper)).build();
    }

    private HearingsResponse getHearings(Long caseId) {
        String userToken = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        return hearingService.getHearings(userToken, caseId, null);
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
