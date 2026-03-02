package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

@Service
@RequiredArgsConstructor
public class SdoHearingPreparationService {

    private static final String HEARING_CHANNEL = "HearingChannel";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";

    private final LocationHelper locationHelper;
    private final SdoLocationService sdoLocationService;
    private final CategoryService categoryService;

    @Value("${genApp.lrd.ccmcc.amountPounds}")
    BigDecimal ccmccAmount;
    @Value("${court-location.unspecified-claim.epimms-id}")
    String ccmccEpimsId;

    public Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(CaseData caseData, String authToken) {
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData);
            preferredCourt.map(RequestedCourt::getCaseLocation)
                .ifPresent(caseData::setCaseManagementLocation);
            preferredCourt
                .map(RequestedCourt::getCaseLocation)
                .map(CaseLocationCivil::getBaseLocation)
                .map(epimmsId -> sdoLocationService.fetchCourtLocationsByEpimmsId(authToken, epimmsId))
                .map(list -> Optional.of(list).orElse(Collections.emptyList()))
                .flatMap(locations -> locations.stream().findFirst())
                .ifPresent(locationRefData -> caseData.setLocationName(locationRefData.getSiteName()));
            return preferredCourt;
        } else {
            return locationHelper.getCaseManagementLocation(caseData);
        }
    }

    public DynamicList getDynamicHearingMethodList(CallbackParams callbackParams, CaseData caseData) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String serviceId = CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID;
        Optional<uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult> categorySearchResult =
            categoryService.findCategoryByCategoryIdAndServiceId(authToken, HEARING_CHANNEL, serviceId);
        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult.orElse(null));
        List<DynamicListElement> filteredItems = hearingMethodList.getListItems().stream()
            .filter(elem -> !HearingMethod.NOT_IN_ATTENDANCE.getLabel().equals(elem.getLabel()))
            .toList();
        hearingMethodList.setListItems(filteredItems);
        return hearingMethodList;
    }

    public void applyVersionSpecificHearingDefaults(
        CallbackParams callbackParams,
        DynamicList hearingMethodList
    ) {
        CallbackVersion version = callbackParams.getVersion();
        if (V_1.equals(version)) {
            DynamicListElement inPerson = hearingMethodList.getListItems().stream()
                .filter(elem -> HearingMethod.IN_PERSON.getLabel().equals(elem.getLabel()))
                .findFirst()
                .orElse(null);
            hearingMethodList.setValue(inPerson);
            CaseData caseData = callbackParams.getCaseData();
            caseData.setHearingMethodValuesFastTrack(hearingMethodList);
            caseData.setHearingMethodValuesDisposalHearing(hearingMethodList);
            caseData.setHearingMethodValuesSmallClaims(hearingMethodList);
        }
    }

    public List<LocationRefData> populateHearingLocations(
        Optional<RequestedCourt> preferredCourt,
        String authToken,
        CaseData caseData
    ) {
        List<LocationRefData> locationRefDataList = sdoLocationService.fetchHearingLocations(authToken);
        DynamicList locationsList;
        if (caseData.getReasonForTransfer() != null && caseData.getTransferCourtLocationList() != null) {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setCaseLocation(caseData.getCaseManagementLocation());
            Optional<RequestedCourt> optionalRequestedCourt = Optional.of(requestedCourt);
            locationsList = sdoLocationService.buildLocationList(
                optionalRequestedCourt.orElse(null),
                false,
                locationRefDataList
            );
        } else {
            locationsList = sdoLocationService.buildLocationList(
                preferredCourt.orElse(null),
                false,
                locationRefDataList
            );
        }
        caseData.setDisposalHearingMethodInPerson(locationsList);
        caseData.setFastTrackMethodInPerson(locationsList);
        caseData.setSmallClaimsMethodInPerson(locationsList);
        return locationRefDataList;
    }

    private Predicate<CaseData> isSpecClaim1000OrLessAndCcmcc(BigDecimal threshold) {
        return caseData ->
            CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && threshold.compareTo(caseData.getTotalClaimAmount()) >= 0
                && caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimsId);
    }
}
