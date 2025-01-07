package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToApplicantExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.PersistDataUtils.persistFlagsForParties;
import static uk.gov.hmcts.reform.civil.utils.PersistDataUtils.persistPartyAddress;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToApplicantWitnesses;

@Component
@RequiredArgsConstructor
@Slf4j
public class AboutToSubmitRespondToDefenceTask implements CaseTask {

    private final ObjectMapper objectMapper;
    private final Time time;
    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FrcDocumentsUtils frcDocumentsUtils;
    private final DQResponseDocumentUtils dqResponseDocumentUtils;
    private final DetermineNextState determineNextState;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;
    @Value("${court-location.specified-claim.epimms-id}") String cnbcEpimsId;

    public CallbackResponse execute(CallbackParams callbackParams) {

        CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());

        CaseData caseData = persistPartyAddress(oldCaseData, callbackParams.getCaseData());

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .applicant1ResponseDate(time.now());

        persistFlagsForParties(oldCaseData, caseData, builder);
        setResponseDocumentNull(builder);
        updateCaselocationDetails(callbackParams, caseData, builder);
        updateApplicant1DQ(callbackParams, caseData, builder);
        assignApplicant1DQExpertsIfPresent(caseData, builder);
        assignApplicant2DQExpertsIfPresent(caseData, builder);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(
            builder, featureToggleService.isUpdateContactDetailsEnabled());

        if (featureToggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToApplicantExperts(builder);
            addEventAndDateAddedToApplicantWitnesses(builder);
        }

