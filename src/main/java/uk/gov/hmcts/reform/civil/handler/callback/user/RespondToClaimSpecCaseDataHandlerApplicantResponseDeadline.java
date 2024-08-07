package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Component
@RequiredArgsConstructor
public class RespondToClaimSpecCaseDataHandlerApplicantResponseDeadline {

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
    private final RespondToClaimSpecDocumentHandler respondToClaimSpecDocumentHandler;

    CallbackResponse setApplicantResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();

        CaseData.CaseDataBuilder<?, ?> updatedData = updateRespondentsAndAddresses(caseData, responseDate);

        if (shouldPersistRespondent2Address(caseData)) {
            persistRespondent2Address(caseData, updatedData);
        }

        if (shouldSetPaymentTime(caseData)) {
            setPaymentTime(updatedData);
        }

        CaseRole respondentTwoCaseRoleToCheck = RESPONDENTSOLICITORTWO;

        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, respondentTwoCaseRoleToCheck)) {
            updateForSingleRespondent(caseData, updatedData, responseDate, callbackParams);
        } else {
            updateForMultipleRespondents(caseData, updatedData, responseDate, callbackParams);
        }

        clearPaymentTimeIfFullDefence(caseData, updatedData, callbackParams, respondentTwoCaseRoleToCheck);

        updateWitnesses(caseData, updatedData);

        updateExperts(caseData, updatedData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData, toggleService.isUpdateContactDetailsEnabled());

        resetRespondentDetailsForClaimTab(updatedData);

        if (toggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToRespondentExperts(updatedData);
            addEventAndDateAddedToRespondentWitnesses(updatedData);
        }

        if (toggleService.isHmcEnabled()) {
            populateDQPartyIds(updatedData);
        }

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_SPEC, updatedData);

        handleDocumentGeneration(callbackParams, caseData, updatedData);

        if (shouldProceedToHeritageSystem(caseData)) {
            return buildResponse(updatedData, CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        }

        respondToClaimSpecDocumentHandler.assembleResponseDocumentsSpec(caseData, updatedData);
        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        return buildResponse(updatedData, CaseState.AWAITING_APPLICANT_INTENTION);
    }

    private CaseData.CaseDataBuilder<?, ?> updateRespondentsAndAddresses(CaseData caseData, LocalDateTime responseDate) {
        Party updatedRespondent1 = updateRespondent1(caseData);
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        if (RespondToClaimSpecUtilsDisputeDetails.respondent2HasSameLegalRep(caseData)
            && caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            updatedData.respondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec());
            updatedData.respondent2ResponseDate(responseDate);
        }
        return updatedData;
    }

    private Party updateRespondent1(CaseData caseData) {
        Party updatedRespondent1;
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails()).build();
        } else {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress()).build();
        }

        if (caseData.getRespondent1Copy() != null) {
            updatedRespondent1 = updatedRespondent1.toBuilder().flags(caseData.getRespondent1Copy().getFlags()).build();
        }
        return updatedRespondent1;
    }

    private void persistRespondent2Address(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        var updatedRespondent2 = caseData.getRespondent2().toBuilder()
            .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
            .flags(caseData.getRespondent2Copy().getFlags())
            .build();
        updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
        updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
    }

    private boolean shouldPersistRespondent2Address(CaseData caseData) {
        return ofNullable(caseData.getRespondent2()).isPresent() && ofNullable(caseData.getRespondent2Copy()).isPresent();
    }

    private void setPaymentTime(CaseData.CaseDataBuilder<?, ?> updatedData) {
        LocalDate whenBePaid = deadlineCalculatorService.calculateExtendedDeadline(
            LocalDate.now(),
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
        updatedData.respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                      .whenWillThisAmountBePaid(whenBePaid).build());
    }

    private boolean shouldSetPaymentTime(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && ifResponseTypeIsPartOrFullAdmission(caseData);
    }

    private void updateForSingleRespondent(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                           LocalDateTime responseDate, CallbackParams callbackParams) {
        updatedData.respondent2ResponseDate(responseDate)
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

        if (caseData.getRespondent1ResponseDate() != null) {
            updatedData.applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate));
        }

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
            .respondent2DQStatementOfTruth(statementOfTruth);
        respondToClaimSpecDocumentHandler.handleCourtLocationForRespondent2DQ(caseData, updatedData, dq, callbackParams);
        updatedData.respondent2DQ(dq.build());
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void updateForMultipleRespondents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                              LocalDateTime responseDate, CallbackParams callbackParams) {
        updatedData.respondent1ResponseDate(responseDate)
            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate))
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

        if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
            Party updatedRespondent2 = updateRespondent2(caseData);
            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
        }

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth)
            .respondent1DQWitnesses(Witnesses.builder()
                                        .witnessesToAppear(caseData.getRespondent1DQWitnessesRequiredSpec())
                                        .details(caseData.getRespondent1DQWitnessesDetailsSpec())
                                        .build());
        respondToClaimSpecDocumentHandler.handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
        updatedData.respondent1DQ(dq.build());
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private Party updateRespondent2(CaseData caseData) {
        Party updatedRespondent2;
        if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
            updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails()).build();
        } else {
            updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress()).build();
        }
        return updatedRespondent2;
    }

    private void clearPaymentTimeIfFullDefence(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData,
                                               CallbackParams callbackParams, CaseRole respondentTwoCaseRoleToCheck) {
        if (solicitorHasCaseRole(callbackParams, respondentTwoCaseRoleToCheck)
            && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.defenceAdmitPartPaymentTimeRouteRequired(null);
        }
    }

    private void updateWitnesses(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getRespondent1DQWitnessesSmallClaim() != null) {
            updatedData.respondent1DQ(
                updatedData.build().getRespondent1DQ().toBuilder().respondent1DQWitnesses(
                    caseData.getRespondent1DQWitnessesSmallClaim()).build());
        }

        if (caseData.getRespondent2DQWitnessesSmallClaim() != null) {
            updatedData.respondent2DQ(
                updatedData.build().getRespondent2DQ().toBuilder().respondent2DQWitnesses(
                    caseData.getRespondent2DQWitnessesSmallClaim()).build());
        }
    }

    private void updateExperts(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getRespondent1DQ() != null
            && YES.equals(caseData.getResponseClaimExpertSpecRequired())
            && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
            updatedData.respondent1DQ(
                updatedData.build().getRespondent1DQ().toBuilder()
                    .respondent1DQExperts(Experts.builder()
                                              .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                              .details(wrapElements(expert))
                                              .build())
                    .build());
        } else if (caseData.getRespondent1DQ() != null
            && NO.equals(caseData.getResponseClaimExpertSpecRequired())) {
            updatedData.respondent1DQ(
                updatedData.build().getRespondent1DQ().toBuilder()
                    .respondent1DQExperts(Experts.builder()
                                              .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                              .build())
                    .build());
        }

        if (caseData.getRespondent2DQ() != null
            && YES.equals(caseData.getResponseClaimExpertSpecRequired2())
            && caseData.getRespondent2DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent2DQ().getSmallClaimExperts());
            updatedData.respondent2DQ(
                updatedData.build().getRespondent2DQ().toBuilder()
                    .respondent2DQExperts(Experts.builder()
                                              .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                              .details(wrapElements(expert))
                                              .build())
                    .build());
        } else if (caseData.getRespondent2DQ() != null
            && NO.equals(caseData.getResponseClaimExpertSpecRequired2())) {
            updatedData.respondent2DQ(
                updatedData.build().getRespondent2DQ().toBuilder()
                    .respondent2DQExperts(Experts.builder()
                                              .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                              .build())
                    .build());
        }
    }

    private void resetRespondentDetailsForClaimTab(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(updatedData.build().getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }
    }

    private void handleDocumentGeneration(CallbackParams callbackParams, CaseData caseData,
                                          CaseData.CaseDataBuilder<?, ?> updatedData) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        updatedData.respondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            updatedData.respondent2DocumentGeneration("userRespondent2");
        }
        updateCorrespondenceAddress(callbackParams, updatedData, caseData);
    }

    private boolean shouldProceedToHeritageSystem(CaseData caseData) {
        return (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP && isAwaitingAnotherDefendantResponse(caseData))
            || (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP && !isAwaitingAnotherDefendantResponse(caseData)
            && (!FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())))
            || (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP && twoVsOneDivergent(caseData))
            || (getMultiPartyScenario(caseData) == TWO_V_ONE && twoVsOneDivergent(caseData));
    }

    private CallbackResponse buildResponse(CaseData.CaseDataBuilder<?, ?> updatedData, CaseState caseState) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state(caseState.name())
            .build();
    }

    private boolean ifResponseTypeIsPartOrFullAdmission(CaseData caseData) {
        return (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
            || (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), caseRole);
    }

    public void updateCorrespondenceAddress(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedCaseData, CaseData caseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            Address newAddress = caseData.getSpecAoSRespondentCorrespondenceAddressdetails();
            updatedCaseData.specRespondentCorrespondenceAddressdetails(newAddress)
                .specAoSRespondentCorrespondenceAddressdetails(Address.builder().build());
            if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP) {
                updatedCaseData.specRespondent2CorrespondenceAddressdetails(newAddress);
            }
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            updatedCaseData.specRespondent2CorrespondenceAddressdetails(
                    caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                .specAoSRespondent2CorrespondenceAddressdetails(Address.builder().build());
        }
    }

    private boolean solicitorHasCaseRole(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), caseRole);
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == null
            || caseData.getRespondent2ClaimResponseTypeForSpec() == null;
    }

    private boolean twoVsOneDivergent(CaseData caseData) {
        return (!FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
            || (!FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            && FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate) {
        return deadlinesCalculator.calculateApplicantResponseDeadlineSpec(responseDate);
    }
}
