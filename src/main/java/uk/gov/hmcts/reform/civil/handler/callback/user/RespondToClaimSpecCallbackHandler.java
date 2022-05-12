package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.*;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.HearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.DefendantAddressValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONESPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONESPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWOSPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
public class RespondToClaimSpecCallbackHandler extends CallbackHandler
    implements ExpertsValidator, WitnessesValidator, DefendantAddressValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE_SPEC);

    private final DateOfBirthValidator dateOfBirthValidator;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final PostcodeValidator postcodeValidator;
    private final PaymentDateValidator paymentDateValidator;
    private final List<RespondToClaimConfirmationTextSpecGenerator> confirmationTextSpecGenerators;
    private final List<RespondToClaimConfirmationHeaderSpecGenerator> confirmationHeaderGenerators;
    private final FeatureToggleService toggleService;
    private final UserService userService;
    private final StateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;

    @Override
    public List<CaseEvent> handledEvents() {
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::setSuperClaimType)
            .put(callbackKey(V_1, ABOUT_TO_START), this::populateRespondent1Copy)
            .put(callbackKey(MID, "confirm-details"), this::validateDateOfBirth)
            .put(callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates)
            .put(callbackKey(MID, "experts"), this::validateRespondentExperts)
            .put(callbackKey(MID, "witnesses"), this::validateRespondentWitnesses)
            .put(callbackKey(MID, "upload"), this::emptyCallbackResponse)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(MID, "validate-payment-date"), this::validateRespondentPaymentDate)
            .put(callbackKey(MID, "specCorrespondenceAddress"), this::validateCorrespondenceApplicantAddress)
            .put(callbackKey(MID, "determineLoggedInSolicitor"), this::determineLoggedInSolicitor)
            .put(callbackKey(MID, "track"), this::handleDefendAllClaim)
            .put(callbackKey(MID, "specHandleResponseType"), this::handleRespondentResponseTypeForSpec)
            .put(callbackKey(MID, "specHandleAdmitPartClaim"), this::handleAdmitPartOfClaim)
            .put(callbackKey(MID, "validate-length-of-unemployment"), this::validateLengthOfUnemployment)
            .put(callbackKey(MID, "validate-repayment-plan"), this::validateDefendant1RepaymentPlan)
            .put(callbackKey(MID, "validate-repayment-plan-2"), this::validateDefendant2RepaymentPlan)
            .put(callbackKey(MID, "set-generic-response-type-flag"), this::setGenericResponseTypeFlag)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::setApplicantResponseDeadlineV1)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse setSuperClaimType(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.superClaimType(SPEC_CLAIM);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse handleDefendAllClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToClaim())
                                                                .orElseGet(() -> RespondToClaim.builder().build()));
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            caseData = populateRespondentResponseTypeSpecPaidStatus(caseData);
            if (caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                caseData = caseData.toBuilder().specPaidLessAmountOrDisputesOrPartAdmission(YES).build();
            } else {
                caseData = caseData.toBuilder().specPaidLessAmountOrDisputesOrPartAdmission(NO).build();
            }
            if (YES.equals(caseData.getIsRespondent2())) {
                if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                    != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                    && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                    || caseData.getRespondent2ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION)) {
                    caseData = caseData.toBuilder().specDisputesOrPartAdmission(YES).build();
                } else {
                    caseData = caseData.toBuilder().specDisputesOrPartAdmission(NO).build();
                }
            } else {
                if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                    != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                    && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                    || caseData.getRespondent1ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION)) {
                    caseData = caseData.toBuilder().specDisputesOrPartAdmission(YES).build();
                } else {
                    caseData = caseData.toBuilder().specDisputesOrPartAdmission(NO).build();
                }
            }

            if (YES.equals(caseData.getIsRespondent1())
                && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT.equals(
                caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()))) {
                caseData = caseData.toBuilder().showHowToAddTimeLinePage(YES).build();
            } else if (YES.equals(caseData.getIsRespondent2())
                && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT.equals(
                caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()))) {
                caseData = caseData.toBuilder().showHowToAddTimeLinePage(YES).build();
            }
            return populateAllocatedTrack(caseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse handleAdmitPartOfClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                                                                .orElseGet(() -> RespondToClaim.builder().build()));
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            caseData = caseData.toBuilder().fullAdmissionAndFullAmountPaid(YES).build();
        } else if (YES.equals(caseData.getIsRespondent1())
            && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            caseData = caseData.toBuilder().fullAdmissionAndFullAmountPaid(YES).build();
        } else {
            caseData = caseData.toBuilder().fullAdmissionAndFullAmountPaid(NO).build();
        }

        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            caseData = caseData.toBuilder().defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).build();
        } else if (YES.equals(caseData.getIsRespondent2())
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            caseData = caseData.toBuilder().defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired2()).build();
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            caseData = caseData.toBuilder().partAdmittedByEitherRespondents(YES).build();
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            caseData = caseData.toBuilder().partAdmittedByEitherRespondents(YES).build();
        } else {
            caseData = caseData.toBuilder().partAdmittedByEitherRespondents(NO).build();
        }



        if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            caseData = caseData.toBuilder().respondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec()).build();
        } else if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            caseData = caseData.toBuilder().respondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2()).build();
        } else {
            caseData = caseData.toBuilder().respondToClaimAdmitPartEmploymentTypeLRspecGeneric(null).build();
            caseData = caseData.toBuilder().respondToClaimAdmitPartEmploymentTypeLRspec(null).build();
            caseData = caseData.toBuilder().respondToClaimAdmitPartEmploymentTypeLRspec2(null).build();
        }
        if (caseData.getRespondToAdmittedClaimOwingAmount() != null) {
            BigDecimal valuePounds = MonetaryConversions
                .penniesToPounds(caseData.getRespondToAdmittedClaimOwingAmount());
            caseData = caseData.toBuilder().respondToAdmittedClaimOwingAmountPounds(valuePounds)
                .build();
        }
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()) {
            caseData = caseData.toBuilder().specPaidLessAmountOrDisputesOrPartAdmission(YES).build();
        } else {
            caseData = caseData.toBuilder().specPaidLessAmountOrDisputesOrPartAdmission(NO).build();
        }
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)) {
            caseData = caseData.toBuilder().specDisputesOrPartAdmission(YES).build();
        } else {
            caseData = caseData.toBuilder().specDisputesOrPartAdmission(NO).build();
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            && caseData.getSpecDefenceAdmittedRequired() == NO) {
            caseData = caseData.toBuilder().specPartAdmitPaid(NO).build();
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
            && caseData.getSpecDefenceFullAdmittedRequired() == NO) {
            caseData = caseData.toBuilder().specFullAdmitPaid(NO).build();
        }
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            return populateAllocatedTrack(caseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().build().toMap(objectMapper))
            .build();
    }

    /**
     * From the responseType, if we choose A, advance for a while, then backtrack and choose B, part of our responses
     * in A stay in frontend and may influence screens that A and B have in common.
     *
     * <p>Why does that happen?
     * Frontend keeps an object with the CaseData information.
     * In mid callbacks frontend sends part of that object, which gets deserialized into an instance of CaseData.
     * We can modify that caseData, but since where using objectMapper.setSerializationInclusion(Include.NON_EMPTY)
     * we only send anything not empty, not null. That means we cannot signal frontend to "clean" info.
     * What we can do, however, is change info.</p>
     *
     * <p>For instance, the field specDefenceFullAdmittedRequired is only accessible from FULL_ADMISSION.
     * If the user went to full admission, checked specDefenceFullAdmittedRequired = yes
     * and then went back and to part admit, a bunch of screens common to both options won't appear because their
     * condition to show include that specDefenceFullAdmittedRequired != yes. So, if in this method we say that whenever
     * responseType is not full admission, then specDefenceFullAdmittedRequired = No, since that is not empty, gets sent
     * to frontend and frontend overwrites that field on its copy.</p>
     *
     * @param callbackParams parameters from frontend.
     * @return caseData cleaned from backtracked paths.
     */
    private CallbackResponse handleRespondentResponseTypeForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            caseData = caseData.toBuilder().specDefenceFullAdmittedRequired(NO).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse setGenericResponseTypeFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData =
            caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
                &&
                (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                    || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                        caseData.getClaimant2ClaimResponseTypeForSpec()))) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            }
        } else if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            if (YES.equals(caseData.getIsRespondent1())
                && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                    caseData.getRespondent1ClaimResponseTypeForSpec()))) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (YES.equals(caseData.getIsRespondent2())
                && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                    caseData.getRespondent2ClaimResponseTypeForSpec()))) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            }

            if (YES.equals(caseData.getIsRespondent1())
                && RespondentResponseTypeSpec.PART_ADMISSION.equals(
                caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
            } else if (YES.equals(caseData.getIsRespondent2())
                && RespondentResponseTypeSpec.PART_ADMISSION.equals(
                caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
            }
        } else if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            if (caseData.getRespondentResponseIsSame().equals(NO)) {
                updatedData.sameSolicitorSameResponse(NO);
            }
        } else if (ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            //this logic to be removed when ccd supports AND-OR combinations
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.COUNTER_CLAIM) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            }
        }

        if (YES.equals(caseData.getIsRespondent1()) && caseData.getRespondent1ClaimResponseTypeForSpec()
            == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else if (YES.equals(caseData.getIsRespondent2()) && caseData.getRespondent2ClaimResponseTypeForSpec()
            == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
        }

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && caseData.getRespondentResponseIsSame().equals(NO)) {
            updatedData.sameSolicitorSameResponse(NO);
            if (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(RespondentResponseTypeSpec.FULL_DEFENCE);
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && caseData.getRespondentResponseIsSame().equals(YES)) {
            updatedData.sameSolicitorSameResponse(YES);
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            if (RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            }
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        }

        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            if (coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                RESPONDENTSOLICITORTWOSPEC
            )) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(
                    caseData.getRespondent2ClaimResponseTypeForSpec());
            } else {
                updatedData.respondentClaimResponseTypeForSpecGeneric(
                    caseData.getRespondent1ClaimResponseTypeForSpec());
            }

        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }

        if (YES.equals(caseData.getIsRespondent1())
            &&  (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else if (YES.equals(caseData.getIsRespondent2())
            && (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        }
        if (YES.equals(caseData.getIsRespondent2())) {
            if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                                                            caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            }
        } else {
            if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                                                        caseData.getRespondent2ClaimResponseTypeForSpec()))) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            }
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission(YES);
        } else {
            updatedData.specFullDefenceOrPartAdmission(NO);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specDefenceFullAdmittedRequired(NO);
        }

        if (YES.equals(caseData.getIsRespondent1())
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))) {
            updatedData.showHowToAddTimeLinePage(NO);
        } else if (YES.equals(caseData.getIsRespondent2())
            &&  (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
            || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.showHowToAddTimeLinePage(NO);
        } else {
            updatedData.showHowToAddTimeLinePage(YES);
        }

        if (YES.equals(caseData.getIsRespondent1())
            && RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedData.showStatementOfTruth(NO);
        } else if (YES.equals(caseData.getIsRespondent2())
            && RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.showStatementOfTruth(NO);
        } else {
            updatedData.showStatementOfTruth(YES);
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedData.partAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedData.partAdmittedByEitherRespondents(NO);
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1())
            && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedData.fullAdmissionAndFullAmountPaid(NO);
        }

        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2())
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        } else {
            //workaround
            updatedData.defenceAdmitPartPaymentTimeRouteGeneric(
                RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CaseData updateCurrentPage(CaseData caseData) {
        caseData = caseData.toBuilder().respondent1ClaimResponsePaymentAdmissionForSpec(null).build();

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            caseData = caseData.toBuilder().defendantResponseLRspecCurrentPage(DefendantResponseLRspecCurrentPage.RespondentResponseTypeSpec).build();
        }
        return caseData;
    }

    private CaseData populateRespondentResponseTypeSpecPaidStatus(CaseData caseData) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
            int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
                .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                caseData = caseData.toBuilder()
                    .respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            } else {
                caseData = caseData.toBuilder()
                    .respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            }
        } else {
            caseData = caseData.toBuilder()
                .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY)
                .build();
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
                && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
                // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
                int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
                if (comparison < 0) {
                    caseData = caseData.toBuilder()
                        .respondent1ClaimResponsePaymentAdmissionForSpec(
                            RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
                } else {
                    caseData = caseData.toBuilder()
                        .respondent1ClaimResponsePaymentAdmissionForSpec(
                            RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
                }
            } else {
                caseData = caseData.toBuilder().respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
            }
        }
        return caseData;
    }

    private CallbackResponse populateAllocatedTrack(CaseData caseData) {
        AllocatedTrack allocatedTrack = AllocatedTrack.getAllocatedTrack(
            caseData.getTotalClaimAmount(),
            null
        );
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().responseClaimTrack(allocatedTrack.name()).build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateCorrespondenceApplicantAddress(CallbackParams callbackParams) {
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            return validateCorrespondenceApplicantAddress(callbackParams, postcodeValidator);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse determineLoggedInSolicitor(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        caseData = updateCurrentPage(caseData);

        var updatedCaseData = caseData.toBuilder();
        updatedCaseData.respondentResponseIsSame(NO);
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONESPEC)) {
            updatedCaseData.isRespondent1(YES);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWOSPEC)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(YES);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONESPEC)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(YES);
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            if (caseData.getRespondent2DetailsForClaimDetailsTab() != null
                && ("Company".equals(caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
                || "Organisation".equals(
                    caseData.getRespondent2DetailsForClaimDetailsTab().getPartyTypeDisplayValue()))) {
                updatedCaseData.neitherCompanyNorOrganisation(NO);
            } else {
                updatedCaseData.neitherCompanyNorOrganisation(YES);
            }
        } else {
            if ((caseData.getRespondent1DetailsForClaimDetailsTab() != null
                && ("Company".equals(caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue())
                || "Organisation".equals(
                    caseData.getRespondent1DetailsForClaimDetailsTab().getPartyTypeDisplayValue())))) {
                updatedCaseData.neitherCompanyNorOrganisation(NO);
            } else {
                updatedCaseData.neitherCompanyNorOrganisation(YES);
            }
        }
        if (!ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
        || !TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            updatedCaseData.sameSolicitorSameResponse(NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse populateRespondent1Copy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1());

        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1());

        ofNullable(caseData.getRespondent2()).ifPresent(r2 ->
            updatedCaseData.respondent2Copy(r2).respondent2DetailsForClaimDetailsTab(r2)
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateRespondentWitnesses(CallbackParams callbackParams) {
        return validateWitnesses(callbackParams.getCaseData().getRespondent1DQ());
    }

    private CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        // UnavailableDates validation & field (model) needs to be created.
        // This will be taken care via different story,
        // because we don't have AC around this date field validation in ROC-9455
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors;
        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            SmallClaimHearing smallClaimHearing = caseData.getRespondent1DQ().getRespondent1DQHearingSmallClaim();
            if (YES.equals(caseData.getIsRespondent2())) {
                smallClaimHearing = caseData.getRespondent2DQ().getRespondent2DQHearingSmallClaim();
            }
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);

        } else {
            HearingLRspec hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        if (respondent == null && callbackParams.getCaseData().getRespondent2() != null) {
            respondent = callbackParams.getCaseData().getRespondent2();
        }
        List<String> errors = dateOfBirthValidator.validate(respondent);

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getAddRespondent2())) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWOSPEC)) {
                //work around: treat this as if it was a 1v2 same solicitor single response
                updatedData.sameSolicitorSameResponse(NO).build();
            } else {
                updatedData.sameSolicitorSameResponse(YES).build();
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getAddRespondent2())) {
            if (NO.equals(caseData.getRespondentResponseIsSame())) {
                updatedData.sameSolicitorSameResponse(NO).build();
            } else {
                updatedData.sameSolicitorSameResponse(YES).build();
            }

        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // resetting statement of truth field, this resets in the page, but the data is still sent to the db.
        // setting null here does not clear, need to overwrite with value.
        // must be to do with the way XUI cache data entered through the lifecycle of an event.
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(StatementOfTruth.builder().role("").build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse setApplicantResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        Party updatedRespondent1;

        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails()).build();
        } else {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress()).build();
        }

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        updatedData.respondent1DetailsForClaimDetailsTab(updatedRespondent1);

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2);
        }

        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWOSPEC)) {
            updatedData.respondent2ResponseDate(responseDate)
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

            if (caseData.getRespondent1ResponseDate() != null) {
                updatedData
                    .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));
            }

            // 1v1, 2v1
            // represents 1st respondent - need to set deadline if only 1 respondent,
            // or wait for 2nd respondent response before setting deadline
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent2DQ dq = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth)
                .build();

            updatedData.respondent2DQ(dq);
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        } else {
            updatedData = caseData.toBuilder()
                .respondent1ResponseDate(responseDate)
                .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack))
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

            updatedData.respondent1DetailsForClaimDetailsTab(updatedRespondent1);

            if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
                Party updatedRespondent2;

                if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
                    updatedRespondent2 = caseData.getRespondent2().toBuilder()
                        .primaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails()).build();
                } else {
                    updatedRespondent2 = caseData.getRespondent2().toBuilder()
                        .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress()).build();
                }

                updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
                updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2);
            }

            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth)
                .build();

            updatedData.respondent1DQ(dq);
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        } else if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && !isAwaitingAnotherDefendantResponse(caseData)) {
            if (!RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || !RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(updatedData.build().toMap(objectMapper))
                    .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                    .build();
            }
        } else if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                .build();
        } else if (getMultiPartyScenario(caseData) == TWO_V_ONE && twoVsOneDivergent(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name())
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state(CaseState.AWAITING_APPLICANT_INTENTION.name())
            .build();
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == null
            || caseData.getRespondent2ClaimResponseTypeForSpec() == null;
    }

    private boolean twoVsOneDivergent(CaseData caseData) {
        return (!RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))
            || (!RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
            && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getClaimant1ClaimResponseTypeForSpec()));
    }

    private CallbackResponse setApplicantResponseDeadlineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1ResponseDate(responseDate)
            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack))
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

        // moving statement of truth value to correct field, this was not possible in mid event.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth)
            .build();

        updatedData.respondent1DQ(dq);
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
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

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate, AllocatedTrack allocatedTrack) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate, allocatedTrack);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = CaseDataToTextGenerator.getTextFor(
            confirmationTextSpecGenerators.stream(),
            () -> getDefaultConfirmationBody(caseData),
            caseData
        );

        String header = CaseDataToTextGenerator.getTextFor(
            confirmationHeaderGenerators.stream(),
            () -> format("# You have submitted your response%n## Claim number: %s", claimNumber),
            caseData
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }

    private String getDefaultConfirmationBody(CaseData caseData) {
        //catch scenario 1v2 Diff Sol - 1 Response Received
        //responseDeadline has not been set yet
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "%n%nThe claimant has until 4pm on %s to respond to your claim. "
                + "We will let you know when they respond."
                + "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>";
        }

        LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
        return format(
            "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "%n%nThe claimant has until 4pm on %s to respond to your claim. "
                + "We will let you know when they respond."
                + "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
            formatLocalDateTime(responseDeadline, DATE),
            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
        );
    }

    private CallbackResponse validateRespondentPaymentDate(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = paymentDateValidator
            .validate(Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                          .orElseGet(() -> RespondToClaimAdmitPartLRspec.builder().build()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateLengthOfUnemployment(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getRespondToClaimAdmitPartUnemployedLRspec() != null
            && caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment() != null) {
            if (caseData.getRespondToClaimAdmitPartUnemployedLRspec().getLengthOfUnemployment()
                .getNumberOfYearsInUnemployment().contains(".")
                || caseData.getRespondToClaimAdmitPartUnemployedLRspec()
                .getLengthOfUnemployment().getNumberOfMonthsInUnemployment().contains(".")) {
                errors.add("Length of time unemployed must be a whole number, for example, 10.");
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateDefendant1RepaymentPlan(CallbackParams callbackParams) {
        return validateRepaymentPlan(callbackParams.getCaseData().getRespondent1RepaymentPlan());
    }

    private CallbackResponse validateDefendant2RepaymentPlan(CallbackParams callbackParams) {
        return validateRepaymentPlan(callbackParams.getCaseData().getRespondent2RepaymentPlan());
    }

    private CallbackResponse validateRepaymentPlan(RepaymentPlanLRspec repaymentPlan) {
        List<String> errors;

        if (repaymentPlan != null
            && repaymentPlan.getFirstRepaymentDate() != null) {
            errors = unavailableDateValidator.validateFuturePaymentDate(repaymentPlan
                                                                            .getFirstRepaymentDate());
        } else {
            errors = new ArrayList<>();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
