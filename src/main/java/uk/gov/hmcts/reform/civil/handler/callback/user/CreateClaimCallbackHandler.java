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
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CaseNameUtils;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllOrganisationPolicyReferences;
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

    public static final String CONFIRMATION_BODY_COS = "<br />Your claim will not be issued until payment is " +
        "confirmed. [Pay your claim fee](%s)"
        + "<br />Your claim will not be issued until payment is confirmed."
        + " Once payment is confirmed you will receive an email. The claim will then progress offline."
        + "%n%nTo continue the claim you need to send the <a href=\"%s\" target=\"_blank\">sealed claim form</a>, "
        + "a <a href=\"%s\" target=\"_blank\">response pack</a> and any supporting documents to "
        + "the defendant within 4 months. "
        + "%n%nOnce you have served the claim, send the Certificate of Service and supporting documents to the County"
        + " Court Claims Centre.";

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
    private final ReferenceNumberRepository referenceNumberRepository;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final FeesService feesService;
    private final OrganisationService organisationService;
    private final IdamClient idamClient;
    private final OrgPolicyValidator orgPolicyValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final ValidateEmailService validateEmailService;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService toggleService;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final AssignCategoryId assignCategoryId;
    private final CaseFlagsInitialiser caseFlagInitialiser;
    private final String caseDocLocation = "/cases/case-details/%s#CaseDocuments";

    @Value("${court-location.unspecified-claim.region-id}")
    private String regionId;
    @Value("${court-location.unspecified-claim.epimms-id}")
    private String epimmsId;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "start-claim"), this::startClaim)
            .put(callbackKey(MID, "applicant"), this::validateApplicant1DateOfBirth)
            .put(callbackKey(MID, "applicant2"), this::validateApplicant2DateOfBirth)
            .put(callbackKey(MID, "fee"), this::calculateFee)
            .put(callbackKey(MID, "idam-email"), this::getIdamEmail)
            .put(callbackKey(MID, "setRespondent2SameLegalRepresentativeToNo"), this::setRespondent2SameLegalRepToNo)
            .put(callbackKey(MID, "validate-defendant-legal-rep-email"), this::validateRespondentRepEmail)
            .put(callbackKey(MID, "validate-claimant-legal-rep-email"), this::validateClaimantRepEmail)
            .put(callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaim)
            .put(callbackKey(MID, "appOrgPolicy"), this::validateApplicantSolicitorOrgPolicy)
            .put(callbackKey(MID, "repOrgPolicy"), this::validateRespondentSolicitorOrgPolicy)
            .put(callbackKey(MID, "rep2OrgPolicy"), this::validateRespondentSolicitor2OrgPolicy)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
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
        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder();
        List<LocationRefData> locations = fetchLocationData(callbackParams);

        caseDataBuilder
            .claimStarted(YES)
            .courtLocation(CourtLocation.builder()
                               .applicantPreferredCourtLocationList(courtLocationUtils.getLocationsFromList(locations))
                               .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    private CallbackResponse populateClaimantSolicitor(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        Optional<Organisation> organisation = organisationService.findOrganisation(authToken);
        organisation.ifPresent(value -> caseDataBuilder.applicant1OrganisationPolicy(OrganisationPolicy.builder()
                 .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                 .organisationID(value.getOrganisationIdentifier()).build())
                 .orgPolicyReference(null)
                 .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName())
                 .build()));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private CallbackResponse validateApplicant1DateOfBirth(CallbackParams callbackParams) {
        Party applicant = callbackParams.getCaseData().getApplicant1();
        List<String> errors = dateOfBirthValidator.validate(applicant);

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
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        Optional<PaymentDetails> paymentDetails = ofNullable(caseData.getClaimIssuedPaymentDetails());
        String customerReference = paymentDetails.map(PaymentDetails::getCustomerReference).orElse(reference);
        PaymentDetails updatedDetails = PaymentDetails.builder().customerReference(customerReference).build();
        caseDataBuilder.claimIssuedPaymentDetails(updatedDetails);

        caseDataBuilder.claimFee(feesService.getFeeDataByClaimValue(caseData.getClaimValue()));
        if (toggleService.isPbaV3Enabled()) {
            caseDataBuilder.paymentTypePBA("PBAv3");
        }
        List<String> pbaNumbers = getPbaAccounts(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.applicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers))
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

    private CallbackResponse setRespondent2SameLegalRepToNo(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder();

        // only default this to NO if respondent 1 isn't represented
        if (callbackParams.getCaseData().getRespondent1Represented().equals(NO)) {
            caseDataBuilder.respondent2SameLegalRepresentative(NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
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

    private void clearOrganisationPolicyId(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (YES.equals(caseData.getRespondent1Represented())) {
            if (StringUtils.isBlank(caseData.getRespondent1OrganisationIDCopy())) {
                String id = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
                    .orElse(null);
                if (id != null) {
                    caseDataBuilder.respondent1OrganisationIDCopy(id);
                }
            }

            caseDataBuilder.respondent1OrganisationPolicy(
                caseData
                    .getRespondent1OrganisationPolicy()
                    .toBuilder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                    .build()
            );
        }

        if (NO.equals(caseData.getRespondent2SameLegalRepresentative()) && YES.equals(caseData.getRespondent2Represented())) {
            if (StringUtils.isBlank(caseData.getRespondent2OrganisationIDCopy())) {
                String id = Optional.ofNullable(caseData.getRespondent2OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID)
                    .orElse(null);
                if (id != null) {
                    caseDataBuilder.respondent2OrganisationIDCopy(id);
                }
            }

            caseDataBuilder.respondent2OrganisationPolicy(
                caseData
                    .getRespondent2OrganisationPolicy()
                    .toBuilder()
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                    .build()
            );
        }
    }

    private void addOrgPolicy2ForSameLegalRepresentative(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (YES.equals(caseData.getRespondent2SameLegalRepresentative())) {
            OrganisationPolicy.OrganisationPolicyBuilder organisationPolicy2Builder = OrganisationPolicy.builder();

            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
            organisationPolicy2Builder.organisation(respondent1OrganisationPolicy.getOrganisation())
                .orgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference())
                .build();

            organisationPolicy2Builder.orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName());

            caseDataBuilder.respondent2OrganisationPolicy(
                organisationPolicy2Builder
                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                    .build()
            );

            caseDataBuilder.respondent2OrganisationIDCopy(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            );
        }
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> validationErrors = validateCourtChoice(caseData);

        if (validationErrors.size() > 0) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(validationErrors).build();
        }

        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);
        clearOrganisationPolicyId(caseData, dataBuilder);
        addOrgPolicy2ForSameLegalRepresentative(caseData, dataBuilder);

        // temporarily remove respondent1OrgRegistered() for CIV-2659
        if (caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2SameLegalRepresentative() == YES) {
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
                .respondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired())
                .respondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
        }

        // moving statement of truth value to correct field, this was not possible in mid event.
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        dataBuilder
            .uiStatementOfTruth(StatementOfTruth.builder().build())
            .applicantSolicitor1ClaimStatementOfTruth(statementOfTruth)
            .respondent1DetailsForClaimDetailsTab(caseData.getRespondent1());

        // data for case list and unassigned list
        dataBuilder
            .allPartyNames(getAllPartyNames(caseData))
            .unassignedCaseListDisplayOrganisationReferences(getAllOrganisationPolicyReferences(caseData))
            .caseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(caseData));

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            dataBuilder.respondent2DetailsForClaimDetailsTab(caseData.getRespondent2());
        }

        dataBuilder
            .claimStarted(null)
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        handleCourtLocationData(caseData, dataBuilder, callbackParams);

        if (toggleService.isNoticeOfChangeEnabled()) {
            // LiP are not represented or registered
            OrgPolicyUtils.addMissingOrgPolicies(dataBuilder);
        }

        // temporarily default to yes for CIV-2659
        if (YES.equals(caseData.getRespondent1Represented()) && caseData.getRespondent1OrgRegistered() == null) {
            dataBuilder.respondent1OrgRegistered(YES);
        }

        if (YES.equals(caseData.getRespondent2Represented()) && caseData.getRespondent2OrgRegistered() == null) {
            dataBuilder.respondent2OrgRegistered(YES);
        }

        //assign casemanagementcategory to the case and assign casenamehmctsinternal
        //casename
        dataBuilder.caseNameHmctsInternal(caseParticipants(caseData).toString());

        //case management category
        CaseManagementCategoryElement civil =
            CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        dataBuilder.caseManagementCategory(
            CaseManagementCategory.builder().value(civil).list_items(itemList).build());
        log.info("Case management equals: " + caseData.getCaseManagementCategory());
        log.info("CaseName equals: " + caseData.getCaseNameHmctsInternal());

        //Adding variables for feature Certificate of Service
        if (toggleService.isCertificateOfServiceEnabled()) {
            if (caseData.getRespondent1Represented().equals(NO)) {
                dataBuilder.defendant1LIPAtClaimIssued(YES);
            } else {
                dataBuilder.defendant1LIPAtClaimIssued(NO);
            }

            if (YES.equals(caseData.getAddRespondent2())) {
                if (caseData.getRespondent2Represented() == NO) {
                    dataBuilder.defendant2LIPAtClaimIssued(YES);
                } else {
                    dataBuilder.defendant2LIPAtClaimIssued(NO);
                }
            }
        }
        //assign category ids to documents uploaded as part of particulars of claim
        assignParticularOfClaimCategoryIds(caseData);

        dataBuilder.caseNamePublic(CaseNameUtils.buildCaseNamePublic(caseData));

        caseFlagInitialiser.initialiseCaseFlags(CREATE_CLAIM, dataBuilder);

        dataBuilder.ccdState(CaseState.PENDING_CASE_ISSUED);

        if (toggleService.isHmcEnabled()) {
            populateWithPartyIds(dataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        //second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        IdamUserDetails.IdamUserDetailsBuilder idam = IdamUserDetails.builder().id(userDetails.getId());

        CaseData caseData = callbackParams.getCaseData();
        CorrectEmail applicantSolicitor1CheckEmail = caseData.getApplicantSolicitor1CheckEmail();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        if (applicantSolicitor1CheckEmail.isCorrect()) {
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1CheckEmail.getEmail()).build());
        } else {
            IdamUserDetails applicantSolicitor1UserDetails = caseData.getApplicantSolicitor1UserDetails();
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1UserDetails.getEmail()).build());
        }

        if (!toggleService.isPbaV3Enabled()) {
            dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM));
        } else {
            dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SERVICE_REQUEST_CLAIM));
        }

        dataBuilder.legacyCaseReference(referenceNumberRepository.getReferenceNumber());
        dataBuilder.allocatedTrack(getAllocatedTrack(caseData.getClaimValue().toPounds(), caseData.getClaimType()));
        dataBuilder.submittedDate(time.now());

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
            : toggleService.isCertificateOfServiceEnabled()
              ? format(CONFIRMATION_BODY_LIP_COS,
                       format("/cases/case-details/%s#Service%%20Request", caseData.getCcdCaseReference()),
                       format(caseDocLocation, caseData.getCcdCaseReference()),
                       claimUrlsConfiguration.getResponsePackLink())
                + exitSurveyContentService.applicantSurvey()
              : format(CONFIRMATION_BODY_COS,
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

    public StringBuilder caseParticipants(CaseData caseData) {
        StringBuilder participantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
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

    private void handleCourtLocationData(CaseData caseData, CaseData.CaseDataBuilder dataBuilder,
                                         CallbackParams callbackParams) {
        // data for court location
        DynamicList courtLocations = caseData.getCourtLocation().getApplicantPreferredCourtLocationList();
        LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
            fetchLocationData(callbackParams), courtLocations);
        if (nonNull(courtLocation)) {
            CourtLocation.CourtLocationBuilder courtLocationBuilder = caseData.getCourtLocation().toBuilder();
            dataBuilder
                .caseManagementLocation(CaseLocationCivil.builder().region(regionId).baseLocation(epimmsId).build())
                .courtLocation(courtLocationBuilder
                                   .applicantPreferredCourt(courtLocation.getCourtLocationCode())
                                   .caseLocation(LocationHelper.buildCaseLocation(courtLocation))
                                   //to clear list of court locations from caseData
                                   .applicantPreferredCourtLocationList(null)
                                   .build());
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
