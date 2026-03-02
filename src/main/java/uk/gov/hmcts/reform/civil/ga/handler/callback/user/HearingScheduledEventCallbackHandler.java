package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED_GA;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class HearingScheduledEventCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    public static final String HEARING_CONFIRMATION_HEADER = """
        # Hearing notice created
        ##### You may need to complete other tasks for the
        ##### hearing for example, book an interpreter.<br/>%s""";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(HEARING_SCHEDULED_GA);
    private final GeneralAppLocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::locationList,
            callbackKey(MID, "hearing-check-date"), this::checkFutureDate,
            callbackKey(ABOUT_TO_SUBMIT), this::validateHearingScheduledProcess,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse locationList(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        DynamicList dynamicLocationList = getLocationsFromList(locationRefDataService.getCourtLocations(authToken));
        if (!caseData.getCcdState().equals(CaseState.ORDER_MADE)
            && Objects.nonNull(caseData.getJudicialListForHearing())
            && Objects.nonNull(caseData.getJudicialListForHearing().getHearingPreferredLocation())
            && Objects.nonNull(caseData.getJudicialListForHearing().getHearingPreferredLocation().getValue())
        ) {
            String preLabel = caseData.getJudicialListForHearing().getHearingPreferredLocation().getValue().getLabel();
            Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
                .filter(l -> l.getLabel().equals(preLabel)).findFirst();
            first.ifPresent(dynamicLocationList::setValue);
        }
        caseDataBuilder.gaHearingNoticeDetail(new GAHearingNoticeDetail()
                                                  .setHearingLocation(dynamicLocationList));

        if (caseData.getCcdState().equals(CaseState.ORDER_MADE)) {
            caseDataBuilder.gaHearingNoticeApplication(null)
                .gaHearingNoticeInformation(null);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .toList());
    }

    private CallbackResponse checkFutureDate(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        LocalDateTime hearingDateTime = null;
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        LocalDate date = caseData.getGaHearingNoticeDetail().getHearingDate();
        String hourMinute = caseData.getGaHearingNoticeDetail().getHearingTimeHourMinute();
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
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private List<String> isFutureDate(LocalDateTime hearingDateTime) {
        List<String> errors = new ArrayList<>();
        if (!checkFutureDateValidation(hearingDateTime)) {
            errors.add("Hearing date must be in the future");
        }
        return errors;
    }

    private boolean checkFutureDateValidation(LocalDateTime localDateTime) {
        return localDateTime != null && localDateTime.isAfter(LocalDateTime.now());
    }

    private CallbackResponse validateHearingScheduledProcess(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        if (nonNull(caseData.getGaHearingNoticeDetail().getHearingLocation())) {
            caseData.getGaHearingNoticeDetail().getHearingLocation().setListItems(null);
        }
        GeneralApplicationCaseData dataBuilder = caseData.copy();
        dataBuilder.businessProcess(BusinessProcess.readyGa(HEARING_SCHEDULED_GA)).build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String confirmationHeader = format(HEARING_CONFIRMATION_HEADER, "<br/>");
        String confirmationBody = "<br/> <br/>";
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(confirmationHeader)
            .confirmationBody(confirmationBody)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
