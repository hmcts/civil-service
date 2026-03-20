package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRANSFER_ONLINE_CASE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG_GA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferOnlineCaseCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(TRANSFER_ONLINE_CASE);
    protected final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private static final String ERROR_SELECT_DIFF_LOCATION = "Select a different hearing court location to transfer!";
    private static final String CONFIRMATION_HEADER = "# Case transferred to new location";
    private final FeatureToggleService featureToggleService;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateTransferCourtLocationList)
            .put(callbackKey(MID, "validate-court-location"), this::validateCourtLocation)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveTransferOnlineCase)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateTransferCourtLocationList(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.setTransferCourtLocationList(toDynamicList(fetchLocationData(callbackParams)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateCourtLocation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (isSameCourtSelected(callbackParams)) {
            errors.add(ERROR_SELECT_DIFF_LOCATION);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    /**
     * Handles the AboutToSubmit event for Case Transfer.
     * Updates the case management location, location name, and EA court location.
     * Also updates Work Allocation locations if the case is in multi or intermediate track.
     *
     * @param callbackParams the callback parameters containing case data and bearer token
     * @return the callback response with updated case data and business process
     */
    private CallbackResponse saveTransferOnlineCase(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<LocationRefData> newCourtLocation = Optional.ofNullable(courtLocationUtils.findPreferredLocationData(
            fetchLocationData(callbackParams),
            caseData.getTransferCourtLocationList()
        ));

        newCourtLocation.ifPresent(location -> {
            caseData.setCaseManagementLocation(LocationHelper.buildCaseLocation(location));
            caseData.setLocationName(location.getSiteName());
            if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
                updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    caseData
                ));
            }
            caseData.setEaCourtLocation(determineEaCourtLocation(caseData, location.getEpimmsId()));
        });

        // Clear the list items to avoid large data payloads in the response
        if (caseData.getTransferCourtLocationList() != null) {
            caseData.getTransferCourtLocationList().setListItems(null);
        }
        caseData.setBusinessProcess(BusinessProcess.ready(TRIGGER_TASK_RECONFIG_GA));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String siteName = courtLocationUtils.findPreferredLocationData(
            fetchLocationData(callbackParams),
            callbackParams.getCaseData().getTransferCourtLocationList()
        ).getSiteName();

        String body = String.format(
            "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "The case has now been transferred to %s. "
                + "If the case has moved out of your region, you will no longer see it.<br><br>",
            siteName
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(body)
            .build();
    }

    /**
     * Determines whether the EA court location should be set to YES based on business rules:
     * 1. Welsh cases (for main case) are always set to YES.
     * 2. Litigant in Person (LiP) cases are set based on specific progression and whitelisting rules.
     * 3. Other cases are whitelisted by default for case progression.
     *
     * @param caseData the case data
     * @param epimmsId the epimmsId of the new court location
     * @return YES or NO based on the conditions
     */
    private YesOrNo determineEaCourtLocation(CaseData caseData, String epimmsId) {
        if (featureToggleService.isWelshEnabledForMainCase()) {
            return YES;
        }
        if (caseData.isApplicantLiP() || caseData.isRespondent1LiP() || caseData.isRespondent2LiP()) {
            return isLipCaseWithProgressionEnabledAndCourtWhiteListed(caseData, epimmsId) ? YES : YesOrNo.NO;
        }
        log.info("Case {} is whitelisted for case progression.", caseData.getCcdCaseReference());
        return YES;
    }

    /**
     * Checks if a LiP case meets the requirements for case progression and location whitelisting.
     * These requirements include specific party representation combinations and feature toggle states.
     *
     * @param caseData the case data
     * @param newCourtLocation the epimmsId of the target location
     * @return true if the LiP case is eligible for case progression at the new location
     */
    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData, String newCourtLocation) {
        return (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne()
            || (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData)))
            && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(newCourtLocation);
    }

    /**
     * Validates whether the newly selected court location is different from the current case management location.
     *
     * @param callbackParams the callback parameters
     * @return true if the selected location is the same as the current location
     */
    private boolean isSameCourtSelected(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return Optional.ofNullable(courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams),
                caseData.getTransferCourtLocationList()
            ))
            .map(LocationRefData::getCourtLocationCode)
            .flatMap(newCode -> Optional.ofNullable(getCurrentLocation(callbackParams))
                .map(current -> newCode.equals(current.getCourtLocationCode())))
            .orElse(false);
    }

    /**
     * Retrieves the current case management location details from reference data.
     * Matches the epimmsId from the case data's base location against the available location reference data.
     *
     * @param callbackParams the callback parameters
     * @return the matching LocationRefData, or null if not found
     */
    private LocationRefData getCurrentLocation(CallbackParams callbackParams) {
        String baseLocation = Optional.ofNullable(callbackParams.getCaseData().getCaseManagementLocation())
            .map(uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil::getBaseLocation)
            .orElse(null);
        if (baseLocation == null) {
            return null;
        }
        return fetchLocationData(callbackParams).stream()
            .filter(loc -> loc.getEpimmsId().equals(baseLocation))
            .findFirst()
            .orElse(null);
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private DynamicList toDynamicList(final List<LocationRefData> locations) {
        return fromList(locations.stream()
                            .map(loc -> String.format("%s - %s - %s", loc.getSiteName(), loc.getCourtAddress(), loc.getPostcode()))
                            .toList());
    }
}
