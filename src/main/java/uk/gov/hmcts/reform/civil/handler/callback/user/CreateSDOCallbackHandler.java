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
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.pipeline.SdoCallbackPipeline;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskResult;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoValidationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_SDO);
    private static final String HEARING_CHANNEL = "HearingChannel";
    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";
    private static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    public static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";


    public static final String ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE = "Date must be in the future";
    public static final String ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO = "The number entered cannot be less than zero";
    public static final String ERROR_MINTI_DISPOSAL_NOT_ALLOWED = "Disposal Hearing is not available for Multi Track and Intermediate Track Claims. "
        + "This can be requested by using the Make an Order event.";

    private final ObjectMapper objectMapper;
    private final WorkingDayIndicator workingDayIndicator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final SdoGeneratorService sdoGeneratorService;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final AssignCategoryId assignCategoryId;
    private final SdoLocationService sdoLocationService;
    private final SdoValidationService sdoValidationService;
    private final SdoCallbackPipeline sdoCallbackPipeline;
    private final CategoryService categoryService;
    private final List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
    private final List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
    static final String witnessStatementString = "This witness statement is limited to 10 pages per party, including any appendices.";
    static final String laterThanFourPmString = "later than 4pm on";
    static final String claimantEvidenceString = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";
    @Value("${genApp.lrd.ccmcc.amountPounds}")
    BigDecimal ccmccAmount;
    @Value("${court-location.unspecified-claim.epimms-id}")
    String ccmccEpimsId;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(V_1, ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
            .put(callbackKey(MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(V_1, MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // This is currently a mid event but once pre states are defined it should be moved to an about to start event.
    // Once it has been moved to an about to start event the following file will need to be updated:
    //  FlowStateAllowedEventService.java.
    // This way pressing previous on the ccd page won't end up calling this method again and thus
    // repopulating the fields if they have been changed.
    // There is no reason to add conditionals to avoid this here since having it as an about to start event will mean
    // it is only ever called once.
    // Then any changes to fields in ccd will persist in ccd regardless of backwards or forwards page navigation.
    private CallbackResponse prePopulateOrderDetailsPages(CallbackParams callbackParams) {
        CaseData originalCaseData = callbackParams.getCaseData();
        SdoTaskContext prePopulateContext = new SdoTaskContext(
            originalCaseData,
            callbackParams,
            SdoLifecycleStage.PRE_POPULATE
        );
        SdoTaskResult prePopulateResult = sdoCallbackPipeline.run(prePopulateContext, SdoLifecycleStage.PRE_POPULATE);
        List<String> prePopulateErrors = prePopulateResult.errors() == null
            ? Collections.emptyList()
            : prePopulateResult.errors();
        CaseData caseData = prePopulateResult.updatedCaseData() != null
            ? prePopulateResult.updatedCaseData()
            : originalCaseData;

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder responseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper));

        if (!prePopulateErrors.isEmpty()) {
            responseBuilder.errors(prePopulateErrors);
        }

        return responseBuilder.build();
    }

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue);

                FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();

                SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();

                updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
            });
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        if (isMultiOrIntermediateTrackClaim(caseData)
            && OrderType.DISPOSAL.equals(caseData.getOrderType())
            && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED))
                .build();
        }

        updateDeductionValue(caseData, updatedData);
        updatedData.setSmallClaimsFlag(NO).build();
        updatedData.setFastTrackFlag(NO).build();
        updatedData.isSdoR2NewScreen(NO).build();

        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            updatedData.setSmallClaimsFlag(YES).build();
            if (SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
                updatedData.isSdoR2NewScreen(YES).build();
            }
        } else if (SdoHelper.isFastTrack(caseData)) {
            updatedData.setFastTrackFlag(YES).build();
            if (SdoHelper.isNihlFastTrack(caseData)) {
                updatedData.isSdoR2NewScreen(YES).build();
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        log.info("generateSdoOrder ccdCaseReference: {} legacyCaseReference: {}",
                 callbackParams.getCaseData().getCcdCaseReference(), callbackParams.getCaseData().getLegacyCaseReference());

        CaseData initialCaseData = V_1.equals(callbackParams.getVersion())
            ? mapHearingMethodFields(callbackParams.getCaseData())
            : callbackParams.getCaseData();

        SdoTaskContext validationContext = new SdoTaskContext(initialCaseData, callbackParams, SdoLifecycleStage.MID_EVENT);
        SdoTaskResult validationResult = sdoCallbackPipeline.run(validationContext, SdoLifecycleStage.MID_EVENT);

        List<String> validationErrors = validationResult.errors() == null
            ? Collections.emptyList()
            : validationResult.errors();
        CaseData caseDataAfterValidation = validationResult.updatedCaseData() != null
            ? validationResult.updatedCaseData()
            : initialCaseData;

        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(validationErrors)
                .data(caseDataAfterValidation.toMap(objectMapper))
                .build();
        }

        SdoTaskContext documentContext = new SdoTaskContext(
            caseDataAfterValidation,
            callbackParams,
            SdoLifecycleStage.DOCUMENT_GENERATION
        );
        SdoTaskResult documentResult = sdoCallbackPipeline.run(documentContext, SdoLifecycleStage.DOCUMENT_GENERATION);

        CaseData finalCaseData = documentResult.updatedCaseData() != null
            ? documentResult.updatedCaseData()
            : caseDataAfterValidation;

        List<String> documentErrors = documentResult.errors() == null
            ? Collections.emptyList()
            : documentResult.errors();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(documentErrors)
            .data(finalCaseData.toMap(objectMapper))
            .build();
    }

    private CaseData mapHearingMethodFields(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (caseData.getHearingMethodValuesDisposalHearing() != null
            && caseData.getHearingMethodValuesDisposalHearing().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearing().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesFastTrack() != null
            && caseData.getHearingMethodValuesFastTrack().getValue() != null) {
            String fastTrackHearingMethodLabel = caseData.getHearingMethodValuesFastTrack().getValue().getLabel();
            if (fastTrackHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesSmallClaims() != null
            && caseData.getHearingMethodValuesSmallClaims().getValue() != null) {
            String smallClaimsHearingMethodLabel = caseData.getHearingMethodValuesSmallClaims().getValue().getLabel();
            if (smallClaimsHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
            }
        }

        return updatedData.build();
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskContext submissionContext = new SdoTaskContext(caseData, callbackParams, SdoLifecycleStage.SUBMISSION);
        SdoTaskResult submissionResult = sdoCallbackPipeline.run(submissionContext, SdoLifecycleStage.SUBMISSION);

        List<String> submissionErrors = submissionResult.errors() == null
            ? Collections.emptyList()
            : submissionResult.errors();
        if (!submissionErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(submissionErrors)
                .build();
        }

        CaseData updatedCaseData = submissionResult.updatedCaseData() != null
            ? submissionResult.updatedCaseData()
            : caseData;

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(updatedCaseData)) {
            CaseData.CaseDataBuilder<?, ?> waUpdateBuilder = updatedCaseData.toBuilder();
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                waUpdateBuilder
            ));
            updatedCaseData = waUpdateBuilder.build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        SdoTaskContext confirmationContext = new SdoTaskContext(caseData, callbackParams, SdoLifecycleStage.CONFIRMATION);
        SdoTaskResult confirmationResult = sdoCallbackPipeline.run(confirmationContext, SdoLifecycleStage.CONFIRMATION);

        if (confirmationResult.submittedCallbackResponse() != null) {
            return confirmationResult.submittedCallbackResponse();
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void setCheckList(
        CaseData.CaseDataBuilder<?, ?> updatedData,
        List<OrderDetailsPagesSectionsToggle> checkList
    ) {
        updatedData.fastTrackAltDisputeResolutionToggle(checkList);
        updatedData.fastTrackVariationOfDirectionsToggle(checkList);
        updatedData.fastTrackSettlementToggle(checkList);
        updatedData.fastTrackDisclosureOfDocumentsToggle(checkList);
        updatedData.fastTrackWitnessOfFactToggle(checkList);
        updatedData.fastTrackSchedulesOfLossToggle(checkList);
        updatedData.fastTrackCostsToggle(checkList);
        updatedData.fastTrackTrialToggle(checkList);
        updatedData.fastTrackTrialBundleToggle(checkList);
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
        updatedData.smallClaimsFlightDelayToggle(checkList);

        if (featureToggleService.isCarmEnabledForCase(updatedData.build())) {
            updatedData.smallClaimsMediationSectionToggle(checkList);
        }
    }

    private void setCheckListNihl(
        CaseData.CaseDataBuilder<?, ?> updatedData,
        List<IncludeInOrderToggle> includeInOrderToggle
    ) {
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
    }

    private Optional<RequestedCourt> updateCaseManagementLocationIfLegalAdvisorSdo(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        Optional<RequestedCourt> preferredCourt;
        if (isSpecClaim1000OrLessAndCcmcc(ccmccAmount).test(caseData)) {
            preferredCourt = locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData);
            preferredCourt.map(RequestedCourt::getCaseLocation)
                .ifPresent(updatedData::caseManagementLocation);
            return preferredCourt;
        } else {
            return locationHelper.getCaseManagementLocation(caseData);
        }
    }

    public Predicate<CaseData> isSpecClaim1000OrLessAndCcmcc(BigDecimal ccmccAmount) {
        return caseData ->
            caseData.getCaseAccessCategory().equals(CaseCategory.SPEC_CLAIM)
                && ccmccAmount.compareTo(caseData.getTotalClaimAmount()) >= 0
                && caseData.getCaseManagementLocation().getBaseLocation().equals(ccmccEpimsId);
    }

    private boolean isMultiOrIntermediateTrackClaim(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && (AllocatedTrack.INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())
            || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

}
