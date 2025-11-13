package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.JudicialReferralUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
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
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.service.PaymentDateService.DATE_FORMATTER;
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
    private final RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;
    private final PaymentDateService paymentDateService;
    @Value("${court-location.specified-claim.epimms-id}")
    String cnbcEpimsId;
    @Value("${court-location.specified-claim.region-id}")
    String cnbcRegionId;

    public CallbackResponse execute(CallbackParams callbackParams) {

        CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());
        CaseData caseData = persistPartyAddress(oldCaseData, callbackParams.getCaseData());

        // Apply all setter-based modifications first
        persistFlagsForParties(oldCaseData, caseData);
        caseData.setApplicant1ResponseDate(time.now());

        setResponseDocumentNull(caseData);
        updateCaselocationDetails(caseData);
        updateApplicant1DQ(callbackParams, caseData);
        assignApplicant1DQExpertsIfPresent(caseData);
        assignApplicant2DQExpertsIfPresent(caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(caseData);

        addEventAndDateAddedToApplicantExperts(caseData);
        addEventAndDateAddedToApplicantWitnesses(caseData);
        populateDQPartyIds(caseData);

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE_SPEC, caseData);
        moveClaimToMediation(callbackParams, caseData);

        String nextState = putCaseStateInJudicialReferral(caseData);
        BusinessProcess businessProcess = BusinessProcess.ready(CLAIMANT_RESPONSE_SPEC);

        nextState = determineNextState.determineNextState(
            caseData,
            callbackParams,
            nextState,
            businessProcess
        );

        is1v1RespondImmediately(caseData);

        frcDocumentsUtils.assembleClaimantsFRCDocuments(caseData);

        caseData.setClaimantResponseDocuments(
            dqResponseDocumentUtils.buildClaimantResponseDocuments(caseData));

        clearTempDocuments(caseData);

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            if ((AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack())
                || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
                && caseData.isLipCase())) {
                caseData.setIsMintiLipCase(YES);
            }

            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                caseData
            ));
        }

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicantSpec(callbackParams, caseData);
        caseData.setNextDeadline(null);
        caseData.setPreviousCCDState(caseData.getCcdState());
        if (isDefendantPartAdmitPayImmediatelyAccepted(caseData)) {
            LocalDate whenBePaid = paymentDateService.calculatePaymentDeadline();
            caseData.setWhenToBePaidText(whenBePaid.format(DATE_FORMATTER));
            caseData.setRespondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                      .whenWillThisAmountBePaid(whenBePaid).build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state(nextState)
            .build();
    }

    private void moveClaimToMediation(CallbackParams callbackParams, CaseData caseData) {
        if ((V_2.equals(callbackParams.getVersion())
            && isOneVOne(caseData)
            && caseData.hasClaimantAgreedToFreeMediation())
            || (featureToggleService.isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && caseData.hasApplicantProceededWithClaim())) {
            caseData.setClaimMovedToMediationOn(LocalDate.now());
            log.info("Moved Claim to mediation for Case : {}", caseData.getCcdCaseReference());
        }
    }

    private boolean isDefendantPartAdmitPayImmediatelyAccepted(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
            && (PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            && YES.equals(caseData.getApplicant1AcceptAdmitAmountPaidSpec());
    }

    private static void assignApplicant2DQExpertsIfPresent(CaseData caseData) {
        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getApplicant2DQ().getSmallClaimExperts());
            caseData.setApplicant2DQ(
                caseData.getApplicant2DQ().toBuilder()
                    .applicant2DQExperts(Experts.builder()
                                             .expertRequired(caseData.getApplicantMPClaimExpertSpecRequired())
                                             .details(wrapElements(expert))
                                             .build())
                    .build());
        } else if (caseData.getApplicant2DQ() != null
            && caseData.getApplicantMPClaimExpertSpecRequired() != null
            && NO.equals(caseData.getApplicantMPClaimExpertSpecRequired())) {
            caseData.setApplicant2DQ(
                caseData.getApplicant2DQ().toBuilder()
                    .applicant2DQExperts(Experts.builder()
                                             .expertRequired(NO)
                                             .build())
                    .build());
        }
    }

    private static void assignApplicant1DQExpertsIfPresent(CaseData caseData) {
        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getApplicant1DQ().getSmallClaimExperts());
            YesOrNo expertRequired = TWO_V_ONE.equals(getMultiPartyScenario(caseData)) ? caseData.getApplicantMPClaimExpertSpecRequired()
                : caseData.getApplicant1ClaimExpertSpecRequired();
            caseData.setApplicant1DQ(
                caseData.getApplicant1DQ().toBuilder()
                    .applicant1DQExperts(Experts.builder()
                                             .expertRequired(expertRequired)
                                             .details(wrapElements(expert))
                                             .build())
                    .build());
        } else if (caseData.getApplicant1DQ() != null
            && (NO.equals(caseData.getApplicantMPClaimExpertSpecRequired())
            || NO.equals(caseData.getApplicant1ClaimExpertSpecRequired()))) {
            caseData.setApplicant1DQ(
                caseData.getApplicant1DQ().toBuilder()
                    .applicant1DQExperts(Experts.builder()
                                             .expertRequired(NO)
                                             .build())
                    .build());
        }
    }

    private void updateApplicant1DQ(CallbackParams callbackParams, CaseData caseData) {
        if (caseData.hasApplicantProceededWithClaim() || (caseData.isPartAdmitClaimSpec() && caseData.isPartAdmitClaimNotSettled())) {
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Applicant1DQ.Applicant1DQBuilder dq = caseData.getApplicant1DQ().toBuilder()
                .applicant1DQStatementOfTruth(statementOfTruth);

            // For flight delay small claims with a specific airline (not OTHER), update CML to flight location
            if (isFlightDelaySmallClaimAndAirline(caseData)) {
                caseData.setCaseManagementLocation(caseData.getFlightDelayDetails().getFlightCourtLocation());
            } else if (notTransferredOnline(caseData) && (!isFlightDelayAndSmallClaim(caseData)
                || isFlightDelaySmallClaimAndOther(caseData))) {
                updateDQCourtLocations(
                    callbackParams,
                    caseData,
                    dq,
                    isFlightDelaySmallClaimAndOther(caseData)
                );
            }

            var smallClaimWitnesses = caseData.getApplicant1DQWitnessesSmallClaim();
            if (smallClaimWitnesses != null) {
                dq.applicant1DQWitnesses(smallClaimWitnesses);
            }

            caseData.setApplicant1DQ(dq.build());
            caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
        }
    }

    private void updateCaselocationDetails(CaseData caseData) {
        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && isMultiOrIntTrackSpec(caseData)
            && caseData.isLipCase()) {
            // If case is Multi or Intermediate, and has a LIP involved, and even if transferred online
            // CML should be set/maintained at CNBC for transfer offline tasks (takeCaseOfflineMinti)
            caseData.setCaseManagementLocation(CaseLocationCivil.builder().baseLocation(cnbcEpimsId).region(cnbcRegionId).build());
        }
        // For non-Minti cases, CML updates are handled in updateDQCourtLocations() method
        // We maintain whatever CML is currently set (whether CNBC or transferred location)

        if (log.isDebugEnabled()) {
            log.debug(
                "Case management location for {} is {}",
                caseData.getLegacyCaseReference(),
                caseData.getCaseManagementLocation()
            );
        }
    }

    private static void setResponseDocumentNull(CaseData caseData) {
        log.info("Resetting Documents");
        caseData.setRespondent1GeneratedResponseDocument(null);
        caseData.setRespondent2GeneratedResponseDocument(null);
        caseData.setRespondent1ClaimResponseDocumentSpec(null);
    }

    private String putCaseStateInJudicialReferral(CaseData caseData) {
        if (caseData.isRespondentResponseFullDefence()
            && JudicialReferralUtils.shouldMoveToJudicialReferral(
            caseData,
            featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
        )) {
            return CaseState.JUDICIAL_REFERRAL.name();
        }
        return null;
    }

    public boolean notTransferredOnline(CaseData caseData) {
        return caseData.getCaseManagementLocation() != null
            && caseData.getCaseManagementLocation().getBaseLocation() != null
            && caseData.getCaseManagementLocation().getBaseLocation().equals(cnbcEpimsId);
    }

    private boolean isFlightDelayAndSmallClaim(CaseData caseData) {
        return (caseData.getIsFlightDelayClaim() != null
            && caseData.getIsFlightDelayClaim().equals(YES)
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack()));
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

    private void updateDQCourtLocations(CallbackParams callbackParams, CaseData caseData,
                                        Applicant1DQ.Applicant1DQBuilder dq, boolean forceClaimantCourt) {
        handleCourtLocationData(caseData, dq, callbackParams);

        caseData.setApplicant1DQ(dq.build());
        Optional<RequestedCourt> newCourt;

        if (forceClaimantCourt) {
            newCourt = locationHelper.getClaimantRequestedCourt(caseData);
        } else {
            newCourt = locationHelper.getCaseManagementLocation(caseData);
        }

        // Don't update CML if it's at CNBC - maintain CNBC location
        if (!notTransferredOnline(caseData)) {
            newCourt.ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                caseData,
                requestedCourt,
                () -> locationRefDataService.getCourtLocationsForDefaultJudgments(
                    callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString())
            ));
        }
        if (log.isDebugEnabled()) {
            log.debug(
                "Case management location for {} is {}",
                caseData.getLegacyCaseReference(),
                caseData.getCaseManagementLocation()
            );
        }
    }

    private void clearTempDocuments(CaseData caseData) {
        Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();
        if (nonNull(applicant1DQ)) {
            caseData.setApplicant1DQ(caseData.getApplicant1DQ().toBuilder().applicant1DQDraftDirections(null).build());
        }
    }

    private void handleCourtLocationData(CaseData caseData,
                                         Applicant1DQ.Applicant1DQBuilder dq,
                                         CallbackParams callbackParams) {
        RequestedCourt requestedCourt = caseData.getApplicant1DQ().getApplicant1DQRequestedCourt();
        if (requestedCourt != null) {
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), requestedCourt.getResponseCourtLocations());
            if (nonNull(courtLocation)) {
                dq.applicant1DQRequestedCourt(
                    caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().toBuilder()
                        .responseCourtLocations(null)
                        .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                        .responseCourtCode(courtLocation.getCourtLocationCode()).build()
                );
            }
        }
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private void is1v1RespondImmediately(CaseData caseData) {
        if (featureToggleService.isJudgmentOnlineLive()
            && isOneVOne(caseData)
            && caseData.isPayImmediately()
            && ((caseData.isFullAdmitClaimSpec() && caseData.getApplicant1ProceedWithClaim() == null)
            || caseData.isPartAdmitImmediatePaymentClaimSettled())) {
            caseData.setRespondForImmediateOption(YesOrNo.YES);
        }
    }

    private boolean isMultiOrIntTrackSpec(CaseData caseData) {
        return AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack());
    }

}
