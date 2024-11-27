package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.CreateSDOCallbackHandlerUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.PrePopulateSdoR2AndNihlFields;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SmallClaimsPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.DisposalHearingPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages.OrderDetailsPagesCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle.SHOW;

@Component
@Slf4j
public class PrePopulateOrderDetailsPages implements CaseTask {

    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final SmallClaimsPopulator smallClaimsPopulator;
    private final CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;
    private final FastTrackPopulator fastTrackPopulator;
    private final DisposalHearingPopulator disposalHearingPopulator;
    private final PrePopulateSdoR2AndNihlFields prePopulateSdoR2AndNihlFields;
    private final BigDecimal ccmccAmount;
    private final String ccmccEpimsId;
    private final List<OrderDetailsPagesCaseFieldBuilder> orderDetailsPagesCaseFieldBuilders;

    public PrePopulateOrderDetailsPages(ObjectMapper objectMapper, LocationReferenceDataService locationRefDataService,
                                        FeatureToggleService featureToggleService,
                                        LocationHelper locationHelper, SmallClaimsPopulator smallClaimsPopulator,
                                        CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils,
                                        FastTrackPopulator fastTrackPopulator, DisposalHearingPopulator disposalHearingPopulator,
                                        PrePopulateSdoR2AndNihlFields prePopulateSdoR2AndNihlFields,
                                        @Value("${genApp.lrd.ccmcc.amountPounds}") BigDecimal ccmccAmount,
                                        @Value("${genApp.lrd.ccmcc.epimsId}") String ccmccEpimsId, List<OrderDetailsPagesCaseFieldBuilder> orderDetailsPagesCaseFieldBuilders
    ) {
        this.objectMapper = objectMapper;
        this.locationRefDataService = locationRefDataService;
        this.featureToggleService = featureToggleService;
        this.locationHelper = locationHelper;
        this.smallClaimsPopulator = smallClaimsPopulator;
        this.createSDOCallbackHandlerUtils = createSDOCallbackHandlerUtils;
        this.fastTrackPopulator = fastTrackPopulator;
        this.disposalHearingPopulator = disposalHearingPopulator;
        this.prePopulateSdoR2AndNihlFields = prePopulateSdoR2AndNihlFields;
        this.ccmccAmount = ccmccAmount;
        this.ccmccEpimsId = ccmccEpimsId;
        this.orderDetailsPagesCaseFieldBuilders = orderDetailsPagesCaseFieldBuilders;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing PrePopulateOrderDetailsPages");
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        try {
            log.debug("Setting initial order methods and CARM fields");
            updatedData
                    .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
                    .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson)
                    .showCarmFields(featureToggleService.isCarmEnabledForCase(caseData) ? YES : NO);

            log.debug("Updating case management location if Legal Advisor SDO");
            Optional<RequestedCourt> preferredCourt = updateCaseManagementLocationIfLegalAdvisorSdo(updatedData, caseData);

            log.debug("Fetching all location reference data");
            List<LocationRefData> locationRefDataList = getAllLocationFromRefData(callbackParams);

            log.debug("Creating dynamic locations list");
            DynamicList locationsList = createSDOCallbackHandlerUtils.getLocationList(
                    preferredCourt.orElse(null), false, locationRefDataList);
            setMethodInPerson(updatedData, locationsList);

            log.debug("Creating dynamic hearing method list");
            DynamicList hearingMethodList = createSDOCallbackHandlerUtils.getDynamicHearingMethodList(callbackParams, caseData);

            handleCallbackVersionIfNeeded(callbackParams, updatedData, hearingMethodList);

            log.debug("Setting checklist sections to SHOW");
            createSDOCallbackHandlerUtils.setCheckList(updatedData, List.of(SHOW));

            log.debug("Populating disposal hearing fields");
            disposalHearingPopulator.setDisposalHearingFields(updatedData, caseData);

            log.debug("Populating fast track fields");
            fastTrackPopulator.setFastTrackFields(updatedData);

            log.debug("Populating small claims fields");
            smallClaimsPopulator.setSmallClaimsFields(updatedData, caseData);

            handleSdoR2FeaturesIfNeeded(caseData, updatedData, callbackParams, preferredCourt, hearingMethodList, locationRefDataList);

            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .build();
        } catch (Exception e) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of("An unexpected error occurred while pre-populating order details."))
                    .data(updatedData.build().toMap(objectMapper))
                    .build();
        }
    }

    private void handleCallbackVersionIfNeeded(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData, DynamicList hearingMethodList) {
        if (V_1.equals(callbackParams.getVersion())) {
            log.debug("Setting hearing method values based on callback version");
            setHearingMethodValues(updatedData, hearingMethodList);
        }
    }

    private void handleSdoR2FeaturesIfNeeded(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams,
                                             Optional<RequestedCourt> preferredCourt, DynamicList hearingMethodList, List<LocationRefData> locationRefDataList) {
        if (featureToggleService.isSdoR2Enabled()
                && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
                && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(
                caseData.getDecisionOnRequestReconsiderationOptions())) {
            log.debug("Resetting fields for reconsideration");
            orderDetailsPagesCaseFieldBuilders.forEach(builder -> builder.build(updatedData));
        }

        if (featureToggleService.isSdoR2Enabled()) {

            orderDetailsPagesCaseFieldBuilders.forEach(builder -> builder.build(updatedData));

            log.debug("Populating DRH fields");
            prePopulateSdoR2AndNihlFields.populateDRHFields(callbackParams, updatedData, preferredCourt, hearingMethodList, locationRefDataList);

            log.debug("Populating NIHL fields");
            prePopulateSdoR2AndNihlFields.prePopulateNihlFields(updatedData, hearingMethodList, preferredCourt, locationRefDataList);

            log.debug("Setting checklist for NIHL inclusion");
            setCheckListNihl(updatedData, List.of(IncludeInOrderToggle.INCLUDE));
        }
    }

    private void setHearingMethodValues(CaseData.CaseDataBuilder<?, ?> updatedData,
                                        DynamicList hearingMethodList) {
        log.debug("Setting hearing method values to IN_PERSON");
        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream()
                .filter(elem -> elem.getLabel().equals(HearingMethod.IN_PERSON.getLabel()))
                .findFirst()
                .orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
        updatedData.hearingMethodValuesFastTrack(hearingMethodList);
        updatedData.hearingMethodValuesDisposalHearing(hearingMethodList);
        updatedData.hearingMethodValuesSmallClaims(hearingMethodList);
    }

    private void setMethodInPerson(CaseData.CaseDataBuilder<?, ?> updatedData,
                                   DynamicList locationsList) {
        log.debug("Setting method to IN_PERSON for disposal hearing, fast track, and small claims");
        updatedData.disposalHearingMethodInPerson(locationsList);
        updatedData.fastTrackMethodInPerson(locationsList);
        updatedData.smallClaimsMethodInPerson(locationsList);
    }

    private List<LocationRefData> getAllLocationFromRefData(CallbackParams callbackParams) {
        log.debug("Fetching hearing court locations from reference data service");
        return locationRefDataService.getHearingCourtLocations(
                callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private void setCheckListNihl(
            CaseData.CaseDataBuilder<?, ?> updatedData,
            List<IncludeInOrderToggle> includeInOrderToggle
    ) {
        log.debug("Setting checklist toggles for NIHL inclusion");
        updatedData.sdoAltDisputeResolution(SdoR2FastTrackAltDisputeResolution.builder().includeInOrderToggle(
                includeInOrderToggle).build());
        updatedData.sdoVariationOfDirections(SdoR2VariationOfDirections.builder().includeInOrderToggle(
                includeInOrderToggle).build());
        updatedData.sdoR2Settlement(SdoR2Settlement.builder().includeInOrderToggle(includeInOrderToggle).build());
        updatedData.sdoR2DisclosureOfDocumentsToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorWitnessesOfFactToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorExpertEvidenceToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorAddendumReportToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorFurtherAudiogramToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorQuestionsClaimantExpertToggle(includeInOrderToggle).build();
        updatedData.sdoR2SeparatorPermissionToRelyOnExpertToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorEvidenceAcousticEngineerToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorQuestionsToEntExpertToggle(includeInOrderToggle);
        updatedData.sdoR2ScheduleOfLossToggle(includeInOrderToggle);
        updatedData.sdoR2SeparatorUploadOfDocumentsToggle(includeInOrderToggle);
        updatedData.sdoR2TrialToggle(includeInOrderToggle);
        if (featureToggleService.isCarmEnabledForCase(updatedData.build())) {
            updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
        }
        log.debug("Checklist toggles set successfully for NIHL inclusion");
    }

    private Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        log.debug("Checking if case qualifies for Legal Advisor SDO based on claim amount and location");
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            log.debug("Case qualifies for Legal Advisor SDO, fetching preferred court");
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData, true);
            preferredCourt.map(RequestedCourt::getCaseLocation)
                    .ifPresent(location -> {
                        updatedData.caseManagementLocation(location);
                        log.debug("Case management location set to: {}", location);
                    });
            return preferredCourt;
        } else {
            log.debug("Case does not qualify for Legal Advisor SDO, fetching standard case management location");
            return locationHelper.getCaseManagementLocation(caseData);
        }
    }

    public Predicate<CaseData> isSpecClaim1000OrLessAndCcmcc(BigDecimal ccmccAmount) {
        return caseData ->
                caseData.getCaseAccessCategory().equals(CaseCategory.SPEC_CLAIM)
                        && ccmccAmount.compareTo(caseData.getTotalClaimAmount()) >= 0
                        && caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimsId);
    }
}
