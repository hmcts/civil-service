package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateFeeTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateSpecFeeTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateTotalClaimAmountTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.GetAirlineListTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimInterestDateTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SpecValidateClaimTimelineDateTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.SubmitClaimTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.ValidateClaimantDetailsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.ValidateRespondentDetailsTask;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimSpecCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(
        CaseEvent.CREATE_CLAIM_SPEC
    );

    public static final String CONFIRMATION_SUMMARY_PBA_V3 = "<br/>"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is "
        + "confirmed you will receive an email. The email will also include the date when you need to notify the Defendant "
        + "legal representative of the claim.%n%nYou must notify the Defendant legal representative of the claim within 4 "
        + "months of the claim being issued. The exact date when you must notify the claim details will be provided "
        + "when you first notify the Defendant legal representative of the claim. <br/>[Pay your claim fee](%s)";

    public static final String LIP_CONFIRMATION_BODY = "<br />Your claim will not be issued until payment is confirmed."
        + " Once payment is confirmed you will receive an email. The claim will then progress offline."
        + "%n%nTo continue the claim you need to send the <a href=\"%s\" target=\"_blank\">sealed claim form</a>, "
        + "a <a href=\"%s\" target=\"_blank\">response pack</a> and any supporting documents to "
        + "the defendant within 4 months. "
        + "%n%nOnce you have served the claim, send the Certificate of Service and supporting documents to the County"
        + " Court Claims Centre.";

    public static final String SPEC_CONFIRMATION_SUMMARY_PBA_V3 = "<br/>"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is "
        + "confirmed you will receive an email. The email will also include the date that the defendants have to " +
        "respond. <br/>[Pay your claim fee](%s)";

    public static final String SPEC_LIP_CONFIRMATION_BODY_PBAV3 = "<br />Your claim will not be issued until payment " +
        "is confirmed. [Pay your claim fee](%s) <br/>When the payment is confirmed your claim " +
        "will be issued "
        + "and you'll be notified by email. The claim will then progress offline."
        + "%n%nOnce the claim has been issued, you will need to serve the claim upon the "
        + "defendant which must include a response pack"
        + "%n%nYou will need to send the following:<ul style=\"margin-bottom : 0px;\"> <li> <a href=\"%s\" target=\"_blank\">sealed claim form</a> "
        +
        "</li><li><a href=\"%s\" target=\"_blank\">response pack</a></li><ul style=\"list-style-type:circle\"><li><a href=\"%s\" target=\"_blank\">N9A</a></li>"
        + "<li><a href=\"%s\" target=\"_blank\">N9B</a></li></ul><li>and any supporting documents</li></ul>"
        + "to the defendant within 4 months."
        + "%n%nFollowing this, you will need to file a Certificate of Service and supporting documents "
        + "to : <a href=\"mailto:OCMCNton@justice.gov.uk\">OCMCNton@justice.gov.uk</a>. The Certificate of Service form can be found here:"
        + "%n%n<ul><li><a href=\"%s\" target=\"_blank\">N215</a></li></ul>";

    private static final String ERROR_MESSAGE_SCHEDULED_DATE_OF_FLIGHT_MUST_BE_TODAY_OR_IN_THE_PAST =
        "Scheduled date of flight must be today or in the past";
    protected static final String INTEREST_FROM_PAGE_ID = "interest-from";

    private final ClaimUrlsConfiguration claimUrlsConfiguration;
    private final ExitSurveyContentService exitSurveyContentService;
    private final UserService userService;
    private final OrgPolicyValidator orgPolicyValidator;
    private final ObjectMapper objectMapper;
    private final ValidateEmailService validateEmailService;
    private final PostcodeValidator postcodeValidator;
    private final InterestCalculator interestCalculator;
    private final FeatureToggleService toggleService;
    private static final String CASE_DOC_LOCATION = "/cases/case-details/%s#CaseDocuments";
    private final ValidateClaimantDetailsTask validateClaimantDetailsTask;
    private final SubmitClaimTask submitClaimTask;
    private final SpecValidateClaimInterestDateTask specValidateClaimInterestDateTask;
    private final SpecValidateClaimTimelineDateTask specValidateClaimTimelineDateTask;
    private final CalculateFeeTask calculateFeeTask;
    private final CalculateTotalClaimAmountTask calculateTotalClaimAmountTask;
    private final CalculateSpecFeeTask calculateSpecFeeTask;
    private final GetAirlineListTask getAirlineListTask;
    private final ValidateRespondentDetailsTask validateRespondentDetailsTask;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "eligibilityCheck"), this::eligibilityCheck)
            .put(callbackKey(MID, "applicant"), this::validateClaimant1Details)
            .put(callbackKey(MID, "applicant2"), this::validateClaimant2Details)
            .put(callbackKey(MID, "fee"), this::calculateFee)
            .put(callbackKey(MID, "idam-email"), this::getIdamEmail)
            .put(callbackKey(MID, "validate-defendant-legal-rep-email"), this::validateRespondentRepEmail)
            .put(callbackKey(MID, "validate-claimant-legal-rep-email"), this::validateClaimantRepEmail)
            .put(callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaim)
            .put(callbackKey(MID, "appOrgPolicy"), this::validateApplicantSolicitorOrgPolicy)
            .put(callbackKey(MID, "repOrgPolicy"), this::validateRespondentSolicitorOrgPolicy)
            .put(callbackKey(MID, "rep2OrgPolicy"), this::validateRespondentSolicitor2OrgPolicy)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .put(callbackKey(MID, "respondent1"), this::validateRespondent1Details)
            .put(callbackKey(MID, "respondent2"), this::validateRespondent2Details)
            .put(callbackKey(MID, "amount-breakup"), this::calculateTotalClaimAmount)
            .put(callbackKey(MID, "respondentSolicitor1"), this::validateRespondentSolicitorAddress)
            .put(callbackKey(MID, "respondentSolicitor2"), this::validateRespondentSolicitor2Address)
            .put(callbackKey(MID, "interest-calc"), this::calculateInterest)
            .put(callbackKey(MID, INTEREST_FROM_PAGE_ID), this::interestFromDefault)
            .put(callbackKey(MID, "ClaimInterest"), this::specCalculateInterest)
            .put(callbackKey(MID, "spec-fee"), this::calculateSpecFee)
            .put(callbackKey(MID, "ValidateClaimInterestDate"), this::specValidateClaimInterestDate)
            .put(callbackKey(MID, "ValidateClaimTimelineDate"), this::specValidateClaimTimelineDate)
            .put(callbackKey(MID, "specCorrespondenceAddress"), this::validateCorrespondenceApplicantAddress)
            .put(callbackKey(MID, "setRespondent2SameLegalRepresentativeToNo"), this::setRespondent2SameLegalRepToNo)
            .put(
                callbackKey(MID, "specRespondentCorrespondenceAddress"),
                params -> validateCorrespondenceRespondentAddress(
                    params,
                    CaseData::getSpecRespondentCorrespondenceAddressRequired,
                    CaseData::getSpecRespondentCorrespondenceAddressdetails
                )
            )
            .put(
                callbackKey(MID, "specRespondent2CorrespondenceAddress"),
                params -> validateCorrespondenceRespondentAddress(
                    params,
                    CaseData::getSpecRespondent2CorrespondenceAddressRequired,
                    CaseData::getSpecRespondent2CorrespondenceAddressdetails
                )
            )
            .put(callbackKey(MID, "validate-spec-defendant-legal-rep-email"), this::validateRespondentRepEmail)
            .put(callbackKey(MID, "validate-spec-defendant2-legal-rep-email"), this::validateSpecRespondent2RepEmail)
            .put(callbackKey(MID, "get-airline-list"), this::getAirlineList)
            .put(callbackKey(MID, "validateFlightDelayDate"), this::validateFlightDelayDate)
            .build();
    }

    private CallbackResponse interestFromDefault(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE.equals(caseData.getInterestClaimFrom())) {
            caseData = caseData.toBuilder()
                .interestClaimUntil(InterestClaimUntilType.UNTIL_SETTLED_OR_JUDGEMENT_MADE)
                .build();
            CallbackParams callbackParamsWithUpdatedCaseData = callbackParams.toBuilder()
                .caseData(caseData)
                .build();
            return calculateInterest(callbackParamsWithUpdatedCaseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse eligibilityCheck(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateClaimant1Details(CallbackParams callbackParams) {
        return validateClaimantDetails(callbackParams, CaseData::getApplicant1);
    }

    private CallbackResponse validateClaimant2Details(CallbackParams callbackParams) {
        return validateClaimantDetails(callbackParams, CaseData::getApplicant2);
    }

    private CallbackResponse validateClaimantDetails(CallbackParams callbackParams,
                                                     Function<CaseData, Party> getApplicant) {
        validateClaimantDetailsTask.setGetApplicant(getApplicant);
        return validateClaimantDetailsTask.validateClaimantDetails(callbackParams.getCaseData(), callbackParams.getRequest().getEventId());
    }

    private CallbackResponse validateApplicantSolicitorOrgPolicy(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        OrganisationPolicy applicant1OrganisationPolicy = caseData.getApplicant1OrganisationPolicy();
        List<String> errors = orgPolicyValidator.validate(applicant1OrganisationPolicy, YES);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateRespondentSolicitorOrgPolicy(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
        YesOrNo respondent1OrgRegistered = caseData.getRespondent1OrgRegistered();
        List<String> errors = orgPolicyValidator.validate(respondent1OrganisationPolicy, respondent1OrgRegistered);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse specValidateClaimInterestDate(CallbackParams callbackParams) {
        return specValidateClaimInterestDateTask.specValidateClaimInterestDate(callbackParams.getCaseData(),
            callbackParams.getRequest().getEventId());
    }

    private CallbackResponse specValidateClaimTimelineDate(CallbackParams callbackParams) {
        return specValidateClaimTimelineDateTask.specValidateClaimTimelineDateTask(callbackParams.getCaseData());
    }

    private CallbackResponse validateRespondentSolicitor2OrgPolicy(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        OrganisationPolicy respondent2OrganisationPolicy = caseData.getRespondent2OrganisationPolicy();
        YesOrNo respondent2OrgRegistered = caseData.getRespondent2OrgRegistered();
        List<String> errors = orgPolicyValidator.validate(respondent2OrganisationPolicy, respondent2OrgRegistered);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse calculateFee(CallbackParams callbackParams) {
        return calculateFeeTask.calculateFees(
            callbackParams.getCaseData(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
    }

    private CallbackResponse getIdamEmail(CallbackParams callbackParams) {
        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder()
            .applicantSolicitor1CheckEmail(CorrectEmail.builder().email(userDetails.getEmail()).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    //WARNING! below function getPbaAccounts is being used by both damages and specified claims,
    // changes to this code may break one of the claim journeys, check with respective teams before changing it
    private CallbackResponse validateClaimantRepEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!caseData.getApplicantSolicitor1CheckEmail().isCorrect()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(validateEmailService.validate(caseData.getApplicantSolicitor1UserDetails().getEmail()))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateRespondentRepEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(caseData.getRespondentSolicitor1EmailAddress()))
            .build();
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // resetting statement of truth field, this resets in the page, but the data is still sent to the db.
        // must be to do with the way XUI cache data entered through the lifecycle of an event.
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(null)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        return submitClaimTask.submitClaim(callbackParams.getCaseData(), callbackParams.getRequest().getEventId(),
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            callbackParams.getCaseData().getIsFlightDelayClaim(),
            callbackParams.getCaseData().getFlightDelayDetails());
    }

    //--------v1 callback overloaded, return to single param
    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (null != callbackParams.getRequest().getEventId()
            && callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getSpecHeader())
                .confirmationBody(getSpecBody(caseData))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getHeader(caseData))
                .confirmationBody(getBody(caseData))
                .build();
        }
    }

    static final String PAY_FEE_MESSAGE = "# Please now pay your claim fee%n# using the link below";

    private String getHeader(CaseData caseData) {
        if (areRespondentsRepresentedAndRegistered(caseData) || isPinInPostCaseMatched(caseData)) {
            return format(PAY_FEE_MESSAGE);
        }
        return format(
            "# Your claim has been received and will progress offline%n## Claim number: %s",
            caseData.getLegacyCaseReference()
        );
    }

    private String getBody(CaseData caseData) {
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        return
            ((areRespondentsRepresentedAndRegistered(caseData)
                || isPinInPostCaseMatched(caseData))
                ? getConfirmationSummary(caseData)
                : format(LIP_CONFIRMATION_BODY, format(
                    CASE_DOC_LOCATION,
                    caseData.getCcdCaseReference()
                ),
                claimUrlsConfiguration.getResponsePackLink(),
                formattedServiceDeadline
            ))
                + exitSurveyContentService.applicantSurvey();
    }

    static final String CASE_DETAILS_URL = "/cases/case-details/%s#Service%%20Request";

    private String getConfirmationSummary(CaseData caseData) {
        return format(
            CONFIRMATION_SUMMARY_PBA_V3,
            format(CASE_DETAILS_URL, caseData.getCcdCaseReference())
        );
    }

    private CallbackResponse validateRespondent1Details(CallbackParams callbackParams) {
        return validateRespondentDetails(callbackParams, CaseData::getRespondent1);
    }

    private CallbackResponse validateRespondent2Details(CallbackParams callbackParams) {
        return validateRespondentDetails(callbackParams, CaseData::getRespondent2);
    }

    private CallbackResponse validateRespondentDetails(CallbackParams callbackParams,
                                                       Function<CaseData, Party> getRespondent) {
        validateRespondentDetailsTask.setGetRespondent(getRespondent);
        return validateRespondentDetailsTask.validateRespondentDetails(callbackParams.getCaseData());
    }

    private CallbackResponse validateRespondentSolicitorAddress(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return validatePostCode(caseData.getRespondentSolicitor1OrganisationDetails().getAddress().getPostCode());
    }

    private CallbackResponse validateRespondentSolicitor2Address(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return validatePostCode(caseData.getRespondentSolicitor2OrganisationDetails().getAddress().getPostCode());
    }

    private CallbackResponse validatePostCode(String postCode) {
        List<String> errors = postcodeValidator.validate(postCode);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateCorrespondenceRespondentAddress(CallbackParams callbackParams,
                                                                     Function<CaseData, YesOrNo> required,
                                                                     Function<CaseData, Address> address) {
        CaseData caseData = callbackParams.getCaseData();
        if (YES.equals(required.apply(caseData))) {
            return validatePostCode(address.apply(caseData).getPostCode());
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
    }

    private CallbackResponse validateCorrespondenceApplicantAddress(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getSpecApplicantCorrespondenceAddressRequired().equals(YES)) {
            return validatePostCode(caseData.getSpecApplicantCorrespondenceAddressdetails().getPostCode());
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        }
    }

    //calculate total amount for specified claim by adding up the claim break up amounts
    private CallbackResponse calculateTotalClaimAmount(CallbackParams callbackParams) {
        return calculateTotalClaimAmountTask.calculateTotalClaimAmount(callbackParams.getCaseData());
    }

    //calculate interest for specified claim
    private CallbackResponse calculateInterest(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        BigDecimal totalAmountWithInterest = caseData.getTotalClaimAmount().add(interest);

        String calculatedInterest = " | Description | Amount | \n |---|---| \n | Claim amount | £ "
            + caseData.getTotalClaimAmount()
            + " | \n | Interest amount | £ " + interest + " | \n | Total amount | £ " + totalAmountWithInterest + " |";
        caseDataBuilder.calculatedInterest(calculatedInterest);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse specCalculateInterest(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        BigDecimal totalAmountWithInterest = caseData.getTotalClaimAmount();

        String calculateInterest = " | Description | Amount | \n |---|---| \n | Claim amount | £ "
            + caseData.getTotalClaimAmount()
            + " | \n | Interest amount | £ " + "0" + " | \n | Total amount | £ " + totalAmountWithInterest + " |";
        caseDataBuilder.calculatedInterest(calculateInterest);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    //calculate fee for specified claim
    private CallbackResponse calculateSpecFee(CallbackParams callbackParams) {
        return calculateSpecFeeTask.calculateSpecFee(callbackParams.getCaseData(), callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private String getSpecHeader() {
        return format(PAY_FEE_MESSAGE);
    }

    private String getSpecBody(CaseData caseData) {
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        return
            ((areRespondentsRepresentedAndRegistered(caseData)
                || isPinInPostCaseMatched(caseData))
                ? getSpecConfirmationSummary(caseData)
                : format(
                SPEC_LIP_CONFIRMATION_BODY_PBAV3,
                format(CASE_DETAILS_URL, caseData.getCcdCaseReference()),
                format(CASE_DOC_LOCATION, caseData.getCcdCaseReference()),
                claimUrlsConfiguration.getResponsePackLink(),
                claimUrlsConfiguration.getN9aLink(),
                claimUrlsConfiguration.getN9bLink(),
                claimUrlsConfiguration.getN215Link(),
                formattedServiceDeadline
            )) + exitSurveyContentService.applicantSurvey();
    }

    private String getSpecConfirmationSummary(CaseData caseData) {
        return format(
            SPEC_CONFIRMATION_SUMMARY_PBA_V3,
            format(CASE_DETAILS_URL, caseData.getCcdCaseReference())
        );
    }

    private CallbackResponse validateSpecRespondent2RepEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(caseData.getRespondentSolicitor2EmailAddress()))
            .build();
    }

    private CallbackResponse getAirlineList(CallbackParams callbackParams) {
        return getAirlineListTask.getAirlineList(callbackParams.getCaseData());
    }

    private CallbackResponse validateFlightDelayDate(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        List<String> errors = new ArrayList<>();
        if (callbackParams.getCaseData().getIsFlightDelayClaim().equals(YES)) {
            LocalDate today = LocalDate.now();
            LocalDate scheduledDate = callbackParams.getCaseData().getFlightDelayDetails().getScheduledDate();
            if (scheduledDate.isAfter(today)) {
                errors.add(ERROR_MESSAGE_SCHEDULED_DATE_OF_FLIGHT_MUST_BE_TODAY_OR_IN_THE_PAST);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse setRespondent2SameLegalRepToNo(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder();

        // only default this to NO if respondent 1 isn't represented
        if (callbackParams.getCaseData().getSpecRespondent1Represented().equals(NO)) {
            caseDataBuilder.respondent2SameLegalRepresentative(NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private boolean isPinInPostCaseMatched(CaseData caseData) {
        return (caseData.getRespondent1Represented() == NO
            && caseData.getAddRespondent2() == NO
            && caseData.getAddApplicant2() == NO
            && toggleService.isPinInPostEnabled());
    }

    private boolean areRespondentsRepresentedAndRegistered(CaseData caseData) {
        return !(caseData.getRespondent1Represented() == NO
            || caseData.getRespondent1OrgRegistered() == NO
            || caseData.getRespondent2Represented() == NO
            || caseData.getRespondent2OrgRegistered() == NO);
    }
}
