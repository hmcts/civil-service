package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.CaseManagementCategoryElement;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.service.AirlineEpimsService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateWithPartyIds;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubmitClaimTask {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final InterestCalculator interestCalculator;
    private final ToggleConfiguration toggleConfiguration;
    private final CaseFlagsInitialiser caseFlagInitialiser;
    private final FeesService feesService;
    private final UserService userService;
    private final Time time;
    private final SpecReferenceNumberRepository specReferenceNumberRepository;
    private final OrganisationService organisationService;
    private final AirlineEpimsService airlineEpimsService;
    private final LocationReferenceDataService locationRefDataService;
    private static final String LOCATION_NOT_FOUND_MESSAGE = "Location not found for ePIMS_ID: %s";
    @Value("${court-location.specified-claim.region-id}")
    private String regionId;
    @Value("${court-location.specified-claim.epimms-id}")
    private String epimmsId;

    public CallbackResponse submitClaim(CaseData caseData, String eventId, String authorisationToken, YesOrNo isFlightDelayClaim,
                                        FlightDelayDetails flightDelayDetails) {
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(caseData, authorisationToken, eventId);

        // moving statement of truth value to correct field, this was not possible in mid event.
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        dataBuilder.uiStatementOfTruth(StatementOfTruth.builder().build());
        dataBuilder.applicantSolicitor1ClaimStatementOfTruth(statementOfTruth);
        if (eventId != null) {
            var respondent1Represented = caseData.getSpecRespondent1Represented();
            dataBuilder.respondent1Represented(respondent1Represented);
            var respondent2Represented = caseData.getSpecRespondent2Represented();
            dataBuilder.respondent2Represented(respondent2Represented);
        }

        addOrgPolicy2ForSameLegalRepresentative(dataBuilder.build(), dataBuilder);

        boolean pinInPostCaseMatched = isPinInPostCaseMatched(caseData);
        log.info("Pin In Post Case Matched {} for caseId {}", pinInPostCaseMatched, caseData.getCcdCaseReference());
        if (pinInPostCaseMatched) {
            log.info("Pin In Post Matched for caseId {}", caseData.getCcdCaseReference());
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
        dataBuilder.caseNameHmctsInternal(buildCaseName(caseData));
        dataBuilder.caseNamePublic(buildCaseName(caseData));

        CaseManagementCategoryElement civil =
            CaseManagementCategoryElement.builder().code("Civil").label("Civil").build();
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        dataBuilder.caseManagementCategory(
            CaseManagementCategory.builder().value(civil).list_items(itemList).build());

        OrgPolicyUtils.addMissingOrgPolicies(dataBuilder);

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

        populateWithPartyIds(dataBuilder);

        if (featureToggleService.isCaseEventsEnabled()) {
            dataBuilder.anyRepresented(YES);
        }

        if (caseData.getSdtRequestIdFromSdt() != null) {
            // assign StdRequestId, to ensure duplicate requests from SDT/bulk claims are not processed
            List<Element<String>> stdRequestIdList = new ArrayList<>();
            stdRequestIdList.add(element(caseData.getSdtRequestIdFromSdt()));
            dataBuilder.sdtRequestId(stdRequestIdList);
            BigDecimal bulkInterest = interestCalculator.calculateBulkInterest(caseData);
            if (!bulkInterest.equals(BigDecimal.ZERO)) {
                dataBuilder.interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST);
            }
            dataBuilder.claimFee(feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount().add(bulkInterest)));
            //PBA manual selection
            List<String> pbaNumbers = getPbaAccounts(authorisationToken);
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

        if (featureToggleService.isSdoR2Enabled() && isFlightDelayClaim != null && isFlightDelayClaim.equals(YES)) {
            String selectedAirlineCode = flightDelayDetails.getAirlineList().getValue().getCode();
            dataBuilder.claimType(ClaimType.FLIGHT_DELAY)
                .flightDelayDetails(FlightDelayDetails.builder()
                                        .airlineList(DynamicList.builder().value(flightDelayDetails.getAirlineList().getValue()).build())
                                        .nameOfAirline(flightDelayDetails.getNameOfAirline())
                                        .flightNumber(flightDelayDetails.getFlightNumber())
                                        .scheduledDate(flightDelayDetails.getScheduledDate())
                                        .flightCourtLocation(getAirlineCaseLocation(selectedAirlineCode, authorisationToken))
                                        .build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder getSharedData(CaseData caseData, String authToken, String eventId) {
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = userService.getUserDetails(authToken);
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

        if (null != eventId) {
            dataBuilder.legacyCaseReference(specReferenceNumberRepository.getSpecReferenceNumber());
            dataBuilder.businessProcess(BusinessProcess.ready(CREATE_SERVICE_REQUEST_CLAIM));
        }

        //set check email field to null for GDPR
        dataBuilder.applicantSolicitor1CheckEmail(CorrectEmail.builder().build());
        return dataBuilder;
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

    private boolean isPinInPostCaseMatched(CaseData caseData) {
        log.info("Respondent1Represented =={}== AddRespondent2 =={}== AddApplicant2 =={}== and isPinInPostEnabled {} for caseId ={}=",
                 caseData.getRespondent1Represented(), caseData.getAddRespondent2(), caseData.getAddApplicant2(),
                 featureToggleService.isPinInPostEnabled(), caseData.getCcdCaseReference());
        return (caseData.getRespondent1Represented() == NO
            && caseData.getAddRespondent2() == NO
            && caseData.getAddApplicant2() == NO
            && featureToggleService.isPinInPostEnabled());
    }

    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
    }

    private CaseLocationCivil getAirlineCaseLocation(String airline, String authToken) {
        if (airline.equals("OTHER")) {
            return null;
        }
        String locationEpimmsId = airlineEpimsService.getEpimsIdForAirline(airline);
        List<LocationRefData> locations = fetchLocationData(authToken);
        var matchedLocations =  locations.stream().filter(loc -> loc.getEpimmsId().equals(locationEpimmsId)).toList();
        if (matchedLocations.isEmpty()) {
            throw new CallbackException(String.format(LOCATION_NOT_FOUND_MESSAGE, locationEpimmsId));
        } else {
            return CaseLocationCivil.builder()
                .region(matchedLocations.get(0).getRegionId())
                .baseLocation(matchedLocations.get(0).getEpimmsId()).build();
        }
    }

    private List<LocationRefData> fetchLocationData(String authToken) {
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
