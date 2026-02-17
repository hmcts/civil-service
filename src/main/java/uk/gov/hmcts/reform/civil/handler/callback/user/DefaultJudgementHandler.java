package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFAULT_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultJudgementHandler extends CallbackHandler {

    public static final String NOT_VALID_DJ = "The Claim is not eligible for Default Judgment until %s";
    public static final String JUDGMENT_GRANTED = "<br /><a href=\"%s\" target=\"_blank\">Download  interim judgment</a> "
        + "%n%n Judgment has been entered and your case will be referred to a judge for directions.";
    public static final String JUDGMENT_REFERRED = "Your request will be referred to a judge and we will contact you "
        + "and tell you what happens next.";
    public static final String JUDGMENT_REQUESTED = "# Judgment for damages to be decided requested ";
    public static final String JUDGMENT_GRANTED_HEADER = "# Judgment for damages to be decided Granted ";
    private static final List<CaseEvent> EVENTS = List.of(DEFAULT_JUDGEMENT);
    private static final int DEFAULT_JUDGEMENT_DEADLINE_EXTENSION_MONTHS = 36;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateDefaultJudgementEligibility,
            callbackKey(MID, "showcertifystatement"), this::checkStatus,
            callbackKey(MID, "checkPreferredLocations"), this::getLocation,
            callbackKey(MID, "acceptCPR"), this::acceptCPR,
            callbackKey(MID, "HearingSupportRequirementsDJ"), this::validateDateValues,
            callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private String getBody(CaseData caseData) {
        if (caseData.getRespondent2() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            return JUDGMENT_REFERRED;
        } else {
            return format(JUDGMENT_GRANTED, format(
                "/cases/case-details/%s#Claim documents",
                caseData.getCcdCaseReference()
            ));
        }
    }

    private String getHeader(CaseData caseData) {
        if (caseData.getRespondent2() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both")) {
            return format(JUDGMENT_REQUESTED);

        } else {
            return format(JUDGMENT_GRANTED_HEADER);
        }
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private CallbackResponse validateDateValues(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();

        List<Element<HearingDates>> hearingDatesElement = caseData
            .getHearingSupportRequirementsDJ().getHearingDates();
        List<String> errors = (Objects.isNull(hearingDatesElement)) ? null :
            isValidRange(hearingDatesElement);
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsForDefaultJudgments(authToken));
        LocationRefData location = fillPreferredLocationData(locations, caseData.getHearingSupportRequirementsDJ());
        if (Objects.nonNull(location)) {
            caseData.setCaseManagementLocation(LocationHelper.buildCaseLocation(location));
            caseData.setLocationName(location.getSiteName());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private List<String> isValidRange(List<Element<HearingDates>> hearingDatesElement) {

        List<String> errors = new ArrayList<>();
        hearingDatesElement.forEach(element -> {
            HearingDates hearingDates = element.getValue();
            if (checkPastDateValidation(hearingDates.getHearingUnavailableFrom())
                || checkPastDateValidation(
                hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable Date cannot be past date");
            } else if (checkThreeMonthsValidation(hearingDates.getHearingUnavailableFrom())
                || checkThreeMonthsValidation(
                hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable Dates must be within the next 3 months.");
            } else if (hearingDates.getHearingUnavailableFrom()
                .isAfter(hearingDates.getHearingUnavailableUntil())) {
                errors.add("Unavailable From Date should be less than To Date");
            }

        });
        return errors;
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        return localDate != null && localDate.isBefore(LocalDate.now());
    }

    private boolean checkThreeMonthsValidation(LocalDate localDate) {
        return localDate != null && localDate.isAfter(LocalDate.now().plusMonths(3));
    }

    private CallbackResponse checkStatus(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        caseData.setBothDefendants("One");
        if (caseData.getDefendantDetails().getValue().getLabel().startsWith("Both")) {
            caseData.setBothDefendants(caseData.getDefendantDetails().getValue().getLabel());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();

    }

    private CallbackResponse getLocation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsForDefaultJudgments(authToken));
        HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
        DynamicList locationsFromList = getLocationsFromList(locations);
        if (caseData.getReasonForTransfer() != null && caseData.getTransferCourtLocationList() != null) {
            CaseLocationCivil cml = caseData.getCaseManagementLocation() != null ? caseData.getCaseManagementLocation()
                : callbackParams.getCaseDataBefore().getCaseManagementLocation();
            if (cml != null) {
                String baseLocation = cml.getBaseLocation();
                List<LocationRefData> locationRef = (locationRefDataService
                    .getCourtLocationsByEpimmsIdAndCourtType(authToken, baseLocation));
                if (!locationRef.isEmpty()) {
                    LocationRefData locationRefData = locations.getFirst();
                    locationsFromList.setValue(DynamicListElement.dynamicElementFromCode(baseLocation,
                                                                                         getLocationLabel(
                                                                                             locationRefData)
                    ));
                }
                log.info("If locationsList [{}] for caseId [{}]", locationsFromList, caseData.getCcdCaseReference());
            }
            hearingSupportRequirementsDJ.setHearingTemporaryLocation(locationsFromList);
        } else {
            log.info("Else locationsList [{}] for caseId [{}]", locationsFromList, caseData.getCcdCaseReference());
            hearingSupportRequirementsDJ.setHearingTemporaryLocation(locationsFromList);
        }

        caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse acceptCPR(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        var acceptance = callbackParams.getRequest().getCaseDetails().getData().get("CPRAcceptance");
        var acceptance2Def = callbackParams.getRequest().getCaseDetails().getData().get("CPRAcceptance2Def");
        if (Objects.isNull(acceptance) && Objects.isNull(acceptance2Def)) {
            errors.add("To apply for default judgment, all of the statements must apply to the defendant "
                           + "- if they do not apply, close this page and apply for default judgment when they do");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse validateDefaultJudgementEligibility(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        ArrayList<String> errors = new ArrayList<>();
        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            // CIV-11985 restrict this event until 5 pm
            LocalDateTime deadlineToUse = caseData.getRespondent1ResponseDeadline()
                .toLocalDate().atTime(17, 0);
            if (deadlineToUse.isAfter(LocalDateTime.now())) {
                String formattedDeadline = formatLocalDateTime(deadlineToUse, DATE_TIME_AT);
                errors.add(format(NOT_VALID_DJ, formattedDeadline));
            }
        }
        List<String> listData = new ArrayList<>();
        listData.add(getPartyNameBasedOnType(caseData.getRespondent1()));
        if (nonNull(caseData.getRespondent2())) {
            listData.add(getPartyNameBasedOnType(caseData.getRespondent2()));
            listData.add("Both Defendants");
            caseData.setDefendantDetails(DynamicList.fromList(listData));
        }
        caseData.setDefendantDetails(DynamicList.fromList(listData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty()
                      ? caseData.toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData caseData = callbackParams.getCaseData();
        if (Objects.nonNull(caseData.getHearingSupportRequirementsDJ())) {
            DynamicList list = formatLocationList(caseData.getHearingSupportRequirementsDJ()
                                                      .getHearingTemporaryLocation());
            HearingSupportRequirementsDJ hearingSupportRequirementsDJ = caseData.getHearingSupportRequirementsDJ();
            hearingSupportRequirementsDJ.setHearingTemporaryLocation(list);
            caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
            final String epimId = list.getValue().getCode();
            List<LocationRefData> locations = (locationRefDataService
                .getCourtLocationsByEpimmsIdAndCourtType(authToken, epimId));

            if (!locations.isEmpty()) {
                LocationRefData locationRefData = locations.getFirst();
                CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
                caseLocationCivil.setRegion(locationRefData.getRegionId());
                caseLocationCivil.setBaseLocation(locationRefData.getEpimmsId());
                caseData.setCaseManagementLocation(caseLocationCivil);
            }
        }

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicantDJ(caseData);
        caseData.setSetRequestDJDamagesFlagForWA(YesOrNo.YES);
        caseData.setBusinessProcess(BusinessProcess.ready(DEFAULT_JUDGEMENT));
        caseData.setClaimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
            DEFAULT_JUDGEMENT_DEADLINE_EXTENSION_MONTHS,
            LocalDate.now()
        ));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        List<DynamicListElement> list = locations.stream()
            .map(location ->
                     DynamicListElement.dynamicElementFromCode(
                         location.getEpimmsId(),
                         location.getSiteName()
                             + " - " + location.getCourtAddress()
                             + " - " + location.getPostcode()
                     )
            ).sorted((object1, object2) -> object1.getLabel().compareTo(object2.getLabel()))
            .toList();
        return DynamicList.fromDynamicListElementList(list);
    }

    private LocationRefData fillPreferredLocationData(final List<LocationRefData> locations,
                                                      HearingSupportRequirementsDJ data) {
        if (Objects.isNull(data.getHearingTemporaryLocation()) || Objects.isNull(locations)) {
            return null;
        }
        String locationLabel = data.getHearingTemporaryLocation().getValue().getLabel();
        var preferredLocation =
            locations
                .stream()
                .filter(locationRefData -> checkLocation(
                    locationRefData,
                    locationLabel
                )).findFirst();
        return preferredLocation.orElse(null);
    }

    public static Boolean checkLocation(final LocationRefData location, String locationTempLabel) {
        return getLocationLabel(location).equals(locationTempLabel);
    }

    private static String getLocationLabel(final LocationRefData location) {
        return location.getSiteName()
            + " - " + location.getCourtAddress()
            + " - " + location.getPostcode();
    }

    private DynamicList formatLocationList(DynamicList locationList) {
        List<DynamicListElement> list = locationList.getListItems()
            .stream()
            .filter(element -> checkLocationItemValue(element, locationList.getValue())).toList();
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(locationList.getValue());
        dynamicList.setListItems(list);
        return dynamicList;
    }

    private boolean checkLocationItemValue(DynamicListElement element, DynamicListElement preferredLocation) {
        return element.getLabel().equals(preferredLocation.getLabel());
    }
}
