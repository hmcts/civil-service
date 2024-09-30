package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
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
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetApplicantResponseDeadline implements CaseTask {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService toggleService;
    private final ObjectMapper objectMapper;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final Time time;
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;
    private final IStateFlowEngine stateFlowEngine;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FrcDocumentsUtils frcDocumentsUtils;
    private final CourtLocationUtils courtLocationUtils;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;
    private final AssignCategoryId assignCategoryId;
    private final DQResponseDocumentUtils dqResponseDocumentUtils;

    private static final String DEF2 = "Defendant 2";

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing SetApplicantResponseDeadline with callbackParams: {}", callbackParams);

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        log.debug("Updating respondent 1");
        LocalDateTime responseDate = time.now();
        Party updatedRespondent1 = updateRespondent1(caseData);
        updatedData.respondent1(updatedRespondent1).respondent1Copy(null);

        log.debug("Handling respondent 2 for same legal representative");
        handleRespondent2ForSameLegalRep(responseDate, caseData, updatedData);

        log.debug("Updating respondent 2 if present");
        updateRespondent2IfPresent(caseData, updatedData);

        log.debug("Handling part payment time");
        handlePartPaymentTime(caseData, updatedData);

        log.debug("Handling respondent DQ");
        handleRespondentDQ(callbackParams, caseData, updatedData, responseDate);

        log.debug("Handling full defence");
        handleFullDefence(callbackParams, caseData, updatedData);

        log.debug("Updating DQ witnesses for small claim");
        updateDQWitnessesSmallClaim(caseData, updatedData);

        log.debug("Updating DQ experts");
        updateDQExperts(caseData, updatedData);

        log.debug("Rolling up unavailability dates for respondent");
        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData, toggleService.isUpdateContactDetailsEnabled());

        log.debug("Updating respondent details for claim tab");
        updateRespondentDetailsForClaimTab(updatedData, caseData);

        log.debug("Handling update contact details");
        handleUpdateContactDetails(updatedData);

        log.debug("Handling HMC enabled");
        handleHmcEnabled(updatedData);

        log.debug("Initializing case flags");
        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_SPEC, updatedData);

        log.debug("Handling user document generation");
        handleUserDocumentGeneration(callbackParams, caseData, updatedData);

        log.debug("Updating correspondence address");
        updateCorrespondenceAddress(callbackParams, updatedData, caseData);

        log.info("Building callback response");
        return buildCallbackResponse(caseData, updatedData);
    }

    private void handleRespondent2ForSameLegalRep(LocalDateTime responseDate, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if respondent 2 has the same legal representative");
        if (respondent2HasSameLegalRep(caseData) && YES.equals(caseData.getRespondentResponseIsSame())) {
            log.debug("Respondent 2 has the same legal representative, updating respondent 2");
            updateRespondent2ForSameLegalRep(responseDate, caseData, updatedData);
        }
    }

    private void handlePartPaymentTime(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if part payment time should be set");
        if (shouldSetPartPaymentTime(caseData)) {
            log.debug("Setting part payment time");
            setPartPaymentTime(updatedData);
        }
    }

    private void handleRespondentDQ(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime responseDate) {
        log.debug("Handling respondent DQ");
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams)) {
            log.debug("Solicitor represents only one of the respondents, processing respondent 2 DQ");
            processRespondent2DQ(callbackParams, caseData, updatedData, responseDate);
        } else {
            log.debug("Processing respondent 1 DQ");
            processRespondent1DQ(callbackParams, caseData, updatedData, responseDate);
            log.debug("Updating respondent 2 if necessary");
            updateRespondent2IfNecessary(caseData, updatedData);
        }
    }

    private void handleFullDefence(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if respondent 2 has a full defence");
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
            && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            log.debug("Respondent 2 has a full defence, updating defence admit part payment time route");
            updatedData.defenceAdmitPartPaymentTimeRouteRequired(null);
        }
    }

    private void handleUpdateContactDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if update contact details is enabled");
        if (toggleService.isUpdateContactDetailsEnabled()) {
            log.debug("Update contact details is enabled, adding event and date to respondent data");
            addEventAndDateToRespondentData(updatedData);
        }
    }

    private void handleHmcEnabled(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if HMC is enabled");
        if (toggleService.isHmcEnabled()) {
            log.debug("HMC is enabled, populating DQ party IDs");
            populateDQPartyIds(updatedData);
        }
    }

    private void handleUserDocumentGeneration(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Handling user document generation");
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            log.debug("User is Respondent Solicitor Two, generating respondent 2 document");
            updatedData.respondent2DocumentGeneration("userRespondent2");
        }
    }

    private Party updateRespondent1(CaseData caseData) {
        log.debug("Updating respondent 1");
        Party updatedRespondent1;
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            log.debug("Setting primary address from applicant correspondence address details");
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails()).build();
        } else {
            log.debug("Setting primary address from respondent 1 copy");
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress()).build();
        }
        if (caseData.getRespondent1Copy() != null) {
            log.debug("Setting flags from respondent 1 copy");
            updatedRespondent1 = updatedRespondent1.toBuilder()
                .flags(caseData.getRespondent1Copy().getFlags()).build();
        }
        return updatedRespondent1;
    }

    private void updateRespondent2ForSameLegalRep(LocalDateTime responseDate, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Updating respondent 2 for same legal representative");
        updatedData.respondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec())
            .respondent2ResponseDate(responseDate);
    }

    private void updateRespondent2IfPresent(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if respondent 2 is present");
        if (ofNullable(caseData.getRespondent2()).isPresent() && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            log.debug("Updating respondent 2");
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .flags(caseData.getRespondent2Copy().getFlags())
                .build();
            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
        }
    }

    private boolean shouldSetPartPaymentTime(CaseData caseData) {
        log.debug("Checking if part payment time should be set");
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && ifResponseTypeIsPartOrFullAdmission(caseData);
    }

    private void setPartPaymentTime(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Setting part payment time");
        LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
            LocalDate.now(), RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
        updatedData.respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                      .whenWillThisAmountBePaid(whenBePaid).build());
    }

    private void processRespondent2DQ(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime responseDate) {
        log.debug("Processing respondent 2 DQ");
        updatedData.respondent2ResponseDate(responseDate)
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));
        if (caseData.getRespondent1ResponseDate() != null) {
            log.debug("Setting applicant 1 response deadline");
            updatedData.applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate));
        }
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
            .respondent2DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent2DQ(caseData, updatedData, dq, callbackParams);
        updatedData.respondent2DQ(dq.build());
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void processRespondent1DQ(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime responseDate) {
        log.debug("Processing respondent 1 DQ");
        updatedData.respondent1ResponseDate(responseDate)
            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate))
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));
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

    private void updateRespondent2IfNecessary(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Checking if respondent 2 needs to be updated");
        if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
            log.debug("Updating respondent 2");
            Party updatedRespondent2;
            if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
                log.debug("Setting primary address from respondent 2 home address details");
                updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails()).build();
            } else {
                log.debug("Setting primary address from respondent 2 copy");
                updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress()).build();
            }
            updatedData.respondent2(updatedRespondent2.toBuilder()
                                        .flags(caseData.getRespondent2Copy().getFlags()).build()).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
        }
    }

    private void updateDQWitnessesSmallClaim(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Updating DQ witnesses for small claim");
        if (caseData.getRespondent1DQWitnessesSmallClaim() != null) {
            log.debug("Updating respondent 1 DQ witnesses");
            updatedData.respondent1DQ(
                updatedData.build().getRespondent1DQ().toBuilder().respondent1DQWitnesses(
                    caseData.getRespondent1DQWitnessesSmallClaim()).build());
        }
        if (caseData.getRespondent2DQWitnessesSmallClaim() != null) {
            log.debug("Updating respondent 2 DQ witnesses");
            updatedData.respondent2DQ(
                updatedData.build().getRespondent2DQ().toBuilder().respondent2DQWitnesses(
                    caseData.getRespondent2DQWitnessesSmallClaim()).build());
        }
    }

    private void updateDQExperts(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Updating DQ experts");
        updateRespondent1DQExperts(caseData, updatedData);
        updateRespondent2DQExperts(caseData, updatedData);
    }

    private void updateRespondent1DQExperts(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getRespondent1DQ() != null) {
            log.debug("Updating respondent 1 DQ experts");
            if (YES.equals(caseData.getResponseClaimExpertSpecRequired()) && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
                Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
                updatedData.respondent1DQ(
                    updatedData.build().getRespondent1DQ().toBuilder()
                        .respondent1DQExperts(Experts.builder()
                                                  .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                                  .details(wrapElements(expert))
                                                  .build())
                        .build());
            } else if (NO.equals(caseData.getResponseClaimExpertSpecRequired())) {
                updatedData.respondent1DQ(
                    updatedData.build().getRespondent1DQ().toBuilder()
                        .respondent1DQExperts(Experts.builder()
                                                  .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                                  .build())
                        .build());
            }
        }
    }

    private void updateRespondent2DQExperts(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getRespondent2DQ() != null) {
            log.debug("Updating respondent 2 DQ experts");
            if (YES.equals(caseData.getResponseClaimExpertSpecRequired2()) && caseData.getRespondent2DQ().getSmallClaimExperts() != null) {
                Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent2DQ().getSmallClaimExperts());
                updatedData.respondent2DQ(
                    updatedData.build().getRespondent2DQ().toBuilder()
                        .respondent2DQExperts(Experts.builder()
                                                  .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                                  .details(wrapElements(expert))
                                                  .build())
                        .build());
            } else if (NO.equals(caseData.getResponseClaimExpertSpecRequired2())) {
                updatedData.respondent2DQ(
                    updatedData.build().getRespondent2DQ().toBuilder()
                        .respondent2DQExperts(Experts.builder()
                                                  .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                                  .build())
                        .build());
            }
        }
    }

    private void updateRespondentDetailsForClaimTab(CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {
        log.debug("Updating respondent details for claim tab");
        updatedData.respondent1DetailsForClaimDetailsTab(
            updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(
                updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }
    }

    private void addEventAndDateToRespondentData(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Adding event and date to respondent data");
        addEventAndDateAddedToRespondentExperts(updatedData);
        addEventAndDateAddedToRespondentWitnesses(updatedData);
    }

    private CallbackResponse buildCallbackResponse(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Building callback response");
        if (isTwoLegalRepAwaitingDefendant(caseData)) {
            log.debug("Scenario: Two legal representatives awaiting defendant");
            if (isDefending(caseData)) {
                log.debug("Defendant is defending");
                assembleResponseDocumentsSpec(caseData, updatedData);
            }
            return buildCallbackResponseWithDefaultState(updatedData);
        }

        if (isTwoLegalRepNonFullDefence(caseData)) {
            log.debug("Scenario: Two legal representatives non-full defence");
            return buildCallbackResponseWithState(updatedData, CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        }

        if (isOneLegalRepDivergent(caseData)) {
            log.debug("Scenario: One legal representative divergent");
            return buildCallbackResponseWithState(updatedData, CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        }

        if (isTwoVsOneDivergent(caseData)) {
            log.debug("Scenario: Two vs one divergent");
            return buildCallbackResponseWithState(updatedData, CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        }

        log.debug("Default scenario: Assembling response documents and setting state to AWAITING_APPLICANT_INTENTION");
        assembleResponseDocumentsSpec(caseData, updatedData);
        return buildCallbackResponseWithState(updatedData, CaseState.AWAITING_APPLICANT_INTENTION);
    }

    private boolean isTwoLegalRepAwaitingDefendant(CaseData caseData) {
        log.debug("Checking if scenario is two legal representatives awaiting defendant");
        return getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP && isAwaitingAnotherDefendantResponse(caseData);
    }

    private boolean isTwoLegalRepNonFullDefence(CaseData caseData) {
        log.debug("Checking if scenario is two legal representatives non-full defence");
        return getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && !isAwaitingAnotherDefendantResponse(caseData)
            && (!FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private boolean isOneLegalRepDivergent(CaseData caseData) {
        log.debug("Checking if scenario is one legal representative divergent");
        return getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP && twoVsOneDivergent(caseData);
    }

    private boolean isTwoVsOneDivergent(CaseData caseData) {
        log.debug("Checking if scenario is two vs one divergent");
        return getMultiPartyScenario(caseData) == TWO_V_ONE && twoVsOneDivergent(caseData);
    }

    private CallbackResponse buildCallbackResponseWithState(CaseData.CaseDataBuilder<?, ?> updatedData, CaseState state) {
        log.debug("Building callback response with state: {}", state.name());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state(state.name())
            .build();
    }

    private CallbackResponse buildCallbackResponseWithDefaultState(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Building callback response with default state");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private boolean isDefending(CaseData caseData) {
        log.debug("Checking if the case is defending");
        return FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private boolean ifResponseTypeIsPartOrFullAdmission(CaseData caseData) {
        log.debug("Checking if the response type is part or full admission");
        return (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
            || (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private void updateCorrespondenceAddress(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData, CaseData caseData) {
        log.debug("Updating correspondence address");
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            handleRespondent1CorrespondenceAddress(caseData, updatedCaseData);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            handleRespondent2CorrespondenceAddress(caseData, updatedCaseData);
        }
    }

    private void handleRespondent1CorrespondenceAddress(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Handling respondent 1 correspondence address");
        if (caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            Address newAddress = caseData.getSpecAoSRespondentCorrespondenceAddressdetails();
            updatedCaseData.specRespondentCorrespondenceAddressdetails(newAddress)
                .specAoSRespondentCorrespondenceAddressdetails(Address.builder().build());
            if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP) {
                updatedCaseData.specRespondent2CorrespondenceAddressdetails(newAddress);
            }
        }
    }

    private void handleRespondent2CorrespondenceAddress(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Handling respondent 2 correspondence address");
        if (caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            updatedCaseData.specRespondent2CorrespondenceAddressdetails(
                    caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                .specAoSRespondent2CorrespondenceAddressdetails(Address.builder().build());
        }
    }

    private void assembleResponseDocumentsSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.debug("Assembling response documents for spec");
        List<Element<CaseDocument>> defendantUploads = getDefendantUploads(caseData);
        addRespondent1SpecDefenceResponseDocument(caseData, updatedCaseData, defendantUploads);
        addRespondent2SpecDefenceResponseDocument(caseData, updatedCaseData, defendantUploads);
        addAdditionalDocuments(updatedCaseData, defendantUploads);

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);
        clearTempDocuments(updatedCaseData);
    }

    private List<Element<CaseDocument>> getDefendantUploads(CaseData caseData) {
        log.debug("Getting defendant uploads");
        return nonNull(caseData.getDefendantResponseDocuments()) ? caseData.getDefendantResponseDocuments() : new ArrayList<>();
    }

    private void addRespondent1SpecDefenceResponseDocument(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent1SpecDefenceResponseDocument = caseData.getRespondent1SpecDefenceResponseDocument();
        if (respondent1SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1SpecDefenceResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildCaseDocumentElement(
                    respondent1ClaimDocument,
                    updatedCaseData.build().getRespondent1ResponseDate(),
                    DocCategory.DEF1_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
    }

    private Element<CaseDocument> buildCaseDocumentElement(
        Document document,
        LocalDateTime responseDate,
        String categoryId
    ) {
        Element<CaseDocument> documentElement = buildElemCaseDocument(
            document, "Defendant", responseDate, DocumentType.DEFENDANT_DEFENCE
        );
        assignCategoryId.assignCategoryIdToDocument(document, categoryId);
        return documentElement;
    }

    private void addRespondent2SpecDefenceResponseDocument(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        log.debug("Adding respondent 2 spec defence response document");
        ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
        if (respondent2SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
            if (respondent2ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent2ClaimDocument, DEF2,
                    updatedCaseData.build().getRespondent2ResponseDate(),
                    DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent2ClaimDocument,
                    DocCategory.DEF2_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
    }

    private void addAdditionalDocuments(CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        log.debug("Adding additional documents");
        List<Element<CaseDocument>> additionalDocuments = dqResponseDocumentUtils.buildDefendantResponseDocuments(updatedCaseData.build());
        defendantUploads.addAll(additionalDocuments);
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData,
                                                     Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        log.debug("Handling court location for Respondent 1 DQ");
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant1(caseData, callbackParams);

        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            log.info("Found preferred court location for Respondent 1: {}", courtLocation.getCourtLocationCode());

            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ()
                                               .getRespondToCourtLocation().toBuilder()
                                               .reasonForHearingAtSpecificCourt(
                                                   caseData.getRespondent1DQ()
                                                       .getRespondToCourtLocation()
                                                       .getReasonForHearingAtSpecificCourt())
                                               .responseCourtLocations(null)
                                               .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                               .responseCourtCode(courtLocation.getCourtLocationCode()).build());
            dq.respondToCourtLocation(RequestedCourt.builder()
                                          .responseCourtLocations(null)
                                          .responseCourtCode(courtLocation.getCourtLocationCode())
                                          .build())
                .responseClaimCourtLocationRequired(YES);
        } else {
            log.info("No preferred court location found for Respondent 1");
            dq.responseClaimCourtLocationRequired(NO);
        }
    }

    private Optional<LocationRefData> getCourtLocationDefendant1(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondToCourtLocation() != null) {
            DynamicList courtLocations = caseData
                .getRespondent1DQ().getRespondToCourtLocation().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            log.debug("Retrieved court location data for Respondent 1: {}", courtLocation);
            return Optional.ofNullable(courtLocation);
        } else {
            log.debug("Respondent 1 DQ or RespondToCourtLocation is null");
            return Optional.empty();
        }
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData,  CaseData.CaseDataBuilder<?, ?> updatedCase,
                                                     Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {
        log.debug("Handling court location for Respondent 2 DQ");
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant2(caseData, callbackParams);
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();
            log.info("Found preferred court location for Respondent 2: {}", courtLocation.getCourtLocationCode());

            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ().getRespondToCourtLocation2().toBuilder()
                                               .responseCourtLocations(null)
                                               .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                               .responseCourtCode(courtLocation.getCourtLocationCode()).build())
                .respondToCourtLocation2(RequestedCourt.builder()
                                             .responseCourtLocations(null)
                                             .responseCourtCode(courtLocation.getCourtLocationCode())
                                             .reasonForHearingAtSpecificCourt(
                                                 caseData.getRespondent2DQ().getRespondToCourtLocation2()
                                                     .getReasonForHearingAtSpecificCourt()
                                             )
                                             .build());
            updatedCase.responseClaimCourtLocation2Required(YES);
        } else {
            log.info("No preferred court location found for Respondent 2");
            updatedCase.responseClaimCourtLocation2Required(NO);
        }
    }

    public Optional<LocationRefData> getCourtLocationDefendant2(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondToCourtLocation2() != null) {
            DynamicList courtLocations = caseData
                .getRespondent2DQ().getRespondToCourtLocation2().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                respondToClaimSpecUtils.getLocationData(callbackParams), courtLocations);
            log.debug("Retrieved court location data for Respondent 2: {}", courtLocation);
            return Optional.ofNullable(courtLocation);
        } else {
            log.debug("Respondent 2 DQ or RespondToCourtLocation2 is null");
            return Optional.empty();
        }
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        boolean condition = caseData.getRespondent1ClaimResponseTypeForSpec() == null
            || caseData.getRespondent2ClaimResponseTypeForSpec() == null;
        log.debug("Checking if awaiting another defendant response: {}", condition);
        return condition;
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        LocalDateTime deadline = deadlinesCalculator.calculateApplicantResponseDeadlineSpec(responseDate);
        log.debug("Calculated applicant 1 response deadline: {}", deadline);
        return deadline;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        log.debug("Checking if respondent 2 has the same legal representative");
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean twoVsOneDivergent(CaseData caseData) {
        log.debug("Checking if the scenario is two vs one divergent");
        return (!FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
            || (!FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams) {
        log.debug("Checking if the solicitor represents only one of the respondents");
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            CaseRole.RESPONDENTSOLICITORTWO
        );
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        log.debug("Checking if the solicitor has the case role: {}", caseRole);
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private void clearTempDocuments(CaseData.CaseDataBuilder<?, ?> builder) {
        log.debug("Clearing temporary documents");
        CaseData caseData = builder.build();

        builder.respondent1SpecDefenceResponseDocument(null);
        builder.respondent2SpecDefenceResponseDocument(null);

        if (nonNull(caseData.getRespondent1DQ())) {
            builder.respondent1DQ(builder.build().getRespondent1DQ().toBuilder().respondent1DQDraftDirections(null).build());
        }
        if (nonNull(caseData.getRespondent2DQ())) {
            builder.respondent2DQ(builder.build().getRespondent2DQ().toBuilder().respondent2DQDraftDirections(null).build());
        }
    }
}
