package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoPrePopulateService {

    private final SdoTrackDefaultsService sdoTrackDefaultsService;
    private final SdoHearingPreparationService sdoHearingPreparationService;
    private final SdoDrhFieldsService sdoDrhFieldsService;
    private final SdoNihlFieldsService sdoNihlFieldsService;

    public CaseData prePopulate(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        CallbackParams callbackParams = context.callbackParams();
        log.info("Pre-populating SDO defaults for caseId {}", caseData.getCcdCaseReference());
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        sdoTrackDefaultsService.applyBaseTrackDefaults(caseData, updatedData);

        Optional<RequestedCourt> preferredCourt = sdoHearingPreparationService
            .updateCaseManagementLocationIfLegalAdvisorSdo(updatedData, caseData);

        DynamicList hearingMethodList = sdoHearingPreparationService.getDynamicHearingMethodList(callbackParams, caseData);
        sdoHearingPreparationService.applyVersionSpecificHearingDefaults(callbackParams, updatedData, hearingMethodList);

        List<LocationRefData> locationRefDataList = sdoHearingPreparationService.populateHearingLocations(
            preferredCourt, authToken, updatedData);

        sdoDrhFieldsService.populateDrhFields(caseData, updatedData, preferredCourt, hearingMethodList, locationRefDataList);
        sdoNihlFieldsService.populateNihlFields(updatedData, hearingMethodList, preferredCourt, locationRefDataList);

        sdoTrackDefaultsService.applyR2Defaults(caseData, updatedData);
        return updatedData.build();
    }
}
