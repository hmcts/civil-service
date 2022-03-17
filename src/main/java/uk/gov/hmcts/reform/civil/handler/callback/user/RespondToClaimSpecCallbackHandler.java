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
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
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

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
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
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            caseData = caseData.toBuilder().specDefenceFullAdmittedRequired(NO).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
            .respondent1Copy(caseData.getRespondent1())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
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
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();

        CaseData.CaseDataBuilder updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null)
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

        String body = Stream.of(getPartialAdmitSetDateSummary(caseData), getPartialAdmitImmediatelySummary(caseData))
            .filter(Optional::isPresent).map(Optional::get).findFirst().orElse(getDefaultConfirmationBody(caseData));

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(
                format("# You've submitted your response%n## Claim number: %s", claimNumber))
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

    /**
     * If applicable, creates the confirmation text for a case with partial admission, an amount still owed,
     * and an offer to pay by a set date.
     *
     * @param caseData a case data
     * @return the confirmation body if the case is a partial admission, the respondent hasn't paid yet,
     *     the respondent wants to pay by a set date and all fields needed for the text are available.
     */
    private Optional<String> getPartialAdmitSetDateSummary(CaseData caseData) {
        if (!RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || !RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE.equals(
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired())) {
            return Optional.empty();
        }
        BigDecimal admitOwed = caseData.getRespondToAdmittedClaimOwingAmountPounds();
        LocalDate whenWillYouPay = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
        BigDecimal totalClaimAmount = caseData.getTotalClaimAmount();

        if (Stream.of(admitOwed, whenWillYouPay, totalClaimAmount)
            .anyMatch(Objects::isNull)) {
            return Optional.empty();
        }
        String applicantName = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicantName += " and " + caseData.getApplicant2().getPartyName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You believe you owe &#163;").append(admitOwed).append(
                ". We've emailed ").append(applicantName)
            .append(" your offer to pay by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE));
        if (admitOwed.compareTo(totalClaimAmount) < 0) {
            sb.append(" and your explanation of why you do not owe the full amount.");
        }

        sb.append("<br>").append("The claimant has until 4pm on ")
            .append(formatLocalDateTime(caseData.getApplicant1ResponseDeadline(), DATE))
            .append(" to respond to your claim. <br>We will let you know when they respond.")
            .append(String
                        .format(
                            "%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
                            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
                        ));

        sb.append("<h2 class=\"govuk-heading-m\">What happens next</h2>")

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName);
        if (caseData.getApplicant2() != null) {
            sb.append(" accept your offer</h3>");
        } else {
            sb.append(" accepts your offer</h3>");
        }
        sb.append("<ul>")
            .append("<li>pay ").append(applicantName).append("</li>")
            .append("<li>make sure any cheques or bank transfers are clear in their account by the deadline</li>")
            .append("<li>keep proof of any payments you make</li>")
            .append("</ul>")
            .append("<p>Contact ")
            .append(applicantName);
        if (applicantName.endsWith("s")) {
            sb.append("'");
        } else {
            sb.append("'s");
        }
        sb.append(" legal representative if you need details on how to pay.</p>")

            .append("<p>Because you've said you will not pay immediately, ")
            .append(applicantName)
            .append(" can either:</p>")
            .append("<ul>")
            .append("<li>ask you to sign a settlement agreement to formalise the repayment plan</li>")
            .append("<li>request a county court judgment against you for &#163;")
            .append(admitOwed).append("</li>")
            .append("</ul>")

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName)
            .append(" disagrees that you only owe &#163;")
            .append(admitOwed)
            .append("</h3>")
            .append("<p>We'll ask if they want to try mediation. ")
            .append("If they agree we'll contact you to arrange a call with the mediator.</p>")
            .append(
                "<p>If they do not want to try mediation the court will review the case for the full amount of &#163;")
            .append(totalClaimAmount).append(".</p>")

            .append("<h3 class=\"govuk-heading-m\">If ")
            .append(applicantName)
            .append(" rejects your offer to pay by ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE))
            .append("</h3>")
            .append("<p>The court will decide how you must pay</p>");
        return Optional.of(sb.toString());
    }

    private Optional<String> getPartialAdmitImmediatelySummary(CaseData caseData) {
        if (!RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY.equals(
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired())) {
            return Optional.empty();
        }
        LocalDate whenWillYouPay = LocalDate.now().plusDays(5);
        String applicantName = caseData.getApplicant1().getPartyName();

        StringBuilder sb = new StringBuilder();
        sb.append("We've emailed ").append(applicantName)
            .append(" to say you will pay immediately.")
            .append("<h2 class=\"govuk-heading-m\">What you need to do:</h2>")
            .append("<ul>")
            .append("<li>pay ").append(applicantName).append(" By ")
            .append(DateFormatHelper.formatLocalDate(whenWillYouPay, DATE)).append("</li>")
            .append("<li>Keep proof of any payments you make</li>")
            .append("<li>make sure ").append(applicantName).append(" tells the court that you've paid").append("</li>")
            .append("</ul>")
            .append("<p>Contact ")
            .append(applicantName);
        if (applicantName.endsWith("s")) {
            sb.append("'");
        } else {
            sb.append("'s");
        }
        sb.append(" legal representative if you need details on how to pay.</p>");
        return Optional.of(sb.toString());
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