        populateDQPartyIds(builder);

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE_SPEC, builder);
        moveClaimToMediation(callbackParams, caseData, builder);

        String nextState = putCaseStateInJudicialReferral(caseData);
        BusinessProcess businessProcess = BusinessProcess.ready(CLAIMANT_RESPONSE_SPEC);
        nextState = determineNextState.determineNextState(caseData, callbackParams, builder, nextState, businessProcess);

        is1v1RespondImmediately(caseData, builder);

        frcDocumentsUtils.assembleClaimantsFRCDocuments(caseData);

        builder.claimantResponseDocuments(
            dqResponseDocumentUtils.buildClaimantResponseDocuments(builder.build()));

        clearTempDocuments(builder);

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                builder
            ));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .state(nextState)
            .build();
    }

    private void moveClaimToMediation(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if ((V_2.equals(callbackParams.getVersion())
            && featureToggleService.isPinInPostEnabled()
            && isOneVOne(caseData)
            && caseData.hasClaimantAgreedToFreeMediation())
            || (featureToggleService.isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && (YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())))) {
            builder.claimMovedToMediationOn(LocalDate.now());
            log.info("Moved Claim to mediation for Case : {}", caseData.getCcdCaseReference());
        }
    }

    private static void assignApplicant2DQExpertsIfPresent(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getApplicant2DQ().getSmallClaimExperts());
            builder.applicant2DQ(
                builder.build().getApplicant2DQ().toBuilder()
                    .applicant2DQExperts(Experts.builder()
                                             .expertRequired(caseData.getApplicantMPClaimExpertSpecRequired())
                                             .details(wrapElements(expert))
                                             .build())
                    .build());
        } else if (caseData.getApplicant2DQ() != null
            && caseData.getApplicantMPClaimExpertSpecRequired() != null
            && NO.equals(caseData.getApplicantMPClaimExpertSpecRequired())) {
            builder.applicant2DQ(
                builder.build().getApplicant2DQ().toBuilder()
                    .applicant2DQExperts(Experts.builder()
                                             .expertRequired(NO)
                                             .build())
                    .build());
        }
    }

    private static void assignApplicant1DQExpertsIfPresent(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getApplicant1DQ().getSmallClaimExperts());
            YesOrNo expertRequired = TWO_V_ONE.equals(getMultiPartyScenario(caseData)) ? caseData.getApplicantMPClaimExpertSpecRequired()
                : caseData.getApplicant1ClaimExpertSpecRequired();
            builder.applicant1DQ(
                builder.build().getApplicant1DQ().toBuilder()
                    .applicant1DQExperts(Experts.builder()
                                             .expertRequired(expertRequired)
                                             .details(wrapElements(expert))
                                             .build())
                    .build());
        } else if (caseData.getApplicant1DQ() != null
            && (NO.equals(caseData.getApplicantMPClaimExpertSpecRequired())
            || NO.equals(caseData.getApplicant1ClaimExpertSpecRequired()))) {
            builder.applicant1DQ(
                builder.build().getApplicant1DQ().toBuilder()
                    .applicant1DQExperts(Experts.builder()
                                             .expertRequired(NO)
                                             .build())
                    .build());
        }
    }

    private void updateApplicant1DQ(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if (caseData.hasApplicantProceededWithClaim() || (caseData.isPartAdmitClaimSpec() && caseData.isPartAdmitClaimNotSettled())) {
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Applicant1DQ.Applicant1DQBuilder dq = caseData.getApplicant1DQ().toBuilder()
                .applicant1DQStatementOfTruth(statementOfTruth);

            if (notTransferredOnline(caseData) && (!isFlightDelayAndSmallClaim(caseData)
                || isFlightDelaySmallClaimAndOther(caseData))) {
                updateDQCourtLocations(callbackParams, caseData, builder, dq, isFlightDelaySmallClaimAndOther(caseData));
            }

            var smallClaimWitnesses = builder.build().getApplicant1DQWitnessesSmallClaim();
            if (smallClaimWitnesses != null) {
                dq.applicant1DQWitnesses(smallClaimWitnesses);
            }

            builder.applicant1DQ(dq.build());
            builder.uiStatementOfTruth(StatementOfTruth.builder().build());
        }
    }

    private void updateCaselocationDetails(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if (notTransferredOnline(caseData)) {
            updateCaseManagementLocation(callbackParams, builder);
        }

        if (log.isDebugEnabled()) {
            log.debug("Case management location for {} is {}", caseData.getLegacyCaseReference(), builder.build().getCaseManagementLocation());
        }
    }

    private static void setResponseDocumentNull(CaseData.CaseDataBuilder<?, ?> builder) {
        log.info("Resetting Documents");
        builder.respondent1GeneratedResponseDocument(null);
        builder.respondent2GeneratedResponseDocument(null);
        builder.respondent1ClaimResponseDocumentSpec(null);
    }

    private String putCaseStateInJudicialReferral(CaseData caseData) {
        if (caseData.isRespondentResponseFullDefence()
            && JudicialReferralUtils.shouldMoveToJudicialReferral(caseData, featureToggleService.isMultiOrIntermediateTrackEnabled(caseData))) {
            return CaseState.JUDICIAL_REFERRAL.name();
        }
        return null;
    }

    public boolean notTransferredOnline(CaseData caseData) {
        return caseData.getCaseManagementLocation().getBaseLocation().equals(cnbcEpimsId);
    }

    private void updateCaseManagementLocation(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = callbackParams.getCaseData();
        if (isFlightDelaySmallClaimAndAirline(caseData)) {
            builder.caseManagementLocation(caseData.getFlightDelayDetails().getFlightCourtLocation());
        } else if (!isFlightDelayAndSmallClaim(caseData)) {
            locationHelper.getCaseManagementLocation(caseData)
                .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                    builder,
                    requestedCourt,
                    () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                        CallbackParams.Params.BEARER_TOKEN).toString())
                ));
        }
    }

    private boolean isFlightDelayAndSmallClaim(CaseData caseData) {
        return (featureToggleService.isSdoR2Enabled() && caseData.getIsFlightDelayClaim() != null
            && caseData.getIsFlightDelayClaim().equals(YES)
            &&  SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }

    private boolean isFlightDelaySmallClaimAndAirline(CaseData caseData) {
        return (isFlightDelayAndSmallClaim(caseData) && caseData.getFlightDelayDetails() != null
            && !caseData.getFlightDelayDetails().getAirlineList()
            .getValue().getCode().equals("OTHER"));
    }

    private boolean isFlightDelaySmallClaimAndOther(CaseData caseData) {
        return (isFlightDelayAndSmallClaim(caseData) && caseData.getFlightDelayDetails() != null
            && caseData.getFlightDelayDetails().getAirlineList()
            .getValue().getCode().equals("OTHER"));
    }

    private void updateDQCourtLocations(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder,
                                        Applicant1DQ.Applicant1DQBuilder dq, boolean forceClaimantCourt) {
        handleCourtLocationData(caseData, builder, dq, callbackParams);
        Optional<RequestedCourt> newCourt;

        if (forceClaimantCourt) {
            newCourt = locationHelper.getClaimantRequestedCourt(builder.applicant1DQ(dq.build()).build());
        } else {
            newCourt = locationHelper.getCaseManagementLocation(builder.applicant1DQ(dq.build()).build());
        }

        newCourt.ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
            builder,
            requestedCourt,
            () -> locationRefDataService.getCourtLocationsForDefaultJudgments(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString())
        ));
        if (log.isDebugEnabled()) {
            log.debug("Case management location for {} is {}", caseData.getLegacyCaseReference(), builder.build().getCaseManagementLocation());
        }
    }

    private void clearTempDocuments(CaseData.CaseDataBuilder<?, ?> builder) {
        Applicant1DQ applicant1DQ = builder.build().getApplicant1DQ();
        if (nonNull(applicant1DQ)) {
            builder.applicant1DQ(builder.build().getApplicant1DQ().toBuilder().applicant1DQDraftDirections(null).build());
        }
    }

    private void handleCourtLocationData(CaseData caseData, CaseData.CaseDataBuilder dataBuilder,
                                         Applicant1DQ.Applicant1DQBuilder dq,
                                         CallbackParams callbackParams) {
        RequestedCourt requestedCourt = caseData.getApplicant1DQ().getApplicant1DQRequestedCourt();
        if (requestedCourt != null) {
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), requestedCourt.getResponseCourtLocations());
            if (nonNull(courtLocation)) {
                dataBuilder
                    .applicant1DQ(dq.applicant1DQRequestedCourt(
                        caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().toBuilder()
                            .responseCourtLocations(null)
                            .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                            .responseCourtCode(courtLocation.getCourtLocationCode()).build()
                    ).build());
            }
        }
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private void is1v1RespondImmediately(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        if (featureToggleService.isJudgmentOnlineLive()
            && isOneVOne(caseData)
            && ((caseData.isFullAdmitPayImmediatelyClaimSpec()
            && caseData.getApplicant1ProceedWithClaim() == null)
            || caseData.isPartAdmitImmediatePaymentClaimSettled())) {
            builder.respondForImmediateOption(YesOrNo.YES);
        }
    }
}
