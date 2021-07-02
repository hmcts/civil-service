package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.OnBoardingOrganisationControlService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateClaimCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Arrays.asList(
        CaseEvent.CREATE_CLAIM_SPEC,
        CaseEvent.CREATE_CLAIM
    );
    public static final String CONFIRMATION_SUMMARY = "<br/>[Download the sealed claim form](%s)"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is confirmed you will "
        + "receive an email. The email will also include the date when you need to notify the Defendant legal "
        + "representative of the claim.%n%nYou must notify the Defendant legal representative of the claim within 4 "
        + "months of the claim being issued. The exact date when you must notify the claim details will be provided "
        + "when you first notify the Defendant legal representative of the claim.";

    public static final String LIP_CONFIRMATION_BODY = "<br />Your claim will not be issued until payment is confirmed."
        + " Once payment is confirmed you will receive an email. The claim will then progress offline."
        + "%n%nTo continue the claim you need to send the <a href=\"%s\" target=\"_blank\">sealed claim form</a>, "
        + "a <a href=\"%s\" target=\"_blank\">response pack</a> and any supporting documents to "
        + "the defendant within 4 months. "
        + "%n%nOnce you have served the claim, send the Certificate of Service and supporting documents to the County"
        + " Court Claims Centre.";

    private final ClaimIssueConfiguration claimIssueConfiguration;
    private final ExitSurveyContentService exitSurveyContentService;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final FeesService feesService;
    private final OrganisationService organisationService;
    private final IdamClient idamClient;
    private final OrgPolicyValidator orgPolicyValidator;
    private final OnBoardingOrganisationControlService onboardingOrganisationControlService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final PostcodeValidator postcodeValidator;
    private final InterestCalculator interestCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "eligibilityCheck"), this::eligibilityCheck)
            .put(callbackKey(MID, "applicant"), this::validateClaimantDetails)
            .put(callbackKey(MID, "applicant2"), this::validateApplicant2DateOfBirth)
            .put(callbackKey(MID, "fee"), this::calculateFee)
            .put(callbackKey(MID, "idam-email"), this::getIdamEmail)
            .put(callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaim)
            .put(callbackKey(MID, "appOrgPolicy"), this::validateApplicantSolicitorOrgPolicy)
            .put(callbackKey(MID, "repOrgPolicy"), this::validateRespondentSolicitorOrgPolicy)
            .put(callbackKey(MID, "rep2OrgPolicy"), this::validateRespondentSolicitor2OrgPolicy)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitClaimBackwardsCompatible)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .put(callbackKey(MID, "respondent1"), this::validateRespondent1Address)
            .put(callbackKey(MID, "amount-breakup"), this::calculateTotalClaimAmount)
            .put(callbackKey(MID, "respondentSolicitor1"), this::validateRespondentSolicitorAddress)
            .put(callbackKey(MID, "interest-calc"), this::calculateInterest)
            .put(callbackKey(MID, "ClaimInterest"), this::specCalculateInterest)
            .put(callbackKey(MID, "spec-fee"), this::calculateSpecFee)
            .put(callbackKey(MID, "ValidateClaimInterestDate"), this::specValidateClaimInterestDate)
            .put(callbackKey(MID, "ValidateClaimTimelineDate"), this::specValidateClaimTimelineDate)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse eligibilityCheck(CallbackParams callbackParams) {
        String userBearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = onboardingOrganisationControlService.validateOrganisation(userBearerToken);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateClaimantDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Party applicant = caseData.getApplicant1();
        List<String> errors = dateOfBirthValidator.validate(applicant);
        if (errors.size() == 0 && callbackParams.getRequest().getEventId() != null
            && callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            errors = postcodeValidator.validatePostCodeForDefendant(
                caseData.getApplicant1().getPrimaryAddress().getPostCode());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateApplicant2DateOfBirth(CallbackParams callbackParams) {
        Party applicant = callbackParams.getCaseData().getApplicant2();
        List<String> errors = dateOfBirthValidator.validate(applicant);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
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
        if (callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            CaseData caseData = callbackParams.getCaseData();
            List<String> errors = new ArrayList<String>();
            if (caseData.getInterestFromSpecificDate() != null) {
                if (caseData.getInterestFromSpecificDate().isAfter(LocalDate.now())) {
                    errors.add("Correct the date. You can’t use a future date.");
                }
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse specValidateClaimTimelineDate(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            CaseData caseData = callbackParams.getCaseData();
            List<String> errors = new ArrayList<String>();
            if (caseData.getTimelineOfEvents() != null) {
                List<TimelineOfEvents> timelineOfEvent = caseData.getTimelineOfEvents();
                timelineOfEvent.forEach(timelineOfEvents -> {
                    if (timelineOfEvents.getValue().getTimelineDate().isAfter(LocalDate.now())) {
                        errors.add("Correct the date. You can’t use a future date.");
                    }
                });
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
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
        CaseData caseData = callbackParams.getCaseData();
        Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
        String reference = references.map(SolicitorReferences::getApplicantSolicitor1Reference).orElse("");
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        Optional<PaymentDetails> paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails());
        String customerReference = paymentDetails.map(PaymentDetails::getCustomerReference).orElse(reference);
        PaymentDetails updatedDetails = PaymentDetails.builder().customerReference(customerReference).build();
        caseDataBuilder.claimIssuedPaymentDetails(updatedDetails);

        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());

        caseDataBuilder.claimFee(feesService.getFeeDataByClaimValue(caseData.getClaimValue()))
            .applicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers))
            .applicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YES : NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse getIdamEmail(CallbackParams callbackParams) {
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder()
            .applicantSolicitor1CheckEmail(CorrectEmail.builder().email(userDetails.getEmail()).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    //WARNING! below function getPbaAccounts is being used by both damages and specified claims,
    // changes to this code may break one of the claim journeys, check with respective teams before changing it
    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
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

    private CallbackResponse submitClaimBackwardsCompatible(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        // moving statement of truth value to correct field, this was not possible in mid event.
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        dataBuilder.uiStatementOfTruth(StatementOfTruth.builder().build());
        dataBuilder.applicantSolicitor1ClaimStatementOfTruth(statementOfTruth);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        IdamUserDetails.IdamUserDetailsBuilder idam = IdamUserDetails.builder().id(userDetails.getId());
        CorrectEmail applicantSolicitor1CheckEmail = caseData.getApplicantSolicitor1CheckEmail();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        if (applicantSolicitor1CheckEmail.isCorrect()) {
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1CheckEmail.getEmail()).build());
        } else {
            IdamUserDetails applicantSolicitor1UserDetails = caseData.getApplicantSolicitor1UserDetails();
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1UserDetails.getEmail()).build());
        }

        dataBuilder.legacyCaseReference(referenceNumberRepository.getReferenceNumber());
        dataBuilder.submittedDate(time.now());

        if (null != callbackParams.getRequest().getEventId()
            && callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM_SPEC));
        } else {
            dataBuilder.allocatedTrack(getAllocatedTrack(caseData.getClaimValue().toPounds(), caseData.getClaimType()));
            dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM));
        }

        //set check email field to null for GDPR
        dataBuilder.applicantSolicitor1CheckEmail(CorrectEmail.builder().build());
        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        if (caseData.getRespondent1Represented() == NO || caseData.getRespondent1OrgRegistered() == NO) {
            return format(
                "# Your claim has been received and will progress offline%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            );
        }
        return format("# Your claim has been received%n## Claim number: %s", caseData.getLegacyCaseReference());
    }

    private String getBody(CaseData caseData) {
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        return format(
            caseData.getRespondent1Represented() == NO || caseData.getRespondent1OrgRegistered() == NO
                ? LIP_CONFIRMATION_BODY
                : CONFIRMATION_SUMMARY,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()),
            claimIssueConfiguration.getResponsePackLink(),
            formattedServiceDeadline
        ) + exitSurveyContentService.applicantSurvey();
    }

    private CallbackResponse validateRespondent1Address(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            CaseData caseData = callbackParams.getCaseData();
            List<String> errors = postcodeValidator.validatePostCodeForDefendant(
                caseData.getRespondent1().getPrimaryAddress().getPostCode());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse validateRespondentSolicitorAddress(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            CaseData caseData = callbackParams.getCaseData();
            List<String> errors = postcodeValidator.validatePostCodeForDefendant(
                caseData.getRespondentSolicitor1OrganisationDetails().getAddress().getPostCode());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    //calculate total amount for specified claim by adding up the claim break up amounts
    private CallbackResponse calculateTotalClaimAmount(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        var ref = new Object() {
            BigDecimal totalClaimAmount = new BigDecimal(0);
        };
        List<ClaimAmountBreakup> claimAmountBreakups = caseData.getClaimAmountBreakup();

        String totalAmount = " | Description | Amount | \n |---|---| \n | ";
        StringBuilder stringBuilder = new StringBuilder();
        claimAmountBreakups.stream().forEach(
            claimAmountBreakup -> {
                ref.totalClaimAmount =
                    ref.totalClaimAmount.add(claimAmountBreakup.getValue().getClaimAmount());

                stringBuilder.append(claimAmountBreakup.getValue().getClaimReason() + " | ");
                stringBuilder.append("£ "
                                         + MonetaryConversions.penniesToPounds(claimAmountBreakup.getValue()
                                                                                   .getClaimAmount()) + " |\n ");
            }
        );
        totalAmount = totalAmount.concat(stringBuilder.toString());

        List<String> errors = new ArrayList<>();
        if (MonetaryConversions.penniesToPounds(ref.totalClaimAmount).doubleValue() > 25000) {
            errors.add("Total Claim Amount cannot exceed £ 25,000");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.totalClaimAmount(
            MonetaryConversions.penniesToPounds(ref.totalClaimAmount));

        totalAmount = totalAmount.concat(" | **Total** | £ " + MonetaryConversions
            .penniesToPounds(ref.totalClaimAmount) + " | ");

        caseDataBuilder.claimAmountBreakupSummaryObject(totalAmount);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
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
        CaseData caseData = callbackParams.getCaseData();
        Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
        String reference = references.map(SolicitorReferences::getApplicantSolicitor1Reference).orElse("");
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        Optional<PaymentDetails> paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails());
        String customerReference = paymentDetails.map(PaymentDetails::getCustomerReference).orElse(reference);
        PaymentDetails updatedDetails = PaymentDetails.builder().customerReference(customerReference).build();
        caseDataBuilder.claimIssuedPaymentDetails(updatedDetails);

        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        caseDataBuilder.claimFee(feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount().add(interest)))
            .applicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers))
            .applicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YES : NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
