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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.DefendantAddressValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_MANUALLY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.TIMELINE_UPLOAD;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateWithPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

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
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final AssignCategoryId assignCategoryId;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateRespondent1Copy)
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
            .put(callbackKey(MID, "set-upload-timeline-type-flag"), this::setUploadTimelineTypeFlag)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
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
        CaseData.CaseDataBuilder<?, ?> updatedCase = caseData.toBuilder();
        updatedCase.showConditionFlags(whoDisputesFullDefence(caseData));
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase);
            if (caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(YES);
            } else {
                updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(NO);
            }
            if (YES.equals(caseData.getIsRespondent2())) {
                if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                    != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                    && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                    || caseData.getRespondent2ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION)) {
                    updatedCase.specDisputesOrPartAdmission(YES);
                } else {
                    updatedCase.specDisputesOrPartAdmission(NO);
                }
            } else {
                if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                    != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                    && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                    || caseData.getRespondent1ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION)) {
                    updatedCase.specDisputesOrPartAdmission(YES);
                } else {
                    updatedCase.specDisputesOrPartAdmission(NO);
                }
            }
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCase.responseClaimTrack(allocatedTrack.name());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCase.build().toMap(objectMapper))
            .build();
    }

    // called on full_admit, also called after whenWillClaimBePaid
    private CallbackResponse handleAdmitPartOfClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                                                                .orElseGet(() -> RespondToClaim.builder().build()));
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceFullAdmitted2Required())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else if (YES.equals(caseData.getIsRespondent1())
            && YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            updatedCaseData.fullAdmissionAndFullAmountPaid(YES);
        } else {
            updatedCaseData.fullAdmissionAndFullAmountPaid(NO);
        }

        if (YES.equals(caseData.getIsRespondent1()) && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
        } else if (YES.equals(caseData.getIsRespondent2())
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != null) {
            updatedCaseData.defenceAdmitPartPaymentTimeRouteGeneric(
                caseData.getDefenceAdmitPartPaymentTimeRouteRequired2());
        }

        if (YES.equals(caseData.getIsRespondent2()) && YES.equals(caseData.getSpecDefenceAdmitted2Required())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else if (YES.equals(caseData.getIsRespondent1()) && YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            updatedCaseData.partAdmittedByEitherRespondents(YES);
        } else {
            updatedCaseData.partAdmittedByEitherRespondents(NO);
        }

        if (YES.equals(caseData.getDefenceAdmitPartEmploymentTypeRequired())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec());
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
        }
        if (caseData.getRespondToAdmittedClaimOwingAmount() != null) {
            BigDecimal valuePounds = MonetaryConversions
                .penniesToPounds(caseData.getRespondToAdmittedClaimOwingAmount());
            updatedCaseData.respondToAdmittedClaimOwingAmountPounds(valuePounds);
        }
        if (YES.equals(caseData.getDefenceAdmitPartEmploymentType2Required())) {
            updatedCaseData.respondToClaimAdmitPartEmploymentTypeLRspecGeneric(
                caseData.getRespondToClaimAdmitPartEmploymentTypeLRspec2());
        }
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds);
        Optional.ofNullable(caseData.getRespondToAdmittedClaimOwingAmount2())
            .map(MonetaryConversions::penniesToPounds)
            .ifPresent(updatedCaseData::respondToAdmittedClaimOwingAmountPounds2);
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            == caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()) {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
        if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)) {
            updatedCaseData.specDisputesOrPartAdmission(YES);
        } else {
            updatedCaseData.specDisputesOrPartAdmission(NO);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            && caseData.getSpecDefenceAdmittedRequired() == NO) {
            updatedCaseData.specPartAdmitPaid(NO);
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
            && caseData.getSpecDefenceFullAdmittedRequired() == NO) {
            updatedCaseData.specFullAdmitPaid(NO);
        }
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCaseData.responseClaimTrack(allocatedTrack.name());
        }
        Set<DefendantResponseShowTag> currentShowFlags = new HashSet<>(caseData.getShowConditionFlags());
        currentShowFlags.removeAll(EnumSet.of(
            NEED_FINANCIAL_DETAILS_1,
            NEED_FINANCIAL_DETAILS_2,
            WHY_1_DOES_NOT_PAY_IMMEDIATELY,
            WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            WHEN_WILL_CLAIM_BE_PAID
        ));
        currentShowFlags.addAll(checkNecessaryFinancialDetails(caseData));
        if (mustWhenWillClaimBePaidBeShown(caseData)) {
            currentShowFlags.add(WHEN_WILL_CLAIM_BE_PAID);
        }
        updatedCaseData.showConditionFlags(currentShowFlags);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private Set<DefendantResponseShowTag> checkNecessaryFinancialDetails(CaseData caseData) {
        Set<DefendantResponseShowTag> necessary = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            if (caseData.getRespondent1().getType() != Party.Type.COMPANY
                && caseData.getRespondent1().getType() != Party.Type.ORGANISATION) {
                if (needFinancialInfo1(caseData)) {
                    necessary.add(NEED_FINANCIAL_DETAILS_1);
                }
            }

            if (respondent1doesNotPayImmediately(caseData, scenario)) {
                necessary.add(WHY_1_DOES_NOT_PAY_IMMEDIATELY);
            }
        }

        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            if (caseData.getRespondent2().getType() != Party.Type.COMPANY
                && caseData.getRespondent2().getType() != Party.Type.ORGANISATION) {
                if (scenario == ONE_V_TWO_TWO_LEGAL_REP) {
                    if (needFinancialInfo21v2ds(caseData)) {
                        necessary.add(NEED_FINANCIAL_DETAILS_2);
                    }
                } else if (scenario == ONE_V_TWO_ONE_LEGAL_REP
                    && ((caseData.getRespondentResponseIsSame() != YES && needFinancialInfo21v2ds(caseData))
                    || (needFinancialInfo1(caseData) && caseData.getRespondentResponseIsSame() == YES))) {
                    necessary.add(NEED_FINANCIAL_DETAILS_2);
                }

                if (respondent2doesNotPayImmediately(caseData, scenario)) {
                    necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
                }
            }

            if (respondent2doesNotPayImmediately(caseData, scenario)) {
                necessary.add(WHY_2_DOES_NOT_PAY_IMMEDIATELY);
            }

            if ((caseData.getRespondentResponseIsSame() == YES
                && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
                || caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() == SUGGESTION_OF_REPAYMENT_PLAN) {
                necessary.add(REPAYMENT_PLAN_2);
            }
        }

        return necessary;
    }

    private boolean respondent1doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        if (YES.equals(caseData.getIsRespondent1())
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE) {
            if (scenario != ONE_V_TWO_ONE_LEGAL_REP || caseData.getRespondentResponseIsSame() == YES) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmittedRequired() != YES
                    && caseData.getSpecDefenceAdmittedRequired() != YES;
            }
        }
        return false;
    }

    private boolean respondent2doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        if (caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE) {
            if (scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmittedRequired() != YES
                    && caseData.getSpecDefenceAdmittedRequired() != YES;
            } else if (caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmitted2Required() != YES
                    && caseData.getSpecDefenceAdmitted2Required() != YES;
            }
        }
        return false;
    }

    /**
     * this condition has been copied from ccd's on the moment of writing.
     *
     * @param caseData the case data
     * @return true if the financial details for r1 are needed. Doesn't consider if the r1
     */
    private boolean needFinancialInfo1(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
            && caseData.getSpecDefenceAdmittedRequired() != YES
            && caseData.getSpecDefenceFullAdmittedRequired() != YES
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM
            && caseData.getMultiPartyResponseTypeFlags() != MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART
            && (caseData.getSameSolicitorSameResponse() != NO
            || MultiPartyScenario.getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP)
            && caseData.getDefendantSingleResponseToBothClaimants() != NO;
    }

    /**
     * Adapts the conditions from needFinancialInfo1 for use with 2nd solicitor.
     *
     * @param caseData case data
     * @return true if the financial details for 2nd defendant are needed, in a 1v2 different solicitor claim.
     */
    private boolean needFinancialInfo21v2ds(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
            && caseData.getSpecDefenceAdmitted2Required() != YES
            && caseData.getSpecDefenceFullAdmitted2Required() != YES
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
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

        if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
            &&
            (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
        //this logic to be removed when ccd supports AND-OR combinations
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (ONE_V_ONE.equals(multiPartyScenario)) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.COUNTER_CLAIM) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION);
            }
        }

        Set<RespondentResponseTypeSpec> someAdmission = EnumSet.of(PART_ADMISSION, FULL_ADMISSION);
        if (TWO_V_ONE.equals(multiPartyScenario)
            && someAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && someAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else {
            updatedData.specFullAdmissionOrPartAdmission(NO);
        }

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)
            && Objects.equals(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getRespondent2ClaimResponseTypeForSpec()
        )) {
            updatedData.respondentResponseIsSame(YES);
            caseData = caseData.toBuilder()
                .respondentResponseIsSame(YES)
                .build();
        }
        if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)
            && caseData.getRespondentResponseIsSame().equals(NO)) {
            updatedData.sameSolicitorSameResponse(NO);
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(FULL_DEFENCE);
            }
        } else if (ONE_V_TWO_ONE_LEGAL_REP.equals(multiPartyScenario)
            && caseData.getRespondentResponseIsSame().equals(YES)) {
            updatedData.sameSolicitorSameResponse(YES);
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
            if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
            }
        } else {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent1ClaimResponseTypeForSpec());
        }

        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            if (coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                RESPONDENTSOLICITORTWO
            )) {
                updatedData.respondentClaimResponseTypeForSpecGeneric(
                    caseData.getRespondent2ClaimResponseTypeForSpec());
            } else {
                updatedData.respondentClaimResponseTypeForSpecGeneric(
                    caseData.getRespondent1ClaimResponseTypeForSpec());
            }
        }

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            if (YES.equals(caseData.getIsRespondent1())
                && RespondentResponseTypeSpec.PART_ADMISSION.equals(
                caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
            } else if (YES.equals(caseData.getIsRespondent2())
                && RespondentResponseTypeSpec.PART_ADMISSION.equals(
                caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION);
            }
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            updatedData.respondentClaimResponseTypeForSpecGeneric(caseData.getRespondent2ClaimResponseTypeForSpec());
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE);
        }

        if (YES.equals(caseData.getIsRespondent1())
            && (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        } else if (YES.equals(caseData.getIsRespondent2())
            && (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION)) {
            updatedData.specFullAdmissionOrPartAdmission(YES);
        }
        if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission(YES);
        } else {
            updatedData.specFullDefenceOrPartAdmission(NO);
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specDefenceFullAdmittedRequired(NO);
        }

        if (YES.equals(caseData.getSpecPaidLessAmountOrDisputesOrPartAdmission())
            && !MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART
            .equals(caseData.getMultiPartyResponseTypeFlags())
            && (!RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT
            .equals(caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()))) {
            updatedData.showHowToAddTimeLinePage(YES);
        }

        if (YES.equals(caseData.getIsRespondent1())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (RespondentResponseTypeSpec.FULL_ADMISSION
                .equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
            }
        } else if (YES.equals(caseData.getIsRespondent2())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART);
            } else if (RespondentResponseTypeSpec.FULL_ADMISSION
                .equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
                updatedData.showHowToAddTimeLinePage(NO);
            }
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
                IMMEDIATELY);
        }

        Set<DefendantResponseShowTag> updatedShowConditions = whoDisputesPartAdmission(caseData);
        EnumSet<RespondentResponseTypeSpec> anyAdmission = EnumSet.of(
            RespondentResponseTypeSpec.PART_ADMISSION,
            RespondentResponseTypeSpec.FULL_ADMISSION
        );
        if (updatedShowConditions.contains(CAN_ANSWER_RESPONDENT_1)
            && anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_1_ADMITS_PART_OR_FULL);
            if (caseData.getRespondentResponseIsSame() == YES) {
                updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
            }
        }
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedShowConditions.add(RESPONDENT_2_ADMITS_PART_OR_FULL);
        }
        if (someoneDisputes(caseData)) {
            updatedShowConditions.add(SOMEONE_DISPUTES);
        }
        if ((anyAdmission.contains(caseData.getRespondent1ClaimResponseTypeForSpec())
            && YES.equals(caseData.getIsRespondent1()))
            || (anyAdmission.contains(caseData.getRespondent2ClaimResponseTypeForSpec())
            && YES.equals(caseData.getIsRespondent2()))) {
            updatedShowConditions.removeIf(EnumSet.of(
                CURRENT_ADMITS_PART_OR_FULL
            )::contains);
            updatedShowConditions.add(CURRENT_ADMITS_PART_OR_FULL);
        }
        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse setUploadTimelineTypeFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        Set<DefendantResponseShowTag> updatedShowConditions = new HashSet<>(caseData.getShowConditionFlags());
        updatedShowConditions.removeIf(EnumSet.of(
            TIMELINE_UPLOAD,
            TIMELINE_MANUALLY
        )::contains);

        if ((YES.equals(caseData.getIsRespondent1())
            && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.UPLOAD)
            || (YES.equals(caseData.getIsRespondent2())
            && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.UPLOAD)) {
            updatedShowConditions.add(TIMELINE_UPLOAD);
        } else if ((YES.equals(caseData.getIsRespondent1())
            && caseData.getSpecClaimResponseTimelineList() == TimelineUploadTypeSpec.MANUAL)
            || (YES.equals(caseData.getIsRespondent2())
            && caseData.getSpecClaimResponseTimelineList2() == TimelineUploadTypeSpec.MANUAL)) {
            updatedShowConditions.add(TIMELINE_MANUALLY);
        }
        updatedData.showConditionFlags(updatedShowConditions);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    /**
     * The condition to show the right title for why does X disputes the claim is too complex for the current
     * abilities of front, so we have to take care of it in back.
     * This method may add the flags only_respondent_1_disputes, only_respondent_2_disputes or both_respondent_dispute.
     *
     * @param caseData the current case data
     * @return updated copy of caseData.showConditionFlag
     */
    private Set<DefendantResponseShowTag> whoDisputesPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        removeWhoDisputesAndWhoPaidLess(tags);
        tags.addAll(whoDisputesBcoPartAdmission(caseData));
        return tags;
    }

    /**
     * Returns the flags that should be active because part admission has been chosen.
     *
     * @param caseData the claim info
     * @return flags to describe who disputes because a response is part admission
     */
    private Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        switch (mpScenario) {
            case ONE_V_ONE:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case TWO_V_ONE:
                if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
                    || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                    || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    if (caseData.getRespondentResponseIsSame() == YES
                        || caseData.getRespondent2ClaimResponseTypeForSpec()
                        == RespondentResponseTypeSpec.PART_ADMISSION) {
                        tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
                    } else {
                        tags.add(ONLY_RESPONDENT_1_DISPUTES);
                    }
                } else if (caseData.getRespondent2ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
                }
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
                    && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown mp scenario");
        }
        return tags;
    }

    private boolean someoneDisputes(CaseData caseData) {
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return ((caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE)
                || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION);
        } else {
            return someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_1,
                                   caseData.getRespondent1ClaimResponseTypeForSpec()
            )
                || someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_2,
                                   caseData.getRespondent2ClaimResponseTypeForSpec()
            );
        }
    }

    private boolean someoneDisputes(CaseData caseData, DefendantResponseShowTag respondent,
                                    RespondentResponseTypeSpec response) {
        return caseData.getShowConditionFlags().contains(respondent)
            && (response == FULL_DEFENCE
            || (response == PART_ADMISSION && !NO.equals(caseData.getRespondentResponseIsSame())));
    }

    private Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        // in case of backtracking
        removeWhoDisputesAndWhoPaidLess(tags);
        Set<DefendantResponseShowTag> bcoPartAdmission = whoDisputesBcoPartAdmission(caseData);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        switch (mpScenario) {
            case ONE_V_ONE:
                fullDefenceAndPaidLess(
                    caseData.getRespondent1ClaimResponseTypeForSpec(),
                    caseData.getDefenceRouteRequired(),
                    caseData.getRespondToClaim(),
                    caseData.getTotalClaimAmount(),
                    ONLY_RESPONDENT_1_DISPUTES,
                    DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                ).ifPresent(bcoPartAdmission::add);
                break;
            case TWO_V_ONE:
                if (!bcoPartAdmission.contains(ONLY_RESPONDENT_1_DISPUTES)) {
                    if (caseData.getDefendantSingleResponseToBothClaimants() == YES) {
                        fullDefenceAndPaidLess(
                            caseData.getRespondent1ClaimResponseTypeForSpec(),
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            ONLY_RESPONDENT_1_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);
                    } else {
                        fullDefenceAndPaidLess(
                            caseData.getClaimant1ClaimResponseTypeForSpec(),
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            ONLY_RESPONDENT_1_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);

                        fullDefenceAndPaidLess(
                            caseData.getClaimant2ClaimResponseTypeForSpec(),
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            ONLY_RESPONDENT_1_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);
                    }
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (caseData.getRespondentResponseIsSame() == YES) {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent1ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired(),
                        caseData.getRespondToClaim(),
                        caseData.getTotalClaimAmount(),
                        BOTH_RESPONDENTS_DISPUTE,
                        DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                    ).ifPresent(tag -> {
                        bcoPartAdmission.add(tag);
                        if (tag == DefendantResponseShowTag.RESPONDENT_1_PAID_LESS) {
                            bcoPartAdmission.add(DefendantResponseShowTag.RESPONDENT_2_PAID_LESS);
                        }
                    });
                } else {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent1ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired(),
                        caseData.getRespondToClaim(),
                        caseData.getTotalClaimAmount(),
                        ONLY_RESPONDENT_1_DISPUTES,
                        DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                    ).ifPresent(bcoPartAdmission::add);
                    if (caseData.getRespondentResponseIsSame() == YES) {
                        if (bcoPartAdmission.contains(RESPONDENT_1_PAID_LESS)) {
                            bcoPartAdmission.add(RESPONDENT_2_PAID_LESS);
                        }
                    } else {
                        fullDefenceAndPaidLess(
                            caseData.getRespondent2ClaimResponseTypeForSpec(),
                            // if only 2nd defends, defenceRouteRequired2 field is not used
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);
                    }
                    EnumSet<DefendantResponseShowTag> bothOnlyDisputes = EnumSet.of(
                        DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES,
                        DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES
                    );
                    if (bcoPartAdmission.containsAll(bothOnlyDisputes)) {
                        bcoPartAdmission.removeAll(bothOnlyDisputes);
                        bcoPartAdmission.add(BOTH_RESPONDENTS_DISPUTE);
                    }
                }
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)) {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent1ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired(),
                        caseData.getRespondToClaim(),
                        caseData.getTotalClaimAmount(),
                        ONLY_RESPONDENT_1_DISPUTES,
                        DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                    ).ifPresent(bcoPartAdmission::add);
                } else if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)) {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent2ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired2(),
                        caseData.getRespondToClaim2(),
                        caseData.getTotalClaimAmount(),
                        DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                        DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
                    ).ifPresent(bcoPartAdmission::add);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown mp scenario");
        }
        tags.addAll(bcoPartAdmission);
        if (tags.contains(ONLY_RESPONDENT_1_DISPUTES)
            || tags.contains(ONLY_RESPONDENT_2_DISPUTES)
            || tags.contains(BOTH_RESPONDENTS_DISPUTE)) {
            tags.add(SOMEONE_DISPUTES);
        }
        return tags;
    }

    private void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
        tags.removeIf(EnumSet.of(
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
            DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE,
            SOMEONE_DISPUTES,
            DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS,
            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS,
            WHEN_WILL_CLAIM_BE_PAID,
            RESPONDENT_1_ADMITS_PART_OR_FULL,
            RESPONDENT_2_ADMITS_PART_OR_FULL,
            NEED_FINANCIAL_DETAILS_1,
            NEED_FINANCIAL_DETAILS_2,
            DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY,
            WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            REPAYMENT_PLAN_2,
            DefendantResponseShowTag.MEDIATION
        )::contains);
    }

    private Optional<DefendantResponseShowTag> fullDefenceAndPaidLess(
        RespondentResponseTypeSpec responseType,
        String fullDefenceRoute,
        RespondToClaim responseDetails,
        BigDecimal claimedAmount,
        DefendantResponseShowTag ifDisputing,
        DefendantResponseShowTag ifPaidLess) {
        if (FULL_DEFENCE == responseType) {
            if (DISPUTES_THE_CLAIM.equals(fullDefenceRoute)) {
                return Optional.ofNullable(ifDisputing);
            } else if (Optional.ofNullable(responseDetails)
                .map(RespondToClaim::getHowMuchWasPaid)
                .map(MonetaryConversions::penniesToPounds)
                .map(wasPaid1 -> wasPaid1.compareTo(claimedAmount) < 0)
                .orElse(false)) {
                return Optional.ofNullable(ifPaidLess);
            }
        }
        return Optional.empty();
    }

    private void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData,
                                                              CaseData.CaseDataBuilder<?, ?> updated) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
            int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
                .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                    RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            } else {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                    RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            }
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY)
                .build();
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
                && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
                // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
                int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
                if (comparison < 0) {
                    updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
                } else {
                    updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
                }
            } else {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
            }
        }
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(
            caseData.getTotalClaimAmount(),
            null
        );
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

        var updatedCaseData = caseData.toBuilder();
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)) {
            updatedCaseData.isRespondent1(YES);
            updatedCaseData.isRespondent2(NO);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedCaseData.isRespondent1(NO);
            updatedCaseData.isRespondent2(YES);
            updatedCaseData.isApplicant1(NO);
        } else if (solicitorHasCaseRole(callbackParams, APPLICANTSOLICITORONE)) {
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

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse populateRespondent1Copy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        Set<DefendantResponseShowTag> initialShowTags = getInitialShowTags(callbackParams);
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .respondent1ClaimResponseTestForSpec(caseData.getRespondent1ClaimResponseTypeForSpec())
            .respondent2ClaimResponseTestForSpec(caseData.getRespondent2ClaimResponseTypeForSpec())
            .showConditionFlags(initialShowTags);

        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build());

        ofNullable(caseData.getRespondent2())
            .ifPresent(r2 -> updatedCaseData.respondent2Copy(r2)
                .respondent2DetailsForClaimDetailsTab(r2.toBuilder().flags(null).build())
            );

        DynamicList courtLocationList = courtLocationUtils.getLocationsFromList(fetchLocationData(callbackParams));
        if (initialShowTags.contains(CAN_ANSWER_RESPONDENT_1)) {
            updatedCaseData.respondent1DQ(Respondent1DQ.builder()
                                              .respondToCourtLocation(
                                                  RequestedCourt.builder()
                                                      .responseCourtLocations(courtLocationList)
                                                      .build())
                                              .build());
        }
        if (initialShowTags.contains(CAN_ANSWER_RESPONDENT_2)) {
            updatedCaseData.respondent2DQ(Respondent2DQ.builder()
                                              .respondToCourtLocation2(
                                                  RequestedCourt.builder()
                                                      .responseCourtLocations(courtLocationList)
                                                      .build())
                                              .build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private Set<DefendantResponseShowTag> getInitialShowTags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        Set<DefendantResponseShowTag> set = EnumSet.noneOf(DefendantResponseShowTag.class);
        switch (mpScenario) {
            case ONE_V_ONE:
            case TWO_V_ONE:
                set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1);
                set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
                List<String> roles = coreCaseUserService.getUserCaseRoles(
                    callbackParams.getCaseData().getCcdCaseReference().toString(),
                    userInfo.getUid()
                );
                if (roles.contains(RESPONDENTSOLICITORONE.getFormattedName())) {
                    set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1);
                }
                if (roles.contains(RESPONDENTSOLICITORTWO.getFormattedName())) {
                    set.add(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown mp scenario");
        }
        return set;
    }

    private CallbackResponse validateRespondentWitnesses(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                return validateR1Witnesses(caseData);
            } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
                return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
            } else if (respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if (caseData.getRespondent2DQ() != null
                        && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null) {
                        return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
                    }
                }
            }
        }
        return validateR1Witnesses(caseData);
    }

    private CallbackResponse validateR1Witnesses(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getRespondent1DQWitnessesRequiredSpec() == YES
            && caseData.getRespondent1DQWitnessesDetailsSpec() == null) {
            errors.add("Witness details required");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
            } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
                return validateExperts(callbackParams.getCaseData().getRespondent2DQ());
            } else if (respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if (caseData.getRespondent2DQ() != null
                        && caseData.getRespondent2DQ().getRespondent2DQExperts() != null) {
                        return validateExperts(callbackParams.getCaseData().getRespondent2DQ());
                    }
                }
            }
        }
        return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
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
            Hearing hearingLRspec = caseData.getRespondent1DQ().getRespondent1DQHearingFastClaim();
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
        errors.addAll(correspondenceAddressCorrect(caseData));
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getAddRespondent2())) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                && solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                updatedData.sameSolicitorSameResponse(YES).build();
            } else {
                updatedData.sameSolicitorSameResponse(NO).build();
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

    /**
     * Checks that the address of case data was ok when the applicant set it, or that its postcode is correct
     * if the defendant has modified.
     *
     * @param caseData the case data
     * @return errors of the correspondence address (if any)
     */
    private List<String> correspondenceAddressCorrect(CaseData caseData) {
        if (caseData.getIsRespondent1() == YesOrNo.YES
            && caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            return postcodeValidator.validate(
                Optional.ofNullable(caseData.getSpecAoSRespondentCorrespondenceAddressdetails())
                    .map(Address::getPostCode)
                    .orElse(null)
            );
        } else if (caseData.getIsRespondent2() == YesOrNo.YES
            && caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            return postcodeValidator.validate(
                Optional.ofNullable(caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                    .map(Address::getPostCode)
                    .orElse(null)
            );
        }
        return Collections.emptyList();
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
                .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
                .flags(caseData.getRespondent1Copy().getFlags())
                .build();
        }

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        if (respondent2HasSameLegalRep(caseData)) {
            // if responses are marked as same, copy respondent 1 values into respondent 2
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                updatedData.respondent2ClaimResponseTypeForSpec(caseData.getRespondent1ClaimResponseTypeForSpec());
                updatedData
                    .respondent2ResponseDate(responseDate);
            }
        }

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .flags(caseData.getRespondent2Copy().getFlags())
                .build();
            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
        }

        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && ifResponseTypeIsPartOrFullAdmission(caseData)) {
            LocalDate whenBePaid = deadlinesCalculator.calculateWhenToBePaid(responseDate);
            updatedData.respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                          .whenWillThisAmountBePaid(whenBePaid).build());
        }

        CaseRole respondentTwoCaseRoleToCheck;

        respondentTwoCaseRoleToCheck = RESPONDENTSOLICITORTWO;

        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, respondentTwoCaseRoleToCheck)) {
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
            Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent2DQ(caseData, updatedData, dq, callbackParams);
            updatedData.respondent2DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        } else {
            updatedData
                .respondent1ResponseDate(responseDate)
                .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack))
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

            if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
                Party updatedRespondent2;

                if (NO.equals(caseData.getSpecAoSRespondent2HomeAddressRequired())) {
                    updatedRespondent2 = caseData.getRespondent2().toBuilder()
                        .primaryAddress(caseData.getSpecAoSRespondent2HomeAddressDetails()).build();
                } else {
                    updatedRespondent2 = caseData.getRespondent2().toBuilder()
                        .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                        .flags(caseData.getRespondent2Copy().getFlags())
                        .build();
                }

                updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
                updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());
            }

            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth)
                .respondent1DQWitnesses(Witnesses.builder()
                                            .witnessesToAppear(caseData.getRespondent1DQWitnessesRequiredSpec())
                                            .details(caseData.getRespondent1DQWitnessesDetailsSpec())
                                            .build());
            handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
            updatedData.respondent1DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        }
        if (solicitorHasCaseRole(callbackParams, respondentTwoCaseRoleToCheck)
            && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
            updatedData.defenceAdmitPartPaymentTimeRouteRequired(null);
        }

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

        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
            updatedData.respondent1DQ(
                updatedData.build().getRespondent1DQ().toBuilder()
                    .respondent1DQExperts(Experts.builder()
                                              .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                              .details(wrapElements(expert))
                                              .build())
                    .build());
        }

        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getSmallClaimExperts() != null) {
            Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent2DQ().getSmallClaimExperts());
            updatedData.respondent2DQ(
                updatedData.build().getRespondent2DQ().toBuilder()
                    .respondent2DQExperts(Experts.builder()
                                              .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                              .details(wrapElements(expert))
                                              .build())
                    .build());
        }

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData,
                                                                        toggleService.isUpdateContactDetailsEnabled());

        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }

        if (toggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToRespondentExperts(updatedData);
            addEventAndDateAddedToRespondentWitnesses(updatedData);
        }

        if (toggleService.isHmcEnabled()) {
            populateDQPartyIds(updatedData);
        }

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE_SPEC, updatedData);

        if (toggleService.isCaseFileViewEnabled()) {
            // casefileview changes need to assign documents into specific folders, this is help determine
            // which user is "creating" the document and therefore which folder to move the documents
            // into, when directions order is generated in GenerateDirectionsQuestionnaireCallbackHandler
            UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
            updatedData.respondent2DocumentGeneration(null);
            if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                         .toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
                && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                           .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
                updatedData.respondent2DocumentGeneration("userRespondent2");
            }
        }

        if (toggleService.isHmcEnabled()) {
            populateWithPartyIds(updatedData);
        }

        updateCorrespondenceAddress(callbackParams, updatedData, caseData);

        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        } else if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && !isAwaitingAnotherDefendantResponse(caseData)) {
            if (!FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec())) {
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
        assembleResponseDocumentsSpec(caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state(CaseState.AWAITING_APPLICANT_INTENTION.name())
            .build();
    }

    private boolean ifResponseTypeIsPartOrFullAdmission(CaseData caseData) {
        return (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(
            caseData.getRespondent2ClaimResponseTypeForSpec())
            ) || (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(
            caseData.getRespondent2ClaimResponseTypeForSpec())
            );
    }

    private void updateCorrespondenceAddress(CallbackParams callbackParams,
                                             CaseData.CaseDataBuilder<?, ?> updatedCaseData,
                                             CaseData caseData) {
        if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getSpecAoSRespondentCorrespondenceAddressRequired() == YesOrNo.NO) {
            Address newAddress = caseData.getSpecAoSRespondentCorrespondenceAddressdetails();
            updatedCaseData.specRespondentCorrespondenceAddressdetails(newAddress)
                .specAoSRespondentCorrespondenceAddressdetails(Address.builder().build());
            if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP) {
                // to keep with heading tab
                updatedCaseData.specRespondent2CorrespondenceAddressdetails(newAddress);
            }
        } else if (solicitorHasCaseRole(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getSpecAoSRespondent2CorrespondenceAddressRequired() == YesOrNo.NO) {
            updatedCaseData.specRespondent2CorrespondenceAddressdetails(
                    caseData.getSpecAoSRespondent2CorrespondenceAddressdetails())
                .specAoSRespondent2CorrespondenceAddressdetails(Address.builder().build());
        }
    }

    private void assembleResponseDocumentsSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        ResponseDocument respondent1SpecDefenceResponseDocument = caseData.getRespondent1SpecDefenceResponseDocument();
        if (respondent1SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1SpecDefenceResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                defendantUploads.add(
                    buildElemCaseDocument(respondent1ClaimDocument, "Defendant",
                                          updatedCaseData.build().getRespondent1ResponseDate(),
                                          DocumentType.DEFENDANT_DEFENCE
                    ));
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1ClaimDocument,
                    "defendant1DefenseDirectionsQuestionnaire"
                );
            }
        }
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        if (respondent1DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1DQDraftDirections = respondent1DQ.getRespondent1DQDraftDirections();
            if (respondent1DQDraftDirections != null) {
                defendantUploads.add(
                    buildElemCaseDocument(
                        respondent1DQDraftDirections,
                        "Defendant",
                        updatedCaseData.build().getRespondent1ResponseDate(),
                        DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                    ));
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1DQDraftDirections,
                    "defendant1DefenseDirectionsQuestionnaire"
                );
            }
            ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
            if (respondent2SpecDefenceResponseDocument != null) {
                uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
                if (respondent2ClaimDocument != null) {
                    defendantUploads.add(
                        buildElemCaseDocument(respondent2ClaimDocument, "Defendant 2",
                                              updatedCaseData.build().getRespondent2ResponseDate(),
                                              DocumentType.DEFENDANT_DEFENCE
                        ));
                    assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        "defendant2DefenseDirectionsQuestionnaire"
                    );
                }
            }
        } else {
            ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
            if (respondent2SpecDefenceResponseDocument != null) {
                uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
                if (respondent2ClaimDocument != null) {
                    defendantUploads.add(
                        buildElemCaseDocument(respondent2ClaimDocument, "Defendant 2",
                                              updatedCaseData.build().getRespondent2ResponseDate(),
                                              DocumentType.DEFENDANT_DEFENCE
                        ));
                    assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        "defendant2DefenseDirectionsQuestionnaire"
                    );
                }
            }
        }
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        if (respondent2DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2DQDraftDirections = respondent2DQ.getRespondent2DQDraftDirections();
            if (respondent2DQDraftDirections != null) {
                defendantUploads.add(
                    buildElemCaseDocument(
                        respondent2DQDraftDirections,
                        "Defendant 2",
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                    ));
                assignCategoryId.assignCategoryIdToDocument(
                    respondent2DQDraftDirections,
                    "defendant2DefenseDirectionsQuestionnaire"
                );
            }
        }
        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
        // these documents are added to defendantUploads, if we do not remove/null the original,
        // case file view will show duplicate documents
        if (toggleService.isCaseFileViewEnabled()) {
            updatedCaseData.respondent1SpecDefenceResponseDocument(null);
            updatedCaseData.respondent2SpecDefenceResponseDocument(null);
        }

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

    private void handleCourtLocationForRespondent1DQ(CaseData caseData,
                                                     Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        Optional<LocationRefData> optCourtLocation = getCourtLocationDefendant1(caseData, callbackParams);
        // data for court location
        if (optCourtLocation.isPresent()) {
            LocationRefData courtLocation = optCourtLocation.get();

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
            dq.responseClaimCourtLocationRequired(NO);
        }
    }

    private Optional<LocationRefData> getCourtLocationDefendant1(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondToCourtLocation() != null) {
            DynamicList courtLocations = caseData
                .getRespondent1DQ().getRespondToCourtLocation().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), courtLocations);
            return Optional.ofNullable(courtLocation);
        } else {
            return Optional.empty();
        }
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData, CaseData.CaseDataBuilder updatedCase,
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
                                             .reasonForHearingAtSpecificCourt(
                                                 caseData.getRespondent2DQ().getRespondToCourtLocation2()
                                                     .getReasonForHearingAtSpecificCourt()
                                             )
                                             .build());
            updatedCase.responseClaimCourtLocation2Required(YES);
        } else {
            updatedCase.responseClaimCourtLocation2Required(NO);
        }
    }

    private Optional<LocationRefData> getCourtLocationDefendant2(CaseData caseData, CallbackParams callbackParams) {
        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondToCourtLocation2() != null) {
            DynamicList courtLocations = caseData
                .getRespondent2DQ().getRespondToCourtLocation2().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), courtLocations);
            return Optional.ofNullable(courtLocation);
        } else {
            return Optional.empty();
        }
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
        return deadlinesCalculator.calculateApplicantResponseDeadlineSpec(responseDate, allocatedTrack);
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
        LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
        if (responseDeadline == null) {
            return format(
                "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                    + "After the other solicitor has responded and/or the time"
                    + " for responding has passed the claimant will be notified."
                    + "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        } else {
            return format(
                "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                    + "%n%nThe claimant has until 4pm on %s to respond to your claim. "
                    + "We will let you know when they respond."
                    + "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                formatLocalDateTime(responseDeadline, DATE),
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        }
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

    /**
     * WhenWillClaimBePaid has to be shown if the respondent admits (full or part) and say they didn't pay.
     * At the moment of writing, full admit doesn't ask for how much the respondent paid (if they say they paid)
     * and part admit doesn't ask when will the amount be paid even if paid less.
     *
     * @param caseData claim data
     * @return true if pageId WhenWillClaimBePaid must be shown
     */
    public boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        // 1v1 or 1v2 dif sol
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            // admit part not pay or admit full not pay
            return caseData.getSpecDefenceFullAdmittedRequired() == NO
                || caseData.getSpecDefenceAdmittedRequired() == NO;
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            // admit part not pay or admit full not pay
            return caseData.getSpecDefenceFullAdmitted2Required() == NO
                || caseData.getSpecDefenceAdmitted2Required() == NO;
        }

        return false;
    }

}
