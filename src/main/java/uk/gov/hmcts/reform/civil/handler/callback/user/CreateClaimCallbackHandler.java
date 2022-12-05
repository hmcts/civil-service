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
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.OrgPolicyValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllOrganisationPolicyReferences;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateClaimCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_CLAIM);
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
    private final ObjectMapper objectMapper;
    private final Time time;
    private final ValidateEmailService validateEmailService;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService toggleService;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    @Value("${court-location.unspecified-claim.region-id}")
    private String regionId;
    @Value("${court-location.unspecified-claim.epimms-id}")
    private String epimmsId;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::setSuperClaimType)
            .put(callbackKey(V_1, ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "start-claim"), this::startClaim)
            .put(callbackKey(V_1, MID, "start-claim"), this::startClaim)
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
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::submitClaim)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse startClaim(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder();
        caseDataBuilder.claimStarted(YES);

        if (V_1.equals(callbackParams.getVersion()) && toggleService.isCourtLocationDynamicListEnabled()) {
            List<LocationRefData> locations = fetchLocationData(callbackParams);

            caseDataBuilder
                .courtLocation(CourtLocation.builder()
                   .applicantPreferredCourtLocationList(courtLocationUtils.getLocationsFromList(locations))
                   .build());
        }

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
        List<String> errors = orgPolicyValidator.validate(respondent1OrganisationPolicy, respondent1OrgRegistered);

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

    private void addOrgPolicy2ForSameLegalRepresentative(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (caseData.getRespondent2SameLegalRepresentative() == YES) {
            OrganisationPolicy.OrganisationPolicyBuilder organisationPolicy2Builder = OrganisationPolicy.builder();

            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
            organisationPolicy2Builder.organisation(respondent1OrganisationPolicy.getOrganisation())
                .orgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference())
                .build();

            organisationPolicy2Builder.orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName());
            caseDataBuilder.respondent2OrganisationPolicy(organisationPolicy2Builder.build());
        }
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> validationErrors;

        if (V_1.equals(callbackParams.getVersion()) && toggleService.isCourtLocationDynamicListEnabled()) {
            validationErrors = validateCourtChoice(caseData);
        } else {
            validationErrors = validateCourtTextOld(caseData);
        }

        if (validationErrors.size() > 0) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(validationErrors).build();
        }

        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);
        addOrgPolicy2ForSameLegalRepresentative(caseData, dataBuilder);

        if (caseData.getRespondent1OrgRegistered() == YES
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2SameLegalRepresentative() == YES) {
            // Predicate: Def1 registered, Def 2 unregistered.
            // This is required to ensure mutual exclusion in 1v2 same solicitor case.
            dataBuilder.respondent2OrgRegistered(YES);
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

        dataBuilder.claimStarted(null);

        if (V_1.equals(callbackParams.getVersion()) && toggleService.isCourtLocationDynamicListEnabled()) {
            handleCourtLocationData(caseData, dataBuilder, callbackParams);
        }

        if (V_1.equals(callbackParams.getVersion())
            && toggleService.isAccessProfilesEnabled()) {
            dataBuilder.caseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        }

        if (toggleService.isNoticeOfChangeEnabled()) {
            // LiP are not represented or registered
            if (areAnyRespondentsLitigantInPerson(caseData) == true) {
                dataBuilder.addLegalRepDeadline(deadlinesCalculator.plus14DaysAt4pmDeadline(time.now()));
            }
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
        if (V_1.equals(callbackParams.getVersion()) && toggleService.isGlobalSearchEnabled()) {

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
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private String getAllPartyNames(CaseData caseData) {
        return format("%s%s V %s%s",
                      caseData.getApplicant1().getPartyName(),
                      YES.equals(caseData.getAddApplicant2())
                          ? ", " + caseData.getApplicant2().getPartyName() : "",
                      caseData.getRespondent1().getPartyName(),
                      YES.equals(caseData.getAddRespondent2())
                          && NO.equals(caseData.getRespondent2SameLegalRepresentative())
                            ? ", " + caseData.getRespondent2().getPartyName() : "");
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
        dataBuilder.allocatedTrack(getAllocatedTrack(caseData.getClaimValue().toPounds(), caseData.getClaimType()));
        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM));

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
        if (areRespondentsRepresentedAndRegistered(caseData)) {
            return format("# Your claim has been received%n## Claim number: %s", caseData.getLegacyCaseReference());
        }

        return format(
            "# Your claim has been received and will progress offline%n## Claim number: %s",
            caseData.getLegacyCaseReference()
        );
    }

    private boolean areRespondentsRepresentedAndRegistered(CaseData caseData) {
        return !(caseData.getRespondent1Represented() == NO
            || caseData.getRespondent1OrgRegistered() == NO
            || caseData.getRespondent2Represented() == NO
            || caseData.getRespondent2OrgRegistered() == NO);
    }

    private boolean areAnyRespondentsLitigantInPerson(CaseData caseData) {
        return caseData.getRespondent1Represented() == NO
            || (YES.equals(caseData.getAddRespondent2()) ? (caseData.getRespondent2Represented() == NO) : false);
    }

    private String getBody(CaseData caseData) {
        return format(
            areRespondentsRepresentedAndRegistered(caseData)
                ? CONFIRMATION_SUMMARY
                : LIP_CONFIRMATION_BODY,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()),
            claimIssueConfiguration.getResponsePackLink()
        ) + exitSurveyContentService.applicantSurvey();
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

    // will remove when court location dynamic list flag is turned on for prod
    private List<String> validateCourtTextOld(CaseData caseData) {
        List<String> errorsMessages = new ArrayList<>();
        // Tactical fix. We have an issue where null courtLocation is being submitted.
        // We are validating it exists on submission if not we return an error to the user.
        if (caseData.getCourtLocation() == null || caseData.getCourtLocation().getApplicantPreferredCourt() == null) {
            errorsMessages.add("Court location code is required");
        }
        return errorsMessages;
    }

    /**
     * Sets the super claim type in case data to un-specified claims.
     * @param callbackParams callback containing case data
     * @return AboutToStartOrSubmitCallback response with super claim type set to un-specified claim
     */
    private CallbackResponse setSuperClaimType(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.superClaimType(SuperClaimType.UNSPEC_CLAIM);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
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
        if (Objects.nonNull(courtLocation)) {
            CourtLocation.CourtLocationBuilder courtLocationBuilder = caseData.getCourtLocation().toBuilder();
            dataBuilder
                .caseManagementLocation(CaseLocation.builder().region(regionId).baseLocation(epimmsId).build())
                .courtLocation(courtLocationBuilder
                                   .applicantPreferredCourt(courtLocation.getCourtLocationCode())
                                   .caseLocation(LocationRefDataService.buildCaseLocation(courtLocation))
                                   //to clear list of court locations from caseData
                                   .applicantPreferredCourtLocationList(null)
                                   .build());
        }
    }
}
