package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.PopulateRespondentTabDetails.updateDataForClaimDetailsTab;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetApplicantResponseDeadlineSpec implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final ObjectMapper objectMapper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final Time time;
    private final IStateFlowEngine stateFlowEngine;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CourtLocationUtils courtLocationUtils;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;
    private final List<SetApplicantResponseDeadlineCaseDataUpdater> setApplicantResponseDeadlineCaseDataUpdaters;
    private final List<ExpertsAndWitnessesCaseDataUpdater> expertsAndWitnessesCaseDataUpdaters;
    private final RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;
    private final FeatureToggleService featureToggleService;

    public static void getUserInfo(CallbackParams callbackParams,
                                   CaseData caseData, UserService userService,
                                   CoreCaseUserService coreCaseUserService) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseData.setRespondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORONE
        )
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO
        )) {
            caseData.setRespondent2DocumentGeneration("userRespondent2");
        }
    }

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();

        setApplicantResponseDeadlineCaseDataUpdaters.forEach(updater -> updater.update(caseData));
        handleSolicitorRepresentation(callbackParams, caseData, responseDate);
        handleExpertsAndWitnesses(caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(caseData);

        updateDataForClaimDetailsTab(caseData, objectMapper, false);

        addEventAndDateAddedToRespondentExperts(caseData);
        addEventAndDateAddedToRespondentWitnesses(caseData);
        populateDQPartyIds(caseData);

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_SPEC, caseData);
        handleDocumentGeneration(callbackParams, caseData);
        updateCorrespondenceAddresses(callbackParams, caseData);
        return handleMultiPartyScenarios(caseData);
    }

    private void handleSolicitorRepresentation(CallbackParams callbackParams, CaseData caseData,
                                               LocalDateTime responseDate) {
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams)) {
            handleSingleRespondentRepresentation(callbackParams, caseData, responseDate);
        } else {
            handleBothRespondentsRepresentation(callbackParams, caseData, responseDate);
        }

        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
            && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(null);
        }
    }

    private void handleSingleRespondentRepresentation(CallbackParams callbackParams, CaseData caseData,
                                                      LocalDateTime responseDate) {
        caseData.setRespondent2ResponseDate(responseDate);
        caseData.setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));
        if (caseData.getRespondent1ResponseDate() != null) {
            LocalDateTime applicant1ResponseDeadline = getApplicant1ResponseDeadline(responseDate);
            caseData
                .setApplicant1ResponseDeadline(applicant1ResponseDeadline);
            caseData.setNextDeadline(applicant1ResponseDeadline.toLocalDate());
        }
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        respondent2DQ.setRespondent2DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent2DQ(caseData, respondent2DQ, callbackParams);
        caseData.setRespondent2DQ(respondent2DQ);
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent2Spec(callbackParams, caseData);
        caseData.setUiStatementOfTruth(new StatementOfTruth());
    }

    private void handleBothRespondentsRepresentation(CallbackParams callbackParams, CaseData caseData,
                                                     LocalDateTime responseDate) {
        boolean nextDeadlineRespondent2 = NO.equals(caseData.getRespondent2SameLegalRepresentative())
            && isNull(caseData.getRespondent2ResponseDate());
        LocalDateTime applicant1ResponseDeadline = getApplicant1ResponseDeadline(responseDate);
        LocalDate respondent2Deadline = nonNull(caseData.getRespondent2ResponseDeadline())
            ? caseData.getRespondent2ResponseDeadline().toLocalDate()
            : caseData.getRespondent1ResponseDeadline().toLocalDate();

        LocalDate nextDeadline = nextDeadlineRespondent2
            ? respondent2Deadline
            : applicant1ResponseDeadline.toLocalDate();

        caseData
            .setRespondent1ResponseDate(responseDate);
        caseData.setApplicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate));
        caseData.setApplicant1ResponseDeadline(applicant1ResponseDeadline);
        caseData.setNextDeadline(nextDeadline);
        caseData.setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

        if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
            Party updatedRespondent2;

            if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
                Party resp2 = caseData.getRespondent2();
                resp2.setPrimaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails());
                updatedRespondent2 = resp2;
            } else {
                Party resp2 = caseData.getRespondent2();
                resp2.setPrimaryAddress(caseData.getRespondent2Copy().getPrimaryAddress());
                updatedRespondent2 = resp2;
            }

            updatedRespondent2.setFlags(caseData.getRespondent2Copy().getFlags());
            caseData.setRespondent2(updatedRespondent2);
            caseData.setRespondent2Copy(null);
            Party respondent2Clone = objectMapper.convertValue(updatedRespondent2, Party.class);
            respondent2Clone.setFlags(null);
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2Clone);
        }

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        respondent1DQ.setRespondent1DQStatementOfTruth(statementOfTruth);
        Witnesses witnesses = new Witnesses();
        witnesses.setWitnessesToAppear(caseData.getRespondent1DQWitnessesRequiredSpec());
        witnesses.setDetails(caseData.getRespondent1DQWitnessesDetailsSpec());
        respondent1DQ.setRespondent1DQWitnesses(witnesses);
        handleCourtLocationForRespondent1DQ(caseData, respondent1DQ, callbackParams);
        caseData.setRespondent1DQ(respondent1DQ);
        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabRespondent1Spec(caseData, callbackParams);

        caseData.setUiStatementOfTruth(new StatementOfTruth());
    }

    private void handleExpertsAndWitnesses(CaseData caseData) {
        expertsAndWitnessesCaseDataUpdaters.forEach(updater -> updater.update(caseData));
    }

    private void handleDocumentGeneration(CallbackParams callbackParams,
                                          CaseData caseData) {
        getUserInfo(callbackParams, caseData, userService, coreCaseUserService);
    }

    private void updateCorrespondenceAddresses(CallbackParams callbackParams,
                                               CaseData caseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)
            && NO.equals(caseData.getSpecAoSRespondentCorrespondenceAddressRequired())) {
            Address newAddress = caseData.getSpecAoSRespondentCorrespondenceAddressdetails();
            caseData.setSpecRespondentCorrespondenceAddressdetails(newAddress);
            caseData.setSpecAoSRespondentCorrespondenceAddressdetails(new Address());
            if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
                caseData.setSpecRespondent2CorrespondenceAddressdetails(newAddress);
            }
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
            && NO.equals(caseData.getSpecAoSRespondent2CorrespondenceAddressRequired())) {
            caseData.setSpecRespondent2CorrespondenceAddressdetails(
                caseData.getSpecAoSRespondent2CorrespondenceAddressdetails());
            caseData.setSpecAoSRespondent2CorrespondenceAddressdetails(new Address());
        }
    }

    private AboutToStartOrSubmitCallbackResponse handleMultiPartyScenarios(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(scenario) && isAwaitingAnotherDefendantResponse(caseData)) {
            if (isDefending(caseData)) {
                respondToClaimSpecUtils.assembleResponseDocumentsSpec(caseData);
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        } else if (ONE_V_TWO_TWO_LEGAL_REP.equals(scenario) && !isAwaitingAnotherDefendantResponse(caseData)) {
            if (!FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseData.toMap(objectMapper))
                    .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                    .build();
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(scenario) && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                .build();
        } else if (TWO_V_ONE.equals(scenario) && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                .build();
        }
        respondToClaimSpecUtils.assembleResponseDocumentsSpec(caseData);

        if (featureToggleService.isWelshEnabledForMainCase() && caseData.isLipvLROneVOne()
            && caseData.isClaimantBilingual()) {
            caseData.setApplicant1ResponseDeadline(null);
            caseData.setNextDeadline(null);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state(CaseState.AWAITING_APPLICANT_INTENTION.name())
            .build();
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        return deadlinesCalculator.calculateApplicantResponseDeadlineSpec(responseDate);
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData,
                                                     Respondent2DQ dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant2(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            RequestedCourt requestedCourt = caseData.getRespondent2DQ().getRespondToCourtLocation2();
            requestedCourt.setResponseCourtLocations(null);
            requestedCourt.setCaseLocation(LocationHelper.buildCaseLocation(courtLocation));
            requestedCourt.setResponseCourtCode(courtLocation.getCourtLocationCode());
            dq.setRespondent2DQRequestedCourt(requestedCourt);
            RequestedCourt requestedCourt1 = new RequestedCourt();
            requestedCourt1.setResponseCourtLocations(null);
            requestedCourt1.setResponseCourtCode(courtLocation.getCourtLocationCode());
            requestedCourt1.setReasonForHearingAtSpecificCourt(caseData.getRespondent2DQ()
                                                     .getRespondToCourtLocation2()
                                                     .getReasonForHearingAtSpecificCourt());
            dq.setRespondToCourtLocation2(requestedCourt1);
            caseData.setResponseClaimCourtLocation2Required(YES);
        } else {
            caseData.setResponseClaimCourtLocation2Required(NO);
        }
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData,
                                                     Respondent1DQ dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant1(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            RequestedCourt requestedCourt = caseData.getRespondent1DQ().getRespondToCourtLocation();
            requestedCourt.setReasonForHearingAtSpecificCourt(caseData.getRespondent1DQ()
                                                                  .getRespondToCourtLocation()
                                                                  .getReasonForHearingAtSpecificCourt());
            requestedCourt.setResponseCourtLocations(null);
            requestedCourt.setCaseLocation(LocationHelper.buildCaseLocation(courtLocation));
            requestedCourt.setResponseCourtCode(courtLocation.getCourtLocationCode());
            dq.setRespondent1DQRequestedCourt(requestedCourt);
            RequestedCourt requestedCourt1 = new RequestedCourt();
            requestedCourt1.setResponseCourtLocations(null);
            requestedCourt1.setResponseCourtCode(courtLocation.getCourtLocationCode());
            dq.setRespondToCourtLocation(requestedCourt1);
            dq.setResponseClaimCourtLocationRequired(YES);
        } else {
            dq.setResponseClaimCourtLocationRequired(NO);
        }
    }

    private Optional<LocationRefData> getCourtLocationDefendant2(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondToCourtLocation2() != null) {
            DynamicList courtLocations =
                caseData.getRespondent2DQ().getRespondToCourtLocation2().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            return Optional.ofNullable(courtLocation);
        }
        return Optional.empty();
    }

    private Optional<LocationRefData> getCourtLocationDefendant1(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondToCourtLocation() != null) {
            DynamicList courtLocations =
                caseData.getRespondent1DQ().getRespondToCourtLocation().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            return Optional.ofNullable(courtLocation);
        }
        return Optional.empty();
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            CaseRole.RESPONDENTSOLICITORTWO
        );
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == null
            || caseData.getRespondent2ClaimResponseTypeForSpec() == null;
    }

    private boolean isDefending(CaseData caseData) {
        return FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private boolean twoVsOneDivergent(CaseData caseData) {
        return (!FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
            || (!FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
    }
}
