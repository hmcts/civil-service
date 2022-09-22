package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.HearingReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

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
    private final PublicHolidaysCollection publicHolidaysCollection;
    private final HearingReferenceNumberRepository hearingReferenceNumberRepository;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "locationName"), this::locationList)
            .put(callbackKey(MID, "checkPastDate"), this::checkPastDate)
            .put(callbackKey(MID, "checkFutureDate"), this::checkFutureDate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::getDueDateAndFee)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private String getBody() {
        return format(HEARING_TASKS);
    }

    private String getHeader() {
        return format(HEARING_CREATED_HEADER, hearingReferenceNumberRepository.getHearingReferenceNumber());
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader())
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
        StringBuilder stringBuilder = new StringBuilder();
        return fromList(locations.stream().map(location -> stringBuilder.append(location.getSiteName())
                                 .append(" - ").append(location.getCourtAddress())
                                .append(" - ").append(location.getPostcode()).toString())
                            .collect(Collectors.toList()));
    }

    private CallbackResponse checkPastDate(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        LocalDate dateOfApplication = caseData.getDateOfApplication();

        List<String> errors = (Objects.isNull(dateOfApplication)) ? null :
            isPastDate(dateOfApplication);

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private List<String> isPastDate(LocalDate dateOfApplication) {
        List<String> errors = new ArrayList<>();
        if (!checkPastDateValidation(dateOfApplication)) {
            errors.add("The Date must be in the past");
        }
        return errors;
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        return localDate != null && localDate.isBefore(LocalDate.now());
    }

    private CallbackResponse checkFutureDate(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        LocalDateTime hearingDateTime = null;
        var caseData = callbackParams.getCaseData();
        LocalDate date = caseData.getHearingDate();
        String hourMinute = caseData.getHearingTimeHourMinute();
        if (hourMinute != null) {
            int hours = Integer.parseInt(hourMinute.substring(0, 2));
            int minutes = Integer.parseInt(hourMinute.substring(2, 4));
            LocalTime time = LocalTime.of(hours, minutes, 0);
            hearingDateTime = LocalDateTime.of(date, time);
        } else {
            errors.add("Time is required");
        }

        errors = (Objects.isNull(hearingDateTime)) ? null :
            isFutureDate(hearingDateTime);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse getDueDateAndFee(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (caseData.getListingOrRelisting().equals(ListingOrRelisting.LISTING)) {
            if (LocalDate.now().isBefore(caseData.getHearingDate().minusWeeks(4))) {
                caseDataBuilder.hearingDueDate(
                    HearingUtils.addBusinessDays(
                        LocalDate.now(), 7, publicHolidaysCollection.getPublicHolidays()));
            } else {
                caseDataBuilder.hearingDueDate(
                    HearingUtils.addBusinessDays(
                        LocalDate.now(), 20, publicHolidaysCollection.getPublicHolidays()));
            }
            switch (caseData.getAllocatedTrack()) {
                case SMALL_CLAIM:
                    caseDataBuilder.hearingFee("£545");
                    break;
                case FAST_CLAIM:
                    caseDataBuilder.hearingFee(
                        HearingUtils.getFastTrackFee(caseData.getClaimFee().getCalculatedAmountInPence().intValue()));
                    break;
                case MULTI_CLAIM:
                    caseDataBuilder.hearingFee("£1.175");
                    break;
                default:
                    caseDataBuilder.hearingFee("£0");
            }
        }
        if (nonNull(caseData.getHearingLocation())) {
            DynamicList locationList = caseData.getHearingLocation();
            locationList.setListItems(null);
            caseDataBuilder.hearingLocation(locationList);
        }
        caseDataBuilder.businessProcess(BusinessProcess.ready(HEARING_SCHEDULED));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> isFutureDate(LocalDateTime hearingDateTime) {
        List<String> errors = new ArrayList<>();
        if (!checkFutureDateValidation(hearingDateTime)) {
            errors.add("The Date & Time must be 24hs in advance from now");
        }
        return errors;
    }

    private boolean checkFutureDateValidation(LocalDateTime localDateTime) {
        return localDateTime != null && localDateTime.isAfter(LocalDateTime.now().plusHours(24));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
