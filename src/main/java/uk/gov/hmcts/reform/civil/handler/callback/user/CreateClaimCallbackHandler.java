package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.ClaimTypeHelper;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.CasemanReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CaseNameUtils;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllOrganisationPolicyReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateWithPartyIds;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_CLAIM);

    public static final String CONFIRMATION_SUMMARY = "<br/>"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is "
        + "confirmed you will receive an email. The email will also include the date when you need to notify the Defendant "
        + "legal representative of the claim.%n%nYou must notify the Defendant legal representative of the claim within 4 "
        + "months of the claim being issued. The exact date when you must notify the claim details will be provided "
        + "when you first notify the Defendant legal representative of the claim. <br/>[Pay your claim fee](%s)";

    public static final String CONFIRMATION_BODY_LIP_COS = "<br />Your claim will not be issued until payment is "
        + "confirmed. [Pay your claim fee](%s)"
        + "%n%nYour claim will not be issued until payment is confirmed. Once payment is confirmed you will receive "
        + "an email. The email will also include the date when you need to notify the the Defendant of the Claim."
        + "%n%nYou must notify the Defendant of the claim within 4 months of the claim being issued."
        + "%n%nIf the defendant(s) include a litigant in person you must serve the claim outside of the digital portal."
        + "%n%nThe claim will remain in the digital portal to allow the litigant in person time to appoint a "
        + "legal representative who can respond the claim via the portal."
        + "%n%nIf service of the claim and claim details are processed outside of the digital portal you must complete "
        + "the next steps option 'notify claim' for the service of the claim form and 'notify claim details' for service "
        + "of the claim details.%n%n If notification of the claim is processed in the digital portal, the exact date "
        + "when you must notify the claim details will be provided when you first notify the Defendant legal "
        + "representative of the claim.";

    private final ClaimUrlsConfiguration claimUrlsConfiguration;
    private final ExitSurveyContentService exitSurveyContentService;
    private final CasemanReferenceNumberRepository casemanReferenceNumberRepository;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final FeesService feesService;
    private final OrganisationService organisationService;
    private final UserService userService;
    private final OrgPolicyValidator orgPolicyValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final ValidateEmailService validateEmailService;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService toggleService;
    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final AssignCategoryId assignCategoryId;
    private final CaseFlagsInitialiser caseFlagInitialiser;
    private final ToggleConfiguration toggleConfiguration;
    private final String caseDocLocation = "/cases/case-details/%s#CaseDocuments";
    private final PartyValidator partyValidator;

    @Value("${court-location.unspecified-claim.region-id}")
    private String regionId;
    @Value("${court-location.unspecified-claim.epimms-id}")
    private String epimmsId;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "start-claim"), this::startClaim)
            .put(callbackKey(MID, "applicant"), this::validateApplicant1Details)
            .put(callbackKey(MID, "applicant2"), this::validateApplicant2Details)
            .put(callbackKey(MID, "fee"), this::calculateFee)
            .put(callbackKey(MID, "idam-email"), this::getIdamEmail)
            .put(callbackKey(MID, "setRespondent2SameLegalRepresentativeToNo"), this::setRespondent2SameLegalRepToNo)
            .put(callbackKey(MID, "validate-defendant-legal-rep-email"), this::validateRespondentRepEmail)
            .put(callbackKey(MID, "validate-claimant-legal-rep-email"), this::validateClaimantRepEmail)
            .put(callbackKey(MID, "respondent1"), this::validateRespondent1Details)
            .put(callbackKey(MID, "respondent2"), this::validateRespondent2Details)
            .put(callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaim)
            .put(callbackKey(MID, "appOrgPolicy"), this::validateApplicantSolicitorOrgPolicy)
            .put(callbackKey(MID, "repOrgPolicy"), this::validateRespondentSolicitorOrgPolicy)
            .put(callbackKey(MID, "rep2OrgPolicy"), this::validateRespondentSolicitor2OrgPolicy)
            .put(callbackKey(MID, "statement-of-truth"), this::emptyCallbackResponse)
            .put(callbackKey(MID, "populateClaimantSolicitor"), this::populateClaimantSolicitor)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse startClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<LocationRefData> locations = fetchLocationData(authToken);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setApplicantPreferredCourtLocationList(courtLocationUtils.getLocationsFromList(locations));
        caseData.setClaimStarted(YES);
        caseData.setFeatureToggleWA(toggleConfiguration.getFeatureToggle());
        caseData.setCourtLocation(courtLocation);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper)).build();
    }

    private CallbackResponse populateClaimantSolicitor(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        Optional<Organisation> organisation = organisationService.findOrganisation(authToken);
        organisation.ifPresent(value -> caseData.setApplicant1OrganisationPolicy(
            new OrganisationPolicy().setOrganisation(
                new uk.gov.hmcts.reform.ccd.model.Organisation()
                    .setOrganisationID(value.getOrganisationIdentifier()))
                    .setOrgPolicyReference(null)
                    .setOrgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper)).build();
    }

    private List<LocationRefData> fetchLocationData(String authToken) {
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private CallbackResponse validateApplicant1Details(CallbackParams callbackParams) {
        Party applicant = callbackParams.getCaseData().getApplicant1();
        List<String> errors = dateOfBirthValidator.validate(applicant);
        validatePartyDetails(applicant, errors);;

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateApplicant2Details(CallbackParams callbackParams) {
        Party applicant2 = callbackParams.getCaseData().getApplicant2();
        List<String> errors = dateOfBirthValidator.validate(applicant2);
        validatePartyDetails(applicant2, errors);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateRespondent1Details(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        List<String> errors = new ArrayList<>();
        validatePartyDetails(respondent, errors);

        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    private CallbackResponse validateRespondent2Details(CallbackParams callbackParams) {
        Party respondent2 = callbackParams.getCaseData().getRespondent2();
        List<String> errors = new ArrayList<>();
        if (respondent2 != null) {
            validatePartyDetails(respondent2, errors);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private List<String> validatePartyDetails(Party party, List<String> errors) {
        if (toggleService.isJudgmentOnlineLive()) {
            if (party.getPrimaryAddress() != null) {
                partyValidator.validateAddress(party.getPrimaryAddress(), errors);
            }
            partyValidator.validateName(party.getPartyName(), errors);
        }
        return errors;
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
        List<String> errors;
        errors = orgPolicyValidator.validate(respondent1OrganisationPolicy, respondent1OrgRegistered);
        if (errors.isEmpty()) {
            errors = orgPolicyValidator.validateSolicitorOrganisations(caseData);
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
        if (errors.isEmpty()) {
            errors = orgPolicyValidator.validateSolicitorOrganisations(caseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse calculateFee(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
        String reference = references.map(SolicitorReferences::getApplicantSolicitor1Reference).orElse("");

        Optional<PaymentDetails> paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails());
        String customerReference = paymentDetails.map(PaymentDetails::getCustomerReference).orElse(reference);
        PaymentDetails updatedDetails = new PaymentDetails();
        updatedDetails.setCustomerReference(customerReference);
        caseData.setClaimIssuedPaymentDetails(updatedDetails);
        caseData.setClaimFee(feesService.getFeeDataByClaimValue(caseData.getClaimValue()));
        caseData.setPaymentTypePBA("PBAv3");
        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseData.setApplicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers));
        caseData.setApplicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YES : NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse getIdamEmail(CallbackParams callbackParams) {
        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        CaseData caseData = callbackParams.getCaseData();
        CorrectEmail correctEmail = new CorrectEmail();
        correctEmail.setEmail(userDetails.getEmail());
        caseData.setApplicantSolicitor1CheckEmail(correctEmail);
        caseData.setApplicantSolicitor1UserDetails(new IdamUserDetails());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse setRespondent2SameLegalRepToNo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // only default this to NO if respondent 1 isn't represented
        if (caseData.getRespondent1Represented().equals(NO)) {
            caseData.setRespondent2SameLegalRepresentative(NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

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

    private void clearOrganisationPolicyId(CaseData caseData) {
        if (YES.equals(caseData.getRespondent1Represented())) {
            if (StringUtils.isBlank(caseData.getRespondent1OrganisationIDCopy())) {
                String id = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
                    .orElse(null);
                if (id != null) {
                    caseData.setRespondent1OrganisationIDCopy(id);
                }
            }

            OrganisationPolicy respondent1Policy = copyOrganisationPolicy(caseData.getRespondent1OrganisationPolicy());
            respondent1Policy.setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation());
            caseData.setRespondent1OrganisationPolicy(respondent1Policy);
        }

        if (NO.equals(caseData.getRespondent2SameLegalRepresentative()) && YES.equals(caseData.getRespondent2Represented())) {
            if (StringUtils.isBlank(caseData.getRespondent2OrganisationIDCopy())) {
                String id = Optional.ofNullable(caseData.getRespondent2OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
                    .orElse(null);
                if (id != null) {
                    caseData.setRespondent2OrganisationIDCopy(id);
                }
            }

            OrganisationPolicy respondent2Policy = copyOrganisationPolicy(caseData.getRespondent2OrganisationPolicy());
            respondent2Policy.setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation());
            caseData.setRespondent2OrganisationPolicy(respondent2Policy);
        }
    }

    private void addOrgPolicy2ForSameLegalRepresentative(CaseData caseData) {
        if (YES.equals(caseData.getRespondent2SameLegalRepresentative())) {
            OrganisationPolicy respondent2OrganisationPolicy = new OrganisationPolicy();
            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();

            if (respondent1OrganisationPolicy != null) {
                respondent2OrganisationPolicy
                    .setOrgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference());
            }

            respondent2OrganisationPolicy
                .setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName())
                .setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation());

            caseData.setRespondent2OrganisationPolicy(respondent2OrganisationPolicy);

            // Use the respondent1OrganisationIDCopy which was already set by clearOrganisationPolicyId
            caseData.setRespondent2OrganisationIDCopy(
                caseData.getRespondent1OrganisationIDCopy()
            );
        }
    }

    private OrganisationPolicy copyOrganisationPolicy(OrganisationPolicy sourcePolicy) {
        OrganisationPolicy copy = new OrganisationPolicy();
        if (sourcePolicy == null) {
            return copy;
        }

        return copy
            .setOrganisation(sourcePolicy.getOrganisation())
            .setOrgPolicyReference(sourcePolicy.getOrgPolicyReference())
            .setOrgPolicyCaseAssignedRole(sourcePolicy.getOrgPolicyCaseAssignedRole())
            .setPreviousOrganisations(sourcePolicy.getPreviousOrganisations());
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> validationErrors = validateCourtChoice(caseData);

        if (validationErrors.size() > 0) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(validationErrors).build();
        }

        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        populateSharedData(callbackParams, caseData);
        clearOrganisationPolicyId(caseData);
        addOrgPolicy2ForSameLegalRepresentative(caseData);

        // temporarily remove respondent1OrgRegistered() for CIV-2659
        if (caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2SameLegalRepresentative() == YES) {
            // Predicate: Def1 registered, Def 2 unregistered.
            // This is required to ensure mutual exclusion in 1v2 same solicitor case.
            caseData.setRespondent2OrgRegistered(YES);
            caseData.setRespondentSolicitor2EmailAddress(caseData.getRespondentSolicitor1EmailAddress());

            Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
            references.ifPresent(ref -> {
                SolicitorReferences updatedSolicitorReferences = new SolicitorReferences();
                updatedSolicitorReferences.setApplicantSolicitor1Reference(ref.getApplicantSolicitor1Reference());
                updatedSolicitorReferences.setRespondentSolicitor1Reference(ref.getRespondentSolicitor1Reference());
                updatedSolicitorReferences.setRespondentSolicitor2Reference(ref.getRespondentSolicitor1Reference());
                caseData.setSolicitorReferences(updatedSolicitorReferences);
            });
            caseData.setRespondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired());
            caseData.setRespondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
        }

        // moving statement of truth value to correct field, this was not possible in mid event.
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        caseData.setUiStatementOfTruth(new StatementOfTruth());
        caseData.setApplicantSolicitor1ClaimStatementOfTruth(statementOfTruth);
        Party respondent1DetailsForTab = new Party();
        respondent1DetailsForTab.setPartyID(caseData.getRespondent1().getPartyID());
        respondent1DetailsForTab.setType(caseData.getRespondent1().getType());
        respondent1DetailsForTab.setIndividualTitle(caseData.getRespondent1().getIndividualTitle());
        respondent1DetailsForTab.setIndividualFirstName(caseData.getRespondent1().getIndividualFirstName());
        respondent1DetailsForTab.setIndividualLastName(caseData.getRespondent1().getIndividualLastName());
        respondent1DetailsForTab.setIndividualDateOfBirth(caseData.getRespondent1().getIndividualDateOfBirth());
        respondent1DetailsForTab.setCompanyName(caseData.getRespondent1().getCompanyName());
        respondent1DetailsForTab.setOrganisationName(caseData.getRespondent1().getOrganisationName());
        respondent1DetailsForTab.setSoleTraderTitle(caseData.getRespondent1().getSoleTraderTitle());
        respondent1DetailsForTab.setSoleTraderFirstName(caseData.getRespondent1().getSoleTraderFirstName());
        respondent1DetailsForTab.setSoleTraderLastName(caseData.getRespondent1().getSoleTraderLastName());
        respondent1DetailsForTab.setSoleTraderTradingAs(caseData.getRespondent1().getSoleTraderTradingAs());
        respondent1DetailsForTab.setSoleTraderDateOfBirth(caseData.getRespondent1().getSoleTraderDateOfBirth());
        respondent1DetailsForTab.setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress());
        respondent1DetailsForTab.setPartyName(caseData.getRespondent1().getPartyName());
        respondent1DetailsForTab.setBulkClaimPartyName(caseData.getRespondent1().getBulkClaimPartyName());
        respondent1DetailsForTab.setPartyTypeDisplayValue(caseData.getRespondent1().getPartyTypeDisplayValue());
        respondent1DetailsForTab.setPartyEmail(caseData.getRespondent1().getPartyEmail());
        respondent1DetailsForTab.setPartyPhone(caseData.getRespondent1().getPartyPhone());
        respondent1DetailsForTab.setLegalRepHeading(caseData.getRespondent1().getLegalRepHeading());
        respondent1DetailsForTab.setUnavailableDates(caseData.getRespondent1().getUnavailableDates());
        respondent1DetailsForTab.setFlags(null);
        caseData.setRespondent1DetailsForClaimDetailsTab(respondent1DetailsForTab);

        // data for case list and unassigned list
        caseData.setAllPartyNames(getAllPartyNames(caseData));
        caseData.setUnassignedCaseListDisplayOrganisationReferences(getAllOrganisationPolicyReferences(caseData));
        caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(caseData));

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            Party respondent2DetailsForTab = new Party();
            respondent2DetailsForTab.setPartyID(caseData.getRespondent2().getPartyID());
            respondent2DetailsForTab.setType(caseData.getRespondent2().getType());
            respondent2DetailsForTab.setIndividualTitle(caseData.getRespondent2().getIndividualTitle());
            respondent2DetailsForTab.setIndividualFirstName(caseData.getRespondent2().getIndividualFirstName());
            respondent2DetailsForTab.setIndividualLastName(caseData.getRespondent2().getIndividualLastName());
            respondent2DetailsForTab.setIndividualDateOfBirth(caseData.getRespondent2().getIndividualDateOfBirth());
            respondent2DetailsForTab.setCompanyName(caseData.getRespondent2().getCompanyName());
            respondent2DetailsForTab.setOrganisationName(caseData.getRespondent2().getOrganisationName());
            respondent2DetailsForTab.setSoleTraderTitle(caseData.getRespondent2().getSoleTraderTitle());
            respondent2DetailsForTab.setSoleTraderFirstName(caseData.getRespondent2().getSoleTraderFirstName());
            respondent2DetailsForTab.setSoleTraderLastName(caseData.getRespondent2().getSoleTraderLastName());
            respondent2DetailsForTab.setSoleTraderTradingAs(caseData.getRespondent2().getSoleTraderTradingAs());
            respondent2DetailsForTab.setSoleTraderDateOfBirth(caseData.getRespondent2().getSoleTraderDateOfBirth());
            respondent2DetailsForTab.setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress());
            respondent2DetailsForTab.setPartyName(caseData.getRespondent2().getPartyName());
            respondent2DetailsForTab.setBulkClaimPartyName(caseData.getRespondent2().getBulkClaimPartyName());
            respondent2DetailsForTab.setPartyTypeDisplayValue(caseData.getRespondent2().getPartyTypeDisplayValue());
            respondent2DetailsForTab.setPartyEmail(caseData.getRespondent2().getPartyEmail());
            respondent2DetailsForTab.setPartyPhone(caseData.getRespondent2().getPartyPhone());
            respondent2DetailsForTab.setLegalRepHeading(caseData.getRespondent2().getLegalRepHeading());
            respondent2DetailsForTab.setUnavailableDates(caseData.getRespondent2().getUnavailableDates());
            respondent2DetailsForTab.setFlags(null);
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2DetailsForTab);
        }

        caseData.setClaimStarted(null);
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        handleCourtLocationData(caseData, callbackParams);

        // LiP are not represented or registered
        OrgPolicyUtils.addMissingOrgPolicies(caseData);

        // temporarily default to yes for CIV-2659
        if (YES.equals(caseData.getRespondent1Represented()) && caseData.getRespondent1OrgRegistered() == null) {
            caseData.setRespondent1OrgRegistered(YES);
        }

        if (YES.equals(caseData.getRespondent2Represented()) && caseData.getRespondent2OrgRegistered() == null) {
            caseData.setRespondent2OrgRegistered(YES);
        }

        //assign casemanagementcategory to the case and assign casenamehmctsinternal
        //casename
        caseData.setCaseNameHmctsInternal(buildCaseName(caseData));

        //case management category
        CaseManagementCategoryElement civil = new CaseManagementCategoryElement();
        civil.setCode("Civil");
        civil.setLabel("Civil");
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        CaseManagementCategory cmCategory = new CaseManagementCategory();
        cmCategory.setList_items(itemList);
        cmCategory.setValue(civil);
        caseData.setCaseManagementCategory(cmCategory);
        log.info("Case management equals: " + caseData.getCaseManagementCategory());
        log.info("CaseName equals: " + caseData.getCaseNameHmctsInternal());

        if (caseData.getRespondent1Represented().equals(NO)) {
            caseData.setDefendant1LIPAtClaimIssued(YES);
        } else {
            caseData.setDefendant1LIPAtClaimIssued(NO);
        }

        if (YES.equals(caseData.getAddRespondent2())) {
            if (caseData.getRespondent2Represented() == NO) {
                caseData.setDefendant2LIPAtClaimIssued(YES);
            } else {
                caseData.setDefendant2LIPAtClaimIssued(NO);
            }
        }
        //assign category ids to documents uploaded as part of particulars of claim
        assignParticularOfClaimCategoryIds(caseData);

        caseData.setCaseNamePublic(CaseNameUtils.buildCaseName(caseData));

        // Initialize case flags on the original caseData object
        caseFlagInitialiser.initialiseCaseFlags(CREATE_CLAIM, caseData);

        // Populate party IDs
        populateWithPartyIds(caseData);

        caseData.setCcdState(CaseState.PENDING_CASE_ISSUED);

        caseData.setAnyRepresented(YES);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void populateSharedData(CallbackParams callbackParams, CaseData caseData) {
        //second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        CorrectEmail applicantSolicitor1CheckEmail = caseData.getApplicantSolicitor1CheckEmail();

        if (applicantSolicitor1CheckEmail.isCorrect()) {
            IdamUserDetails idamUserDetails = new IdamUserDetails();
            idamUserDetails.setId(userDetails.getId());
            idamUserDetails.setEmail(applicantSolicitor1CheckEmail.getEmail());
            caseData.setApplicantSolicitor1UserDetails(idamUserDetails);
        } else {
            IdamUserDetails applicantSolicitor1UserDetails = caseData.getApplicantSolicitor1UserDetails();
            IdamUserDetails  idamUserDetails = new IdamUserDetails();
            idamUserDetails.setId(userDetails.getId());
            idamUserDetails.setEmail(applicantSolicitor1UserDetails.getEmail());
            caseData.setApplicantSolicitor1UserDetails(idamUserDetails);
        }

        caseData.setBusinessProcess(BusinessProcess.ready(CREATE_SERVICE_REQUEST_CLAIM));
        caseData.setLegacyCaseReference(casemanReferenceNumberRepository.next("unspec"));

        ClaimType claimType = ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(caseData.getClaimTypeUnSpec());
        caseData.setClaimType(claimType);
        caseData.setAllocatedTrack(getAllocatedTrack(
            caseData.getClaimValue().toPounds(),
            claimType,
            caseData.getPersonalInjuryType(),
            toggleService,
            caseData
        ));

        caseData.setSubmittedDate(time.now());

        //set check email field to null for GDPR
        caseData.setApplicantSolicitor1CheckEmail(new CorrectEmail());
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader())
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader() {
        return format("# Please now pay your claim fee%n# using the link below");
    }

    private boolean areRespondentsRepresentedAndRegistered(CaseData caseData) {
        return !(caseData.getRespondent1Represented() == NO
            || caseData.getRespondent1OrgRegistered() == NO
            || caseData.getRespondent2Represented() == NO
            || caseData.getRespondent2OrgRegistered() == NO);
    }

    private boolean areAnyRespondentsLitigantInPerson(CaseData caseData) {
        return caseData.getRespondent1Represented() == NO
            ||  isSecondRespondentLitigantInPerson(caseData);
    }

    private boolean isSecondRespondentLitigantInPerson(CaseData caseData) {
        return (YES.equals(caseData.getAddRespondent2()) ? (caseData.getRespondent2Represented() == NO) : false);
    }

    private String getBody(CaseData caseData) {
        return areRespondentsRepresentedAndRegistered(caseData)
            ? getConfirmationSummary(caseData)
            : format(CONFIRMATION_BODY_LIP_COS,
                       format("/cases/case-details/%s#Service%%20Request", caseData.getCcdCaseReference()),
                       format(caseDocLocation, caseData.getCcdCaseReference()),
                       claimUrlsConfiguration.getResponsePackLink())
                + exitSurveyContentService.applicantSurvey();
    }

    private String getConfirmationSummary(CaseData caseData) {
        return format(CONFIRMATION_SUMMARY,
                      format(caseDocLocation, caseData.getCcdCaseReference()))
                      + exitSurveyContentService.applicantSurvey();
    }

    private List<String> validateCourtChoice(CaseData caseData) {
        List<String> errorsMessages = new ArrayList<>();
        // Tactical fix. We have an issue where null courtLocation is being submitted.
        // We are validating it exists on submission if not we return an error to the user.
        if (caseData.getCourtLocation() == null
            || caseData.getCourtLocation().getApplicantPreferredCourtLocationList() == null
            || caseData.getCourtLocation().getApplicantPreferredCourtLocationList().getValue() == null) {
            errorsMessages.add("Court location code is required");
        }
        return errorsMessages;
    }

    private void handleCourtLocationData(CaseData caseData, CallbackParams callbackParams) {
        // data for court location
        DynamicList courtLocations = caseData.getCourtLocation().getApplicantPreferredCourtLocationList();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
            fetchLocationData(authToken), courtLocations);
        if (nonNull(courtLocation)) {
            CaseLocationCivil caseLocationCivil = new CaseLocationCivil();
            caseLocationCivil.setBaseLocation(epimmsId);
            caseLocationCivil.setRegion(regionId);
            caseData.setCaseManagementLocation(caseLocationCivil);
            CourtLocation courtLocation1 = new CourtLocation();
            courtLocation1.setApplicantPreferredCourt(courtLocation.getCourtLocationCode());
            courtLocation1.setCaseLocation(LocationHelper.buildCaseLocation(courtLocation));
            courtLocation1.setReasonForHearingAtSpecificCourt(caseData.getCourtLocation().getReasonForHearingAtSpecificCourt());
            //to clear list of court locations from caseData
            courtLocation1.setApplicantPreferredCourtLocationList(null);
            caseData.setCourtLocation(courtLocation1);

            List<LocationRefData> locations = locationRefDataService
                .getCourtLocationsByEpimmsId(authToken, epimmsId);
            Optional.ofNullable(locations)
                .orElseGet(Collections::emptyList).stream().findFirst()
                .ifPresent(locationRefData -> caseData.setLocationName(locationRefData.getSiteName()));
        }
    }

    private void assignParticularOfClaimCategoryIds(CaseData caseData) {
        if (YES.equals(caseData.getUploadParticularsOfClaim())) {
            assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getParticularsOfClaimDocument(),
                                                     Element::getValue, "particularsOfClaim");
            assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getMedicalReport(),
                                                     document -> document.getValue().getDocument(), "particularsOfClaim");
            assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getScheduleOfLoss(),
                                                     document -> document.getValue().getDocument(), "particularsOfClaim");
            assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getCertificateOfSuitability(),
                                                     document -> document.getValue().getDocument(), "particularsOfClaim");
            assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getOther(),
                                                     document -> document.getValue().getDocument(), "particularsOfClaim");
        }
    }
}
