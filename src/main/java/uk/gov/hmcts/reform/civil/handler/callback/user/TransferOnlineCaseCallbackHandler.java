package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
            .put(callbackKey(ABOUT_TO_START), this::locationList)
            .put(callbackKey(MID, "validate-court-location"), this::validateCourtLocation)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveTransferOnlineCase)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateCourtLocation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (ifSameCourtSelected(callbackParams)) {
            errors.add(ERROR_SELECT_DIFF_LOCATION);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String newCourtLocationSiteName = courtLocationUtils.findPreferredLocationData(
            fetchLocationData(callbackParams),
            callbackParams.getCaseData().getTransferCourtLocationList()
        ).getSiteName();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(getBody(newCourtLocationSiteName))
            .build();
    }

    private CallbackResponse locationList(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<LocationRefData> locations = fetchLocationData(callbackParams);
        caseData.setTransferCourtLocationList(getLocationsFromList(locations));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> location.getSiteName() +
                " - " + location.getCourtAddress() +
                " - " + location.getPostcode())
                            .toList());
    }

    private String getBody(String siteName) {
        return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
            + "The case has now been transferred to "
            + siteName
            + ". If the case has moved out of your region, you will no longer see it.<br><br>";
    }

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

        caseData.getTransferCourtLocationList().setListItems(null);
        caseData.setBusinessProcess(BusinessProcess.ready(TRIGGER_TASK_RECONFIG_GA));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

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

    private boolean isLipCaseWithProgressionEnabledAndCourtWhiteListed(CaseData caseData, String newCourtLocation) {
        return (caseData.isLipvLipOneVOne() || caseData.isLRvLipOneVOne()
            || (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData)))
            && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(newCourtLocation);
    }

    private boolean ifSameCourtSelected(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return Optional.ofNullable(courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams),
                caseData.getTransferCourtLocationList()
            ))
            .map(LocationRefData::getCourtLocationCode)
            .flatMap(newCode -> Optional.ofNullable(getLocationRefData(callbackParams))
                .map(current -> newCode.equals(current.getCourtLocationCode())))
            .orElse(false);
    }

    private LocationRefData getLocationRefData(CallbackParams callbackParams) {
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

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
