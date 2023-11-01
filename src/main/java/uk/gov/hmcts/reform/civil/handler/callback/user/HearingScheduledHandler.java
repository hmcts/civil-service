package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.utils.HearingReferenceNumber;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;

@Service
@RequiredArgsConstructor
public class HearingScheduledHandler extends CallbackHandler {

    public static final String HEARING_TASKS = "%n%n You may need to complete other tasks for the hearing"
        + ", for example, book an interpreter.";
    public static final String HEARING_CREATED_HEADER = "# Hearing notice created\n"
        + "# Your reference number\n" + "# %s";

    private static final List<CaseEvent> EVENTS = Collections.singletonList(HEARING_SCHEDULED);
    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final HearingFeesService hearingFeesService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::clearPreviousSelections)
            .put(callbackKey(MID, "locationName"), this::locationList)
            .put(callbackKey(MID, "checkPastDate"), this::checkPastDate)
            .put(callbackKey(MID, "checkFutureDate"), this::checkFutureDate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::handleAboutToSubmit)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private String getBody() {
        return format(HEARING_TASKS);
    }

    private String getHeader(CaseData caseData) {
        return format(HEARING_CREATED_HEADER, caseData.getHearingReferenceNumber());
    }

    // hearing notices can be retriggered i.e. relisted, in such case we clear previous selections
    private CallbackResponse clearPreviousSelections(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        caseDataBuilder
            .hearingNoticeList(null)
            .listingOrRelisting(null)
            .hearingLocation(null)
            .channel(null)
            .hearingDate(null)
            .hearingTimeHourMinute(null)
            .hearingDuration(null)
            .information(null)
            .hearingNoticeListOther(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody())
            .build();
    }

    private CallbackResponse locationList(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsForDefaultJudgments(authToken));
        caseDataBuilder.hearingLocation(getLocationsFromList(locations))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .collect(Collectors.toList()));
    }

    private CallbackResponse checkPastDate(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        LocalDate dateOfApplication = caseData.getDateOfApplication();
        // FIXME: 2023-02-28 verify the following condition. Seems to me that if there is no date of application it should be an error
        List<String> errors = (Objects.isNull(dateOfApplication)) ? null :
            checkTrueOrElseAddError(dateOfApplication.isBefore(time.now().toLocalDate()),
                                    "The Date must be in the past");

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    CallbackResponse checkFutureDate(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        var caseData = callbackParams.getCaseData();
        LocalDate date = caseData.getHearingDate();
        String hourMinute = caseData.getHearingTimeHourMinute();
        if (hourMinute != null) {
            int hours = Integer.parseInt(hourMinute.substring(0, 2));
            int minutes = Integer.parseInt(hourMinute.substring(2, 4));
            LocalDateTime hearingDateTime = LocalDateTime.of(date, LocalTime.of(hours, minutes, 0));
            errors.addAll(checkTrueOrElseAddError(hearingDateTime.isAfter(time.now().plusHours(24)),
                                                  "The Date & Time must be 24hs in advance from now"));
        } else {
            errors.add("Time is required");
        }

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse handleAboutToSubmit(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.hearingReferenceNumber(HearingReferenceNumber.generateHearingReference());
        if (nonNull(caseData.getHearingLocation())) {
            DynamicList locationList = caseData.getHearingLocation();
            locationList.setListItems(null);
            caseDataBuilder.hearingLocation(locationList);
        }
        CaseState caseState = HEARING_READINESS;

        var allocatedTrack = caseData.getAllocatedTrack();
        if (isNull(caseData.getAllocatedTrack())) {
            allocatedTrack = AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null);
            caseDataBuilder.allocatedTrack(allocatedTrack);
        }

        if (ListingOrRelisting.LISTING.equals(caseData.getListingOrRelisting())) {
            caseDataBuilder.hearingDueDate(
                calculateHearingDueDate(time.now().toLocalDate(), caseData.getHearingDate()));
            caseDataBuilder.hearingFee(calculateAndApplyFee(hearingFeesService, caseData, allocatedTrack));
        } else {
            caseState = PREPARE_FOR_HEARING_CONDUCT_HEARING;
        }
        caseDataBuilder.businessProcess(BusinessProcess.ready(HEARING_SCHEDULED));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(caseState.name())
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> checkTrueOrElseAddError(boolean condition, String error) {
        if (!condition) {
            return List.of(error);
        }
        return Collections.emptyList();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
