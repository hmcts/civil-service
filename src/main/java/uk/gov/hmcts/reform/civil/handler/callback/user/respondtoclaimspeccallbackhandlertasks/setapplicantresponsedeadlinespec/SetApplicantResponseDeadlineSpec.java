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
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
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
    private final FeatureToggleService toggleService;
    private final ObjectMapper objectMapper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final Time time;
    private final IStateFlowEngine stateFlowEngine;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CourtLocationUtils courtLocationUtils;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;
    private final List<SetApplicantResponseDeadlineCaseDataUpdater> setApplicantResponseDeadlineCaseDataUpdaters;
    private final List<ExpertsAndWitnessesCaseDataUpdater> expertsAndWitnessesCaseDataUpdaters;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        final LocalDateTime responseDate = time.now();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        setApplicantResponseDeadlineCaseDataUpdaters.forEach(updater -> updater.update(caseData, updatedData));
        handleSolicitorRepresentation(callbackParams, caseData, updatedData, responseDate);
        handleExpertsAndWitnesses(caseData, updatedData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData);

        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }

        addEventAndDateAddedToRespondentExperts(updatedData);
        addEventAndDateAddedToRespondentWitnesses(updatedData);
        populateDQPartyIds(updatedData);

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_SPEC, updatedData);
        handleDocumentGeneration(callbackParams, updatedData, caseData);
        updateCorrespondenceAddresses(callbackParams, updatedData, caseData);
        return handleMultiPartyScenarios(caseData, updatedData);
    }

    private void handleSolicitorRepresentation(CallbackParams callbackParams, CaseData caseData,
                                               CaseData.CaseDataBuilder<?, ?> updatedData,
                                               LocalDateTime responseDate) {
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams)) {
            handleSingleRespondentRepresentation(callbackParams, caseData, updatedData, responseDate);
        } else {
            handleBothRespondentsRepresentation(callbackParams, caseData, updatedData, responseDate);
        }

        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
                && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.defenceAdmitPartPaymentTimeRouteRequired(null);
        }
    }

    private void handleSingleRespondentRepresentation(CallbackParams callbackParams, CaseData caseData,
                                                      CaseData.CaseDataBuilder<?, ?> updatedData,
                                                      LocalDateTime responseDate) {
        updatedData.respondent2ResponseDate(responseDate)
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));
        if (caseData.getRespondent1ResponseDate() != null) {
            updatedData.applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate));
        }
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent2DQ(caseData, updatedData, dq, callbackParams);
        updatedData.respondent2DQ(dq.build());
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void handleBothRespondentsRepresentation(CallbackParams callbackParams, CaseData caseData,
                                                     CaseData.CaseDataBuilder<?, ?> updatedData,
                                                     LocalDateTime responseDate) {
        updatedData.respondent1ResponseDate(responseDate)
                .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate))
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

        if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
            updateRespondent2Details(caseData, updatedData);
        }

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth)
                .respondent1DQWitnesses(Witnesses.builder()
                        .witnessesToAppear(caseData.getRespondent1DQWitnessesRequiredSpec())
                        .details(caseData.getRespondent1DQWitnessesDetailsSpec())
                        .build());
        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
        updatedData.respondent1DQ(dq.build());
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void updateRespondent2Details(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Party updatedRespondent2;
        if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
            updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails())
                    .build();
        } else {
            updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                    .flags(caseData.getRespondent2Copy().getFlags())
                    .build();
        }
        updatedData.respondent2(updatedRespondent2)
                .respondent2Copy(null);
        updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
    }

    private void handleExpertsAndWitnesses(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        expertsAndWitnessesCaseDataUpdaters.forEach(updater -> updater.update(caseData, updatedData));
    }

    private void clearFlagsInDetails(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        updatedData.respondent1DetailsForClaimDetailsTab(
                updatedData.build().getRespondent1().toBuilder().flags(null).build()
        );
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(
                    updatedData.build().getRespondent2().toBuilder().flags(null).build()
            );
        }
    }

    private void handleDocumentGeneration(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData,
                                          CaseData caseData) {
        getUserInfo(callbackParams, updatedData, caseData, userService, coreCaseUserService);
    }

    public static void getUserInfo(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData,
                                   CaseData caseData, UserService userService, CoreCaseUserService coreCaseUserService) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        updatedData.respondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                RESPONDENTSOLICITORONE)
                && coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                RESPONDENTSOLICITORTWO)) {
            updatedData.respondent2DocumentGeneration("userRespondent2");
        }
    }

    private void updateCorrespondenceAddresses(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData,
                                               CaseData caseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)
                && NO.equals(caseData.getSpecAoSRespondentCorrespondenceAddressRequired())) {
            Address newAddress = caseData.getSpecAoSRespondentCorrespondenceAddressdetails();
            updatedData.specRespondentCorrespondenceAddressdetails(newAddress)
                    .specAoSRespondentCorrespondenceAddressdetails(Address.builder().build());
            if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
                updatedData.specRespondent2CorrespondenceAddressdetails(newAddress);
            }
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
                && NO.equals(caseData.getSpecAoSRespondent2CorrespondenceAddressRequired())) {
            updatedData.specRespondent2CorrespondenceAddressdetails(
                            caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                    .specAoSRespondent2CorrespondenceAddressdetails(Address.builder().build());
        }
    }

    private AboutToStartOrSubmitCallbackResponse handleMultiPartyScenarios(CaseData caseData,
                                                                           CaseData.CaseDataBuilder<?, ?> updatedData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(scenario) && isAwaitingAnotherDefendantResponse(caseData)) {
            if (isDefending(caseData)) {
                respondToClaimSpecUtils.assembleResponseDocumentsSpec(caseData, updatedData);
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .build();
        } else if (ONE_V_TWO_TWO_LEGAL_REP.equals(scenario) && !isAwaitingAnotherDefendantResponse(caseData)) {
            if (!FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    || !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                        .data(updatedData.build().toMap(objectMapper))
                        .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                        .build();
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(scenario) && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                    .build();
        } else if (TWO_V_ONE.equals(scenario) && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                    .build();
        }
        respondToClaimSpecUtils.assembleResponseDocumentsSpec(caseData, updatedData);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .state(CaseState.AWAITING_APPLICANT_INTENTION.name())
                .build();
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        return deadlinesCalculator.calculateApplicantResponseDeadlineSpec(responseDate);
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase,
                                                     Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant2(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ().getRespondToCourtLocation2().toBuilder()
                            .responseCourtLocations(null)
                            .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                            .responseCourtCode(courtLocation.getCourtLocationCode()).build())
                    .respondToCourtLocation2(RequestedCourt.builder()
                            .responseCourtLocations(null)
                            .responseCourtCode(courtLocation.getCourtLocationCode())
                            .reasonForHearingAtSpecificCourt(caseData.getRespondent2DQ().getRespondToCourtLocation2()
                                    .getReasonForHearingAtSpecificCourt())
                            .build());
            updatedCase.responseClaimCourtLocation2Required(YES);
        } else {
            updatedCase.responseClaimCourtLocation2Required(NO);
        }
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData,
                                                     Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant1(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ().getRespondToCourtLocation().toBuilder()
                            .reasonForHearingAtSpecificCourt(caseData.getRespondent1DQ().getRespondToCourtLocation()
                                    .getReasonForHearingAtSpecificCourt())
                            .responseCourtLocations(null)
                            .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                            .responseCourtCode(courtLocation.getCourtLocationCode()).build())
                    .respondToCourtLocation(RequestedCourt.builder()
                            .responseCourtLocations(null)
                            .responseCourtCode(courtLocation.getCourtLocationCode())
                            .build())
                    .responseClaimCourtLocationRequired(YES);
        } else {
            dq.responseClaimCourtLocationRequired(NO);
        }
    }

    private Optional<LocationRefData> getCourtLocationDefendant2(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent2DQ() != null
                && caseData.getRespondent2DQ().getRespondToCourtLocation2() != null) {
            DynamicList courtLocations = caseData.getRespondent2DQ().getRespondToCourtLocation2().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                    respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            return Optional.ofNullable(courtLocation);
        }
        return Optional.empty();
    }

    private Optional<LocationRefData> getCourtLocationDefendant1(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1DQ().getRespondToCourtLocation() != null) {
            DynamicList courtLocations = caseData.getRespondent1DQ().getRespondToCourtLocation().getResponseCourtLocations();
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
