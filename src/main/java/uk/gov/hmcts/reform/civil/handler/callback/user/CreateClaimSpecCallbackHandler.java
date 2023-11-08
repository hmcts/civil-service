package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.FlightDelay;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateWithPartyIds;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimSpecCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(
        CaseEvent.CREATE_CLAIM_SPEC
    );
    public static final String CONFIRMATION_SUMMARY = "<br/>[Download the sealed claim form](%s)"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is confirmed you will "
        + "receive an email. The email will also include the date when you eed to notify the Defendant legal "
        + "representative of the claim.%n%nYou must notify the Defendant legal representative of the claim within 4 "
        + "months of the claim being issued. The exact date when you must notify the claim details will be provided "
        + "when you first notify the Defendant legal representative of the claim.";

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

    public static final String SPEC_CONFIRMATION_SUMMARY = "<br/>[Download the sealed claim form](%s)"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is confirmed you will "
        + "receive an email. The email will also include the date that the defendants have to respond.";

    public static final String SPEC_CONFIRMATION_SUMMARY_PBA_V3 = "<br/>"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is "
        + "confirmed you will receive an email. The email will also include the date that the defendants have to " +
        "respond. <br/>[Pay your claim fee](%s)";

    public static final String SPEC_LIP_CONFIRMATION_BODY = "<br />When the payment is confirmed your claim will be issued "
        + "and you'll be notified by email. The claim will then progress offline."
        + "%n%nOnce the claim has been issued, you will need to serve the claim upon the "
        + "defendant which must include a response pack"
        + "%n%nYou will need to send the following:<ul style=\"margin-bottom : 0px;\"> <li> <a href=\"%s\" target=\"_blank\">sealed claim form</a> "
        + "</li><li><a href=\"%s\" target=\"_blank\">response pack</a></li><ul style=\"list-style-type:circle\"><li><a href=\"%s\" target=\"_blank\">N9A</a></li>"
        + "<li><a href=\"%s\" target=\"_blank\">N9B</a></li></ul><li>and any supporting documents</li></ul>"
        + "to the defendant within 4 months."
        + "%n%nFollowing this, you will need to file a Certificate of Service and supporting documents "
        + "to : <a href=\"mailto:OCMCNton@justice.gov.uk\">OCMCNton@justice.gov.uk</a>. The Certificate of Service form can be found here:"
        + "%n%n<ul><li><a href=\"%s\" target=\"_blank\">N215</a></li></ul>";

    public static final String SPEC_LIP_CONFIRMATION_BODY_PBAV3 = "<br />Your claim will not be issued until payment " +
        "is confirmed. [Pay your claim fee](%s) <br/>When the payment is confirmed your claim " +
        "will be issued "
        + "and you'll be notified by email. The claim will then progress offline."
        + "%n%nOnce the claim has been issued, you will need to serve the claim upon the "
        + "defendant which must include a response pack"
        + "%n%nYou will need to send the following:<ul style=\"margin-bottom : 0px;\"> <li> <a href=\"%s\" target=\"_blank\">sealed claim form</a> "
        + "</li><li><a href=\"%s\" target=\"_blank\">response pack</a></li><ul style=\"list-style-type:circle\"><li><a href=\"%s\" target=\"_blank\">N9A</a></li>"
        + "<li><a href=\"%s\" target=\"_blank\">N9B</a></li></ul><li>and any supporting documents</li></ul>"
        + "to the defendant within 4 months."
        + "%n%nFollowing this, you will need to file a Certificate of Service and supporting documents "
        + "to : <a href=\"mailto:OCMCNton@justice.gov.uk\">OCMCNton@justice.gov.uk</a>. The Certificate of Service form can be found here:"
        + "%n%n<ul><li><a href=\"%s\" target=\"_blank\">N215</a></li></ul>";

    private static final String AIRLINE_NOT_FOUND_MESSAGE = "Airline code not found: %s";

    private final ClaimUrlsConfiguration claimUrlsConfiguration;
    private final ExitSurveyContentService exitSurveyContentService;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final SpecReferenceNumberRepository specReferenceNumberRepository;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final FeesService feesService;
    private final FeatureToggleService featureToggleService;
    private final OrganisationService organisationService;
    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final IdamClient idamClient;
    private final OrgPolicyValidator orgPolicyValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final ValidateEmailService validateEmailService;
    private final PostcodeValidator postcodeValidator;
    private final InterestCalculator interestCalculator;
    private final FeatureToggleService toggleService;
    private final StateFlowEngine stateFlowEngine;
    private final CaseFlagsInitialiser caseFlagInitialiser;
    private final ToggleConfiguration toggleConfiguration;
    private final LocationRefDataService locationRefDataService;
    private final String caseDocLocation = "/cases/case-details/%s#CaseDocuments";
    private static final String ERROR_MESSAGE_SCHEDULED_DATE_OF_FLIGHT_MUST_BE_TODAY_OR_IN_THE_PAST = "Scheduled date of flight must be today or in the past";

    @Value("${court-location.specified-claim.region-id}")
    private String regionId;
    @Value("${court-location.specified-claim.epimms-id}")
    private String epimmsId;

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
            .put(callbackKey(MID, "respondent1"), this::validateRespondent1Address)
            .put(callbackKey(MID, "respondent2"), this::validateRespondent2Address)
            .put(callbackKey(MID, "amount-breakup"), this::calculateTotalClaimAmount)
            .put(callbackKey(MID, "respondentSolicitor1"), this::validateRespondentSolicitorAddress)
            .put(callbackKey(MID, "respondentSolicitor2"), this::validateRespondentSolicitor2Address)
            .put(callbackKey(MID, "interest-calc"), this::calculateInterest)
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
            .put(callbackKey(MID, "validate-spec-defendant-legal-rep-email"), this::validateSpecRespondentRepEmail)
            .put(callbackKey(MID, "validate-spec-defendant2-legal-rep-email"), this::validateSpecRespondent2RepEmail)
            .put(callbackKey(MID, "is-flight-delay-claim"), this::isFlightDelayClaim)
            .put(callbackKey(MID, "get-airline-list"), this::getAirlineList)
            .put(callbackKey(MID, "validate-date-of-flight"), this::validateDateOfFlight)
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
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        Party applicant = getApplicant.apply(caseData);
        List<String> errors = dateOfBirthValidator.validate(applicant);
        if (errors.size() == 0 && callbackParams.getRequest().getEventId() != null) {
            errors = postcodeValidator.validate(
                applicant.getPrimaryAddress().getPostCode());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.size() == 0
                      ? caseDataBuilder.build().toMap(objectMapper) : null)
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
            List<String> errors = new ArrayList<>();
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
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
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
        if (toggleService.isPbaV3Enabled()) {
            caseDataBuilder.paymentTypePBASpec("PBAv3");
        }
        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());

        caseDataBuilder.claimFee(feesService
                                     .getFeeDataByClaimValue(caseData.getClaimValue()))
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

    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        StateFlow evaluation = stateFlowEngine.evaluate(caseData);
        State state = evaluation.getState();
        Map<String, Boolean> flags = evaluation.getFlags();

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
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);

        // moving statement of truth value to correct field, this was not possible in mid event.
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        dataBuilder.uiStatementOfTruth(StatementOfTruth.builder().build());
        dataBuilder.applicantSolicitor1ClaimStatementOfTruth(statementOfTruth);
        if (callbackParams.getRequest().getEventId() != null) {
            var respondent1Represented = caseData.getSpecRespondent1Represented();
            dataBuilder.respondent1Represented(respondent1Represented);
            var respondent2Represented = caseData.getSpecRespondent2Represented();
            dataBuilder.respondent2Represented(respondent2Represented);
        }

        addOrgPolicy2ForSameLegalRepresentative(dataBuilder.build(), dataBuilder);

        if (isPinInPostCaseMatched(caseData)) {
            dataBuilder.respondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost());
        }

        dataBuilder
            .caseManagementLocation(CaseLocationCivil.builder().region(regionId).baseLocation(epimmsId).build())
            .respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build())
            .caseAccessCategory(CaseCategory.SPEC_CLAIM);

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            dataBuilder.respondent2DetailsForClaimDetailsTab(caseData.getRespondent2().toBuilder().flags(null).build());
        }

        dataBuilder.caseAccessCategory(CaseCategory.SPEC_CLAIM);
        dataBuilder.featureToggleWA(toggleConfiguration.getFeatureToggle());

        //assign case management category to the case and caseNameHMCTSinternal
        dataBuilder.caseNameHmctsInternal(caseParticipants(caseData).toString());

        CaseManagementCategoryElement civil =
            CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        dataBuilder.caseManagementCategory(
            CaseManagementCategory.builder().value(civil).list_items(itemList).build());
        log.info("Case management equals: " + caseData.getCaseManagementCategory());
        log.info("CaseName equals: " + caseData.getCaseNameHmctsInternal());

        if (featureToggleService.isNoticeOfChangeEnabled()) {
            OrgPolicyUtils.addMissingOrgPolicies(dataBuilder);
        }

        caseFlagInitialiser.initialiseCaseFlags(CREATE_CLAIM_SPEC, dataBuilder);

        CaseData temporaryCaseData = dataBuilder.build();

        if (temporaryCaseData.getRespondent1OrgRegistered() == YES
            && temporaryCaseData.getRespondent1Represented() == YES
            && temporaryCaseData.getRespondent2SameLegalRepresentative() == YES) {
            // Predicate: Def1 registered, Def 2 unregistered.
            // This is required to ensure mutual exclusion in 1v2 same solicitor case.
            dataBuilder
                .respondent2OrgRegistered(YES)
                .respondentSolicitor2EmailAddress(caseData.getRespondentSolicitor1EmailAddress());
            Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
            references.ifPresent(ref -> {
                SolicitorReferences updatedSolicitorReferences = SolicitorReferences.builder()
                    .applicantSolicitor1Reference(ref.getApplicantSolicitor1Reference())
                    .respondentSolicitor1Reference(ref.getRespondentSolicitor1Reference())
                    .respondentSolicitor2Reference(ref.getRespondentSolicitor1Reference())
                    .build();
                dataBuilder.solicitorReferences(updatedSolicitorReferences);
            });
            dataBuilder
                .respondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired());
            dataBuilder.respondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
        } else if (temporaryCaseData.getRespondent1OrgRegistered() == NO
            && temporaryCaseData.getRespondent1Represented() == YES
            && temporaryCaseData.getRespondent2SameLegalRepresentative() == YES) {
            dataBuilder
                .respondent2OrgRegistered(NO)
                .respondentSolicitor2EmailAddress(caseData.getRespondentSolicitor1EmailAddress());
            Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
            references.ifPresent(ref -> {
                SolicitorReferences updatedSolicitorReferences = SolicitorReferences.builder()
                    .applicantSolicitor1Reference(ref.getApplicantSolicitor1Reference())
                    .respondentSolicitor1Reference(ref.getRespondentSolicitor1Reference())
                    .respondentSolicitor2Reference(ref.getRespondentSolicitor1Reference())
                    .build();
                dataBuilder.solicitorReferences(updatedSolicitorReferences);
            });
            dataBuilder
                .respondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired());
            dataBuilder.respondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
            dataBuilder.respondentSolicitor2OrganisationDetails(caseData.getRespondentSolicitor1OrganisationDetails());
        }

        if (toggleService.isHmcEnabled()) {
            populateWithPartyIds(dataBuilder);
        }

        if (caseData.getSdtRequestIdFromSdt() != null) {
            // assign StdRequestId, to ensure duplicate requests from SDT/bulk claims are not processed
            List<Element<String>> stdRequestIdList = new ArrayList<>();
            stdRequestIdList.add(element(caseData.getSdtRequestIdFromSdt()));
            dataBuilder.sdtRequestId(stdRequestIdList);
            //TODO implement bulk claims that have interest added.
            BigDecimal interest = interestCalculator.calculateInterest(caseData);
            dataBuilder.claimFee(feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount().add(interest)));
            //PBA manual selection
            List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());
            dataBuilder.applicantSolicitor1PbaAccounts(DynamicList.builder()
                                                           .value(DynamicListElement.builder()
                                                                      .label(pbaNumbers.get(0))
                                                                      .build()).build());
        }

        List<String> errors = new ArrayList<>();
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP
            && caseData.getSpecRespondentCorrespondenceAddressdetails() != null) {
            // to keep with heading tab
            dataBuilder
                .specRespondent2CorrespondenceAddressRequired(
                    caseData.getSpecRespondentCorrespondenceAddressRequired())
                .specRespondent2CorrespondenceAddressdetails(
                    caseData.getSpecRespondentCorrespondenceAddressdetails());
        }

        dataBuilder.flightDelay(FlightDelay.builder().flightCourtLocation(
            getAirlineCourtLocation(callbackParams.getCaseData()
                                        .getFlightDelay()
                                        .getFlightDetailsAirlineList().getValue()
                                        .getCode(), callbackParams)).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void addOrgPolicy2ForSameLegalRepresentative(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (caseData.getRespondent2SameLegalRepresentative() == YES) {
            OrganisationPolicy.OrganisationPolicyBuilder organisationPolicy2Builder = OrganisationPolicy.builder();

            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
            if (respondent1OrganisationPolicy != null) {
                organisationPolicy2Builder.organisation(respondent1OrganisationPolicy.getOrganisation())
                    .orgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference())
                    .build();
            }
            organisationPolicy2Builder.orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName());
            caseDataBuilder.respondent2OrganisationPolicy(organisationPolicy2Builder.build());
        }
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        IdamUserDetails.IdamUserDetailsBuilder idam = IdamUserDetails.builder().id(userDetails.getId());
        CorrectEmail applicantSolicitor1CheckEmail = caseData.getApplicantSolicitor1CheckEmail();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        if (applicantSolicitor1CheckEmail != null && applicantSolicitor1CheckEmail.isCorrect()) {
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1CheckEmail.getEmail()).build());
        } else {
            IdamUserDetails applicantSolicitor1UserDetails = caseData.getApplicantSolicitor1UserDetails();
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1UserDetails.getEmail()).build());
        }

        dataBuilder.submittedDate(time.now());

        if (null != callbackParams.getRequest().getEventId()) {
            dataBuilder.legacyCaseReference(specReferenceNumberRepository.getSpecReferenceNumber());
            if (!featureToggleService.isPbaV3Enabled()) {
                dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM_SPEC));
            } else {
                dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SERVICE_REQUEST_CLAIM));
            }
        }

        //set check email field to null for GDPR
        dataBuilder.applicantSolicitor1CheckEmail(CorrectEmail.builder().build());
        return dataBuilder;
    }

    //--------v1 callback overloaded, return to single param
    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (null != callbackParams.getRequest().getEventId()
            && callbackParams.getRequest().getEventId().equals("CREATE_CLAIM_SPEC")) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getSpecHeader(caseData))
                .confirmationBody(getSpecBody(caseData))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getHeader(caseData))
                .confirmationBody(getBody(caseData))
                .build();
        }
    }

    private String getHeader(CaseData caseData) {
        if (areRespondentsRepresentedAndRegistered(caseData)
            || isPinInPostCaseMatched(caseData)) {
            if (featureToggleService.isPbaV3Enabled()) {
                return format("# Please now pay your claim fee%n# using the link below");
            }
            return format("# Your claim has been received%n## Claim number: %s", caseData.getLegacyCaseReference());
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
                             caseDocLocation,
                             caseData.getCcdCaseReference()
                         ),
                         claimUrlsConfiguration.getResponsePackLink(),
                         formattedServiceDeadline
            ))
                + exitSurveyContentService.applicantSurvey();
    }

    private String getConfirmationSummary(CaseData caseData) {
        if (featureToggleService.isPbaV3Enabled()) {
            return format(
                CONFIRMATION_SUMMARY_PBA_V3,
                format("/cases/case-details/%s#Service%%20Request", caseData.getCcdCaseReference())
            );
        } else {
            return format(
                CONFIRMATION_SUMMARY,
                format(caseDocLocation, caseData.getCcdCaseReference())
            );
        }
    }

    private CallbackResponse validateRespondentAddress(CallbackParams params, Function<CaseData, Party> getRespondent) {
        CaseData caseData = params.getCaseData();
        return validatePostCode(getRespondent.apply(caseData).getPrimaryAddress().getPostCode());
    }

    private CallbackResponse validateRespondent1Address(CallbackParams callbackParams) {
        return validateRespondentAddress(callbackParams, CaseData::getRespondent1);
    }

    private CallbackResponse validateRespondent2Address(CallbackParams callbackParams) {
        return validateRespondentAddress(callbackParams, CaseData::getRespondent2);
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

        CaseData caseData = callbackParams.getCaseData();

        BigDecimal totalClaimAmount = new BigDecimal(0);

        List<ClaimAmountBreakup> claimAmountBreakups = caseData.getClaimAmountBreakup();

        String totalAmount = " | Description | Amount | \n |---|---| \n | ";
        StringBuilder stringBuilder = new StringBuilder();
        for (ClaimAmountBreakup claimAmountBreakup : claimAmountBreakups) {
            totalClaimAmount =
                totalClaimAmount.add(claimAmountBreakup.getValue().getClaimAmount());

            stringBuilder.append(claimAmountBreakup.getValue().getClaimReason())
                .append(" | ")
                .append("£ ")
                .append(MonetaryConversions.penniesToPounds(claimAmountBreakup.getValue().getClaimAmount()))
                .append(" |\n ");
        }
        totalAmount = totalAmount.concat(stringBuilder.toString());

        List<String> errors = new ArrayList<>();
        if (MonetaryConversions.penniesToPounds(totalClaimAmount).doubleValue() > 25000) {
            errors.add("Total Claim Amount cannot exceed £ 25,000");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.totalClaimAmount(
            MonetaryConversions.penniesToPounds(totalClaimAmount));

        totalAmount = totalAmount.concat(" | **Total** | £ " + MonetaryConversions
            .penniesToPounds(totalClaimAmount) + " | ");

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

        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        caseDataBuilder.claimFee(feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount().add(interest)));
        if (toggleService.isPbaV3Enabled()) {
            caseDataBuilder.paymentTypePBASpec("PBAv3");
        }
        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.applicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers))
            .applicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YES : NO)
            .totalInterest(interest);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private String getSpecHeader(CaseData caseData) {
        if (areRespondentsRepresentedAndRegistered(caseData)
            || isPinInPostCaseMatched(caseData)) {
            if (featureToggleService.isPbaV3Enabled()) {
                return format("# Please now pay your claim fee%n# using the link below");
            }
            return format("# Your claim has been received%n## Claim number: %s", caseData.getLegacyCaseReference());
        } else {
            if (featureToggleService.isPbaV3Enabled()) {
                return format("# Please now pay your claim fee%n# using the link below");
            } else {
                return format(
                    "# Your claim has been received and will progress offline%n## Claim number: %s",
                    caseData.getLegacyCaseReference()
                );
            }
        }
    }

    private String getSpecBody(CaseData caseData) {
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        return
            ((areRespondentsRepresentedAndRegistered(caseData)
                || isPinInPostCaseMatched(caseData))
                ? getSpecConfirmationSummary(caseData)
                : toggleService.isPbaV3Enabled() ? format(
                SPEC_LIP_CONFIRMATION_BODY_PBAV3,
                format("/cases/case-details/%s#Service%%20Request", caseData.getCcdCaseReference()),
                format(caseDocLocation, caseData.getCcdCaseReference()),
                claimUrlsConfiguration.getResponsePackLink(),
                claimUrlsConfiguration.getN9aLink(),
                claimUrlsConfiguration.getN9bLink(),
                claimUrlsConfiguration.getN215Link(),
                formattedServiceDeadline
            ) : format(
                SPEC_LIP_CONFIRMATION_BODY,
                format(caseDocLocation, caseData.getCcdCaseReference()),
                claimUrlsConfiguration.getResponsePackLink(),
                claimUrlsConfiguration.getN9aLink(),
                claimUrlsConfiguration.getN9bLink(),
                claimUrlsConfiguration.getN215Link(),
                formattedServiceDeadline
            )) + exitSurveyContentService.applicantSurvey();
    }

    private String getSpecConfirmationSummary(CaseData caseData) {
        if (featureToggleService.isPbaV3Enabled()) {
            return format(
                SPEC_CONFIRMATION_SUMMARY_PBA_V3,
                format("/cases/case-details/%s#Service%%20Request", caseData.getCcdCaseReference())
            );
        } else {
            return format(
                SPEC_CONFIRMATION_SUMMARY,
                format(caseDocLocation, caseData.getCcdCaseReference())
            );
        }
    }

    private CallbackResponse validateSpecRespondentRepEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(caseData.getRespondentSolicitor1EmailAddress()))
            .build();
    }

    private CallbackResponse validateSpecRespondent2RepEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(caseData.getRespondentSolicitor2EmailAddress()))
            .build();
    }

    private CallbackResponse isFlightDelayClaim(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();

        if (toggleService.isSdoR2Enabled()) {
            caseDataBuilder.isFlightDelayClaim(callbackParams.getCaseData().getIsFlightDelayClaim());
            if (callbackParams.getCaseData().getIsFlightDelayClaim().equals(YES)) {
                caseDataBuilder.claimType(ClaimType.FLIGHT_DELAY);
            } else {
                caseDataBuilder.claimType(null);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse getAirlineList(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        DynamicList airlineList = DynamicList.builder()
            .value(DynamicListElement.builder().build())
            .listItems(List.of(
                DynamicListElement.builder().code("BA/CITYFLYER").label("BA/Cityflyer").build(),
                DynamicListElement.builder().code("AIR_INDIA").label("Air India").build(),
                DynamicListElement.builder().code("GULF_AIR").label("Gulf Air").build(),
                DynamicListElement.builder().code("OTHER").label("OTHER").build())).build();
        FlightDelay flightDelay = FlightDelay.builder().flightDetailsAirlineList(airlineList).build();
        caseDataBuilder.flightDelay(flightDelay);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateDateOfFlight(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        List<String> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate scheduledDate = callbackParams.getCaseData().getFlightDelay().getFlightDetailsScheduledDate();
        if (scheduledDate.isAfter(today)) {
            errors.add(ERROR_MESSAGE_SCHEDULED_DATE_OF_FLIGHT_MUST_BE_TODAY_OR_IN_THE_PAST);
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

    public StringBuilder caseParticipants(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" v ").append(caseData.getRespondent1().getPartyName())
                .append(" and ").append(caseData.getRespondent2().getPartyName());

        } else if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            participantString.append(caseData.getApplicant1().getPartyName())
                .append(" and ").append(caseData.getApplicant2().getPartyName()).append(" v ")
                .append(caseData.getRespondent1()
                            .getPartyName());

        } else {
            participantString.append(caseData.getApplicant1().getPartyName()).append(" v ")
                .append(caseData.getRespondent1()
                            .getPartyName());
        }
        return participantString;

    }

    private LocationRefData getAirlineCourtLocation(String airline, CallbackParams callbackParams) {
        String locationEpimmsId = switch (airline) {
            case "BA/CITYFLYER" -> "111";
            case "AIR_INDIA" -> "222";
            case "GULF_AIR" -> "333";
            case "OTHER" -> "111";
            default -> throw new CallbackException(String.format(AIRLINE_NOT_FOUND_MESSAGE, airline));
        };

        List<LocationRefData> locations = fetchLocationData(callbackParams);
        var matchedLocations =  locations.stream().filter(loc -> loc.getEpimmsId().equals(locationEpimmsId)).toList();
        return !matchedLocations.isEmpty() ? matchedLocations.get(0) : null;
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
