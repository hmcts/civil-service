package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.GeneralAppLocationRefDataService;

import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateGaLocationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(TRIGGER_LOCATION_UPDATE, TRIGGER_TASK_RECONFIG);
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final GeneralAppLocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::updateCaseManagementLocation
        );
    }

    private LocationRefData getWorkAllocationLocationDetails(String baseLocation, String authToken) {
        List<LocationRefData> locationDetails = locationRefDataService.getCourtLocationsByEpimmsId(authToken, baseLocation);
        if (locationDetails != null && !locationDetails.isEmpty()) {
            return locationDetails.get(0);
        } else {
            return LocationRefData.builder().build();
        }
    }

    private CallbackResponse updateCaseManagementLocation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String parentCaseReference = caseData.getGeneralAppParentCaseLink() != null
                ? caseData.getGeneralAppParentCaseLink().getCaseReference() : caseData.getParentCaseReference();
        CaseData civilCaseData = caseDetailsConverter
                .toCaseDataGA(coreCaseDataService
                        .getCase(Long.parseLong(parentCaseReference)));
        CaseLocationCivil parentLocation = civilCaseData.getCaseManagementLocation();
        String parentBaseLocation = parentLocation != null ? parentLocation.getBaseLocation() : null;
        LocationRefData locationDetails = getWorkAllocationLocationDetails(parentBaseLocation, authToken);
        String parentRegion = parentLocation != null ? parentLocation.getRegion() : null;
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseLocationCivil updatedLocation = CaseLocationCivil.builder()
            .baseLocation(parentBaseLocation)
            .region(parentRegion)
            .siteName(locationDetails.getSiteName())
            .address(locationDetails.getCourtAddress())
            .postcode(locationDetails.getPostcode())
            .build();
        caseDataBuilder
                .businessProcess(
                        BusinessProcess.builder()
                                .camundaEvent(callbackParams.getRequest().getEventId())
                                .status(BusinessProcessStatus.FINISHED)
                                .build())
                .isCcmccLocation(YesOrNo.NO)
                .caseManagementLocation(updatedLocation)
                .locationName(civilCaseData.getLocationName());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }
}
