package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.DisposalHearingPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackPopulator;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle.SHOW;

@Component
public class PrePopulateOrderDetailsPages implements CaseTask {

    private static final Logger logger = LoggerFactory.getLogger(PrePopulateOrderDetailsPages.class);

    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private final WorkingDayIndicator workingDayIndicator;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final SmallClaimsPopulator smallClaimsPopulator;
    private final CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;
    private final FastTrackPopulator fastTrackPopulator;
    private final DisposalHearingPopulator disposalHearingPopulator;
    private final PrePopulateSdoR2AndNihlFields prePopulateSdoR2AndNihlFields;
    private final BigDecimal ccmccAmount;
    private final String ccmccEpimsId;

    public PrePopulateOrderDetailsPages(ObjectMapper objectMapper, LocationReferenceDataService locationRefDataService,
                                        WorkingDayIndicator workingDayIndicator, FeatureToggleService featureToggleService,
                                        LocationHelper locationHelper, SmallClaimsPopulator smallClaimsPopulator,
                                        CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils,
                                        FastTrackPopulator fastTrackPopulator, DisposalHearingPopulator disposalHearingPopulator,
                                        PrePopulateSdoR2AndNihlFields prePopulateSdoR2AndNihlFields,
                                        @Value("${genApp.lrd.ccmcc.amountPounds}") BigDecimal ccmccAmount,
                                        @Value("${genApp.lrd.ccmcc.epimsId}") String ccmccEpimsId
    ) {
        this.objectMapper = objectMapper;
        this.locationRefDataService = locationRefDataService;
        this.workingDayIndicator = workingDayIndicator;
        this.featureToggleService = featureToggleService;
        this.locationHelper = locationHelper;
        this.smallClaimsPopulator = smallClaimsPopulator;
        this.createSDOCallbackHandlerUtils = createSDOCallbackHandlerUtils;
        this.fastTrackPopulator = fastTrackPopulator;
        this.disposalHearingPopulator = disposalHearingPopulator;
        this.prePopulateSdoR2AndNihlFields = prePopulateSdoR2AndNihlFields;
        this.ccmccAmount = ccmccAmount;
        this.ccmccEpimsId = ccmccEpimsId;
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        logger.info("Executing PrePopulateOrderDetailsPages");
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        try {
            logger.debug("Setting initial order methods and CARM fields");
            updatedData
                    .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
                    .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson)
                    .showCarmFields(featureToggleService.isCarmEnabledForCase(caseData) ? YES : NO);

            logger.debug("Updating case management location if Legal Advisor SDO");
            Optional<RequestedCourt> preferredCourt = updateCaseManagementLocationIfLegalAdvisorSdo(updatedData, caseData);

            logger.debug("Fetching all location reference data");
            List<LocationRefData> locationRefDataList = getAllLocationFromRefData(callbackParams);

            logger.debug("Creating dynamic locations list");
            DynamicList locationsList = createSDOCallbackHandlerUtils.getLocationList(
                    preferredCourt.orElse(null), false, locationRefDataList);
            setMethodInPerson(updatedData, locationsList);

            logger.debug("Creating dynamic hearing method list");
            DynamicList hearingMethodList = createSDOCallbackHandlerUtils.getDynamicHearingMethodList(callbackParams, caseData);

            handleCallbackVersionIfNeeded(callbackParams, updatedData, hearingMethodList);

            logger.debug("Setting checklist sections to SHOW");
            createSDOCallbackHandlerUtils.setCheckList(updatedData, List.of(SHOW));

            logger.debug("Populating disposal hearing fields");
            disposalHearingPopulator.setDisposalHearingFields(updatedData, caseData);

            logger.debug("Populating fast track fields");
            fastTrackPopulator.setFastTrackFields(updatedData);

            logger.debug("Populating small claims fields");
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
            logger.debug("Setting hearing method values based on callback version");
            setHearingMethodValues(updatedData, hearingMethodList);
        }
    }

    private void handleSdoR2FeaturesIfNeeded(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, CallbackParams callbackParams,
                                             Optional<RequestedCourt> preferredCourt, DynamicList hearingMethodList, List<LocationRefData> locationRefDataList) {
        if (featureToggleService.isSdoR2Enabled()
                && CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
                && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(
                caseData.getDecisionOnRequestReconsiderationOptions())) {
            logger.debug("Resetting fields for reconsideration");
            resetFieldsForReconsideration(updatedData);
        }

        if (featureToggleService.isSdoR2Enabled()) {
            logger.debug("Updating expert evidence fields");
            updateExpertEvidenceFields(updatedData);

            logger.debug("Updating disclosure of document fields");
            updateDisclosureOfDocumentFields(updatedData);

            logger.debug("Populating DRH fields");
            prePopulateSdoR2AndNihlFields.populateDRHFields(callbackParams, updatedData, preferredCourt, hearingMethodList, locationRefDataList);

            logger.debug("Populating NIHL fields");
            prePopulateSdoR2AndNihlFields.prePopulateNihlFields(updatedData, hearingMethodList, preferredCourt, locationRefDataList);

            logger.debug("Setting checklist for NIHL inclusion");
            setCheckListNihl(updatedData, List.of(IncludeInOrderToggle.INCLUDE));

            logger.debug("Setting Welsh language usage fields");
            setWelshLanguageUsage(updatedData);
        }
    }

    private void setHearingMethodValues(CaseData.CaseDataBuilder<?, ?> updatedData,
                                        DynamicList hearingMethodList) {
        logger.debug("Setting hearing method values to IN_PERSON");
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
        logger.debug("Setting method to IN_PERSON for disposal hearing, fast track, and small claims");
        updatedData.disposalHearingMethodInPerson(locationsList);
        updatedData.fastTrackMethodInPerson(locationsList);
        updatedData.smallClaimsMethodInPerson(locationsList);
    }

    private void resetFieldsForReconsideration(CaseData.CaseDataBuilder<?, ?> updatedData) {
        logger.debug("Resetting fields related to reconsideration");
        updatedData.drawDirectionsOrderRequired(null);
        updatedData.drawDirectionsOrderSmallClaims(null);
        updatedData.fastClaims(null);
        updatedData.smallClaims(null);
        updatedData.claimsTrack(null);
        updatedData.orderType(null);
        updatedData.trialAdditionalDirectionsForFastTrack(null);
        updatedData.drawDirectionsOrderSmallClaimsAdditionalDirections(null);
        updatedData.fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(null).build());
        updatedData.disposalHearingAddNewDirections(null);
        updatedData.smallClaimsAddNewDirections(null);
        updatedData.fastTrackAddNewDirections(null);
        updatedData.sdoHearingNotes(null);
        updatedData.fastTrackHearingNotes(null);
        updatedData.disposalHearingHearingNotes(null);
        updatedData.sdoR2SmallClaimsHearing(null);
        updatedData.sdoR2SmallClaimsUploadDoc(null);
        updatedData.sdoR2SmallClaimsPPI(null);
        updatedData.sdoR2SmallClaimsImpNotes(null);
        updatedData.sdoR2SmallClaimsWitnessStatements(null);
        updatedData.sdoR2SmallClaimsHearingToggle(null);
        updatedData.sdoR2SmallClaimsJudgesRecital(null);
        updatedData.sdoR2SmallClaimsWitnessStatementsToggle(null);
        updatedData.sdoR2SmallClaimsPPIToggle(null);
        updatedData.sdoR2SmallClaimsUploadDocToggle(null);
    }

    private void setWelshLanguageUsage(CaseData.CaseDataBuilder<?, ?> updatedData) {
        logger.debug("Setting Welsh language usage descriptions");
        updatedData.sdoR2FastTrackUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
        updatedData.sdoR2SmallClaimsUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
        updatedData.sdoR2DisposalHearingUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
    }

    private List<LocationRefData> getAllLocationFromRefData(CallbackParams callbackParams) {
        logger.debug("Fetching hearing court locations from reference data service");
        return locationRefDataService.getHearingCourtLocations(
                callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private void updateExpertEvidenceFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        logger.debug("Updating expert evidence fields with calculated dates");
        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
                .input1("The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim")
                .input2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert " +
                        "directly and uploaded to the Digital Portal by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(14)))
                .input3("The answers to the questions shall be answered by the Expert by")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(42)))
                .input4("and uploaded to the Digital Portal by the party who has asked the question by")
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(49)))
                .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();
        logger.debug("Expert evidence fields updated successfully");
    }

    private void updateDisclosureOfDocumentFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        logger.debug("Updating disclosure of document fields with calculated dates");
        FastTrackDisclosureOfDocuments tempFastTrackDisclosureOfDocuments = FastTrackDisclosureOfDocuments.builder()
                .input1("Standard disclosure shall be provided by the parties by uploading to the Digital Portal their "
                        + "list of documents by 4pm on")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input2("Any request to inspect a document, or for a copy of a document, shall be made directly to "
                        + "the other party by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(5)))
                .input3("Requests will be complied with within 7 days of the receipt of the request.")
                .input4("Each party must upload to the Digital Portal copies of those documents on which they wish to"
                        + " rely at trial by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .build();

        updatedData.fastTrackDisclosureOfDocuments(tempFastTrackDisclosureOfDocuments).build();
        logger.debug("Disclosure of document fields updated successfully");
    }

    private void setCheckListNihl(
            CaseData.CaseDataBuilder<?, ?> updatedData,
            List<IncludeInOrderToggle> includeInOrderToggle
    ) {
        logger.debug("Setting checklist toggles for NIHL inclusion");
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
        logger.debug("Checklist toggles set successfully for NIHL inclusion");
    }

    private Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        logger.debug("Checking if case qualifies for Legal Advisor SDO based on claim amount and location");
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            logger.debug("Case qualifies for Legal Advisor SDO, fetching preferred court");
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData, true);
            preferredCourt.map(RequestedCourt::getCaseLocation)
                    .ifPresent(location -> {
                        updatedData.caseManagementLocation(location);
                        logger.debug("Case management location set to: {}", location);
                    });
            return preferredCourt;
        } else {
            logger.debug("Case does not qualify for Legal Advisor SDO, fetching standard case management location");
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
