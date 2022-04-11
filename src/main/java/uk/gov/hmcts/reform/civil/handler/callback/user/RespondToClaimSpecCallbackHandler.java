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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.HearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class RespondToClaimSpecCallbackHandler extends CallbackHandler implements ExpertsValidator, WitnessesValidator {

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
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(V_1, ABOUT_TO_START), this::populateRespondent1Copy)
            .put(callbackKey(MID, "confirm-details"), this::validateDateOfBirth)
            .put(callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates)
            .put(callbackKey(MID, "experts"), this::validateRespondentExperts)
            .put(callbackKey(MID, "witnesses"), this::validateRespondentWitnesses)
            .put(callbackKey(MID, "upload"), this::emptyCallbackResponse)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(MID, "validate-payment-date"), this::validateRespondentPaymentDate)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::setApplicantResponseDeadlineV1)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .put(callbackKey(MID, "specCorrespondenceAddress"), this::validateCorrespondenceApplicantAddress)
            .put(callbackKey(MID, "track"), this::handleDefendAllClaim)
            .put(callbackKey(MID, "specHandleResponseType"), this::handleRespondentResponseTypeForSpec)
            .put(callbackKey(MID, "specHandleAdmitPartClaim"), this::handleAdmitPartOfClaim)
            .put(callbackKey(MID, "validate-length-of-unemployment"), this::validateLengthOfUnemployment)
            .put(callbackKey(MID, "validate-repayment-plan"), this::validateRepaymentPlan)
            .put(callbackKey(MID, "set-generic-response-type-flag"), this::setGenericResponseTypeFlag)
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
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
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
        CaseData.CaseDataBuilder updatedData =
            caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec()))
            &&
            (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
                .build();
        }
        //this logic to be removed when ccd supports AND-OR combinations
        if (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE).build();
            }
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.COUNTER_CLAIM) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
                    .build();
            }
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
                updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_ADMISSION).build();
            }
        }

        if (ONE_V_TWO_ONE_LEGAL_REP.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            && caseData.getRespondentResponseIsSame().equals(NO)) {
            updatedData.sameSolicitorSameResponse(NO).build();
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE).build();
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specFullAdmissionOrPartAdmission(YES).build();
        }
        if ((RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            &&
            (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
               || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
               || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.COUNTER_ADMIT_OR_ADMIT_PART)
                .build();
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission1V1(YES).build();
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            updatedData.specFullDefenceOrPartAdmission(YES).build();
        }
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            updatedData.specDefenceFullAdmittedRequired(NO).build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
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
            caseData = caseData.toBuilder().respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
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
            CaseData caseData = callbackParams.getCaseData();
            if (caseData.getSpecAoSApplicantCorrespondenceAddressRequired().equals(NO)) {
                List<String> errors = postcodeValidator.validatePostCodeForDefendant(
                    caseData.getSpecAoSApplicantCorrespondenceAddressdetails().getPostCode());

                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .build();
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse populateRespondent1Copy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1());

        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedCaseData.respondent2Copy(caseData.getRespondent2());
            updatedCaseData.respondent2DetailsForClaimDetailsTab(caseData.getRespondent2());
        }

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
        List<String> errors = dateOfBirthValidator.validate(respondent);

        return AboutToStartOrSubmitCallbackResponse.builder()
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

        if (caseData.getSpecAoSApplicantCorrespondenceAddressRequired().equals(NO)) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails()).build();
        } else {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress()).build();
        }

        CaseData.CaseDataBuilder updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null)
            .respondent1ResponseDate(responseDate)
            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack))
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE_SPEC));

        updatedData.respondent1DetailsForClaimDetailsTab(updatedRespondent1);

        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {

            Party updatedRespondent2;

            if (caseData.getSpecAoSRespondent2HomeAddressRequired().equals(NO)) {
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

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse setApplicantResponseDeadlineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();

        CaseData.CaseDataBuilder updatedData = caseData.toBuilder()
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
            () -> format("# You've submitted your response%n## Claim number: %s", claimNumber),
            caseData
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }

    private String getDefaultConfirmationBody(CaseData caseData) {
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

    private CallbackResponse validateRepaymentPlan(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors;

        if (caseData.getRespondent1RepaymentPlan() != null
            && caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate() != null) {
            errors = unavailableDateValidator.validateFuturePaymentDate(caseData.getRespondent1RepaymentPlan()
                                                                            .getFirstRepaymentDate());
        } else {
            errors = new ArrayList<>();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }
}
