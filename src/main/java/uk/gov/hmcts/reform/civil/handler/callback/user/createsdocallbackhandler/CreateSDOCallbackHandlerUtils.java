package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.HEARING_CHANNEL_SDO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.SPEC_SERVICE_ID;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.UNSPEC_SERVICE_ID;

@Component
@RequiredArgsConstructor
public class CreateSDOCallbackHandlerUtils {

    private static final Logger logger = LoggerFactory.getLogger(CreateSDOCallbackHandlerUtils.class);

    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final CategoryService categoryService;

    public DynamicList getLocationList(RequestedCourt preferredCourt, boolean getAllCourts, List<LocationRefData> locations) {
        logger.info("Getting location list with preferredCourt: {}, getAllCourts: {}", preferredCourt, getAllCourts);
        Optional<LocationRefData> matchingLocation = getMatchingLocation(preferredCourt, getAllCourts, locations);
        DynamicList dynamicList = createDynamicList(locations, matchingLocation);
        logger.info("Location list created with {} items", dynamicList.getListItems().size());
        return dynamicList;
    }

    private Optional<LocationRefData> getMatchingLocation(RequestedCourt preferredCourt, boolean getAllCourts, List<LocationRefData> locations) {
        if (featureToggleService.isSdoR2Enabled() && getAllCourts) {
            logger.debug("SDO R2 is enabled and getAllCourts is true, returning empty matching location");
            return Optional.empty();
        }
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
                .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));
        logger.debug("Matching location found: {}", matchingLocation.orElse(null));
        return matchingLocation;
    }

    private DynamicList createDynamicList(List<LocationRefData> locations, Optional<LocationRefData> matchingLocation) {
        DynamicList dynamicList = DynamicList.fromList(locations,
                this::getLocationEpimms,
                LocationReferenceDataService::getDisplayEntry,
                matchingLocation.orElse(null),
                true);
        logger.debug("Dynamic list created with matching location: {}", matchingLocation.orElse(null));
        return dynamicList;
    }

    public DynamicList getDynamicHearingMethodList(CallbackParams callbackParams, CaseData caseData) {
        logger.info("Getting dynamic hearing method list for caseData: {}", caseData);
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String serviceId = caseData.getCaseAccessCategory().equals(CaseCategory.SPEC_CLAIM)
                ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID;
        Optional<CategorySearchResult> categorySearchResult = categoryService.findCategoryByCategoryIdAndServiceId(
                authToken, HEARING_CHANNEL_SDO, serviceId
        );
        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult.orElse(null));
        List<DynamicListElement> hearingMethodListWithoutNotInAttendance = hearingMethodList
                .getListItems()
                .stream()
                .filter(elem -> !elem.getLabel().equals(HearingMethod.NOT_IN_ATTENDANCE.getLabel()))
                .toList();
        hearingMethodList.setListItems(hearingMethodListWithoutNotInAttendance);
        logger.info("Dynamic hearing method list created with {} items", hearingMethodList.getListItems().size());
        return hearingMethodList;
    }

    public void setCheckList(
            CaseData.CaseDataBuilder<?, ?> updatedData,
            List<OrderDetailsPagesSectionsToggle> checkList
    ) {
        logger.info("Setting checklist for caseData: {}", updatedData.build());
        updatedData.fastTrackAltDisputeResolutionToggle(checkList);
        updatedData.fastTrackVariationOfDirectionsToggle(checkList);
        updatedData.fastTrackSettlementToggle(checkList);
        updatedData.fastTrackDisclosureOfDocumentsToggle(checkList);
        updatedData.fastTrackWitnessOfFactToggle(checkList);
        updatedData.fastTrackSchedulesOfLossToggle(checkList);
        updatedData.fastTrackCostsToggle(checkList);
        updatedData.fastTrackTrialToggle(checkList);
        updatedData.fastTrackMethodToggle(checkList);
        updatedData.disposalHearingDisclosureOfDocumentsToggle(checkList);
        updatedData.disposalHearingWitnessOfFactToggle(checkList);
        updatedData.disposalHearingMedicalEvidenceToggle(checkList);
        updatedData.disposalHearingQuestionsToExpertsToggle(checkList);
        updatedData.disposalHearingSchedulesOfLossToggle(checkList);
        updatedData.disposalHearingFinalDisposalHearingToggle(checkList);
        updatedData.disposalHearingMethodToggle(checkList);
        updatedData.disposalHearingBundleToggle(checkList);
        updatedData.disposalHearingClaimSettlingToggle(checkList);
        updatedData.disposalHearingCostsToggle(checkList);
        updatedData.smallClaimsHearingToggle(checkList);
        updatedData.smallClaimsMethodToggle(checkList);
        updatedData.smallClaimsDocumentsToggle(checkList);
        updatedData.smallClaimsWitnessStatementToggle(checkList);
        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.smallClaimsFlightDelayToggle(checkList);
        }
        if (featureToggleService.isCarmEnabledForCase(updatedData.build())) {
            updatedData.smallClaimsMediationSectionToggle(checkList);
        }
        logger.info("Checklist set for caseData: {}", updatedData.build());
    }

    private String getLocationEpimms(LocationRefData location) {
        return location.getEpimmsId();
    }
}
