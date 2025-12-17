package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
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
import java.util.Collections;
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
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferencesSpec;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;
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
        setSharedData(caseData, authorisationToken, eventId);

        // moving statement of truth value to correct field, this was not possible in mid event.
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
        caseData.setApplicantSolicitor1ClaimStatementOfTruth(statementOfTruth);
        if (eventId != null) {
            var respondent1Represented = caseData.getSpecRespondent1Represented();
            caseData.setRespondent1Represented(respondent1Represented);
            var respondent2Represented = caseData.getSpecRespondent2Represented();
            caseData.setRespondent2Represented(respondent2Represented);
        }

        addOrgPolicy2ForSameLegalRepresentative(caseData);

        boolean caseMatched = isCaseMatched(caseData);
        log.info("Post Case Matched {} for caseId {}", caseMatched, caseData.getCcdCaseReference());
        if (caseMatched) {
            log.info("Matched for caseId {}", caseData.getCcdCaseReference());
            caseData.setRespondent1PinToPostLRspec(defendantPinToPostLRspecService.buildDefendantPinToPost());
        }

        caseData.setCaseManagementLocation(new CaseLocationCivil().setRegion(regionId).setBaseLocation(epimmsId));
        Party respondent1 = new Party();
        BeanUtils.copyProperties(caseData.getRespondent1(), respondent1);
        caseData.setRespondent1DetailsForClaimDetailsTab(respondent1.setFlags(null));
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);

        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsByEpimmsId(authorisationToken, epimmsId));

        Optional.ofNullable(locations)
            .orElseGet(Collections::emptyList).stream().findFirst()
            .ifPresent(locationRefData -> caseData.setLocationName(locationRefData.getSiteName()));

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            Party respondent2 = new Party();
            BeanUtils.copyProperties(caseData.getRespondent2(), respondent2);
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2.setFlags(null));
        }

        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setFeatureToggleWA(toggleConfiguration.getFeatureToggle());

        //assign case management category to the case and caseNameHMCTSinternal
        caseData.setCaseNameHmctsInternal(buildCaseName(caseData));
        caseData.setCaseNamePublic(buildCaseName(caseData));

        CaseManagementCategoryElement civil =
            new CaseManagementCategoryElement().setCode("Civil").setLabel("Civil");
        List<Element<CaseManagementCategoryElement>> itemList = new ArrayList<>();
        itemList.add(element(civil));
        caseData.setCaseManagementCategory(
            new CaseManagementCategory().setValue(civil).setList_items(itemList));

        OrgPolicyUtils.addMissingOrgPolicies(caseData);

        caseFlagInitialiser.initialiseCaseFlags(CREATE_CLAIM_SPEC, caseData);

        defaultInterestClaimUntil(caseData);

        if (caseData.getRespondent1OrgRegistered() == YES
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2SameLegalRepresentative() == YES) {
            // Predicate: Def1 registered, Def 2 unregistered.
            // This is required to ensure mutual exclusion in 1v2 same solicitor case.
            caseData.setRespondent2OrgRegistered(YES);
            caseData.setRespondentSolicitor2EmailAddress(caseData.getRespondentSolicitor1EmailAddress());
            Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
            references.ifPresent(ref -> {
                SolicitorReferences updatedSolicitorReferences = SolicitorReferences.builder()
                    .applicantSolicitor1Reference(ref.getApplicantSolicitor1Reference())
                    .respondentSolicitor1Reference(ref.getRespondentSolicitor1Reference())
                    .respondentSolicitor2Reference(ref.getRespondentSolicitor1Reference())
                    .build();
                caseData.setSolicitorReferences(updatedSolicitorReferences);
            });
            caseData.setRespondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired());
            caseData.setRespondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
        } else if (caseData.getRespondent1OrgRegistered() == NO
            && caseData.getRespondent1Represented() == YES
            && caseData.getRespondent2SameLegalRepresentative() == YES) {
            caseData.setRespondent2OrgRegistered(NO);
            caseData.setRespondentSolicitor2EmailAddress(caseData.getRespondentSolicitor1EmailAddress());
            Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
            references.ifPresent(ref -> {
                SolicitorReferences updatedSolicitorReferences = SolicitorReferences.builder()
                    .applicantSolicitor1Reference(ref.getApplicantSolicitor1Reference())
                    .respondentSolicitor1Reference(ref.getRespondentSolicitor1Reference())
                    .respondentSolicitor2Reference(ref.getRespondentSolicitor1Reference())
                    .build();
                caseData.setSolicitorReferences(updatedSolicitorReferences);
            });
            caseData.setRespondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired());
            caseData.setRespondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
            caseData.setRespondentSolicitor2OrganisationDetails(caseData.getRespondentSolicitor1OrganisationDetails());
        }

        caseData.setAllPartyNames(getAllPartyNames(caseData));
        caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferencesSpec(caseData));

        populateWithPartyIds(caseData);

        caseData.setAnyRepresented(YES);

        if (caseData.getSdtRequestIdFromSdt() != null) {
            // assign StdRequestId, to ensure duplicate requests from SDT/bulk claims are not processed
            List<Element<String>> stdRequestIdList = new ArrayList<>();
            stdRequestIdList.add(element(caseData.getSdtRequestIdFromSdt()));
            caseData.setSdtRequestId(stdRequestIdList);
            BigDecimal bulkInterest = interestCalculator.calculateBulkInterest(caseData);
            if (!bulkInterest.equals(BigDecimal.ZERO)) {
                caseData.setInterestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST);
            }
            caseData.setClaimFee(feesService.getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount().add(bulkInterest)));
            //PBA manual selection
            List<String> pbaNumbers = getPbaAccounts(authorisationToken);
            caseData.setApplicantSolicitor1PbaAccounts(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label(pbaNumbers.get(0))
                    .build()).build());
        }

        List<String> errors = new ArrayList<>();
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP
            && caseData.getSpecRespondentCorrespondenceAddressdetails() != null) {
            // to keep with heading tab
            caseData.setSpecRespondent2CorrespondenceAddressRequired(caseData.getSpecRespondentCorrespondenceAddressRequired());
            caseData.setSpecRespondent2CorrespondenceAddressdetails(caseData.getSpecRespondentCorrespondenceAddressdetails());
        }

        if (isFlightDelayClaim != null && isFlightDelayClaim.equals(YES)) {
            String selectedAirlineCode = flightDelayDetails.getAirlineList().getValue().getCode();
            caseData.setClaimType(ClaimType.FLIGHT_DELAY);
            caseData.setFlightDelayDetails(FlightDelayDetails.builder()
                .airlineList(DynamicList.builder().value(flightDelayDetails.getAirlineList().getValue()).build())
                .nameOfAirline(flightDelayDetails.getNameOfAirline())
                .flightNumber(flightDelayDetails.getFlightNumber())
                .scheduledDate(flightDelayDetails.getScheduledDate())
                .flightCourtLocation(getAirlineCaseLocation(selectedAirlineCode, authorisationToken))
                .build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void defaultInterestClaimUntil(CaseData caseData) {
        if (caseData.getInterestClaimFrom() != null && caseData.getInterestClaimFrom().equals(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)) {
            caseData.setInterestClaimUntil(InterestClaimUntilType.UNTIL_SETTLED_OR_JUDGEMENT_MADE);
        }
    }

    private void setSharedData(CaseData caseData, String authToken, String eventId) {
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = userService.getUserDetails(authToken);
        IdamUserDetails idam = new IdamUserDetails().setId(userDetails.getId());
        CorrectEmail applicantSolicitor1CheckEmail = caseData.getApplicantSolicitor1CheckEmail();

        if (applicantSolicitor1CheckEmail != null && applicantSolicitor1CheckEmail.isCorrect()) {
            IdamUserDetails updatedIdam = new IdamUserDetails();
            BeanUtils.copyProperties(idam, updatedIdam);
            caseData.setApplicantSolicitor1UserDetails(updatedIdam.setEmail(applicantSolicitor1CheckEmail.getEmail()));
        } else {
            IdamUserDetails applicantSolicitor1UserDetails = caseData.getApplicantSolicitor1UserDetails();
            IdamUserDetails updatedIdam = new IdamUserDetails();
            BeanUtils.copyProperties(idam, updatedIdam);
            caseData.setApplicantSolicitor1UserDetails(updatedIdam.setEmail(applicantSolicitor1UserDetails.getEmail()));
        }

        caseData.setSubmittedDate(time.now());

        if (null != eventId) {
            caseData.setLegacyCaseReference(specReferenceNumberRepository.getSpecReferenceNumber());
            caseData.setBusinessProcess(BusinessProcess.ready(CREATE_SERVICE_REQUEST_CLAIM));
        }

        //set check email field to null for GDPR
        caseData.setApplicantSolicitor1CheckEmail(CorrectEmail.builder().build());
    }

    private void addOrgPolicy2ForSameLegalRepresentative(CaseData caseData) {
        if (caseData.getRespondent2SameLegalRepresentative() == YES) {
            OrganisationPolicy.OrganisationPolicyBuilder organisationPolicy2Builder = OrganisationPolicy.builder();

            OrganisationPolicy respondent1OrganisationPolicy = caseData.getRespondent1OrganisationPolicy();
            if (respondent1OrganisationPolicy != null) {
                organisationPolicy2Builder.organisation(respondent1OrganisationPolicy.getOrganisation())
                    .orgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference())
                    .build();
            }
            organisationPolicy2Builder.orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName());
            caseData.setRespondent2OrganisationPolicy(organisationPolicy2Builder.build());
        }
    }

    private boolean isCaseMatched(CaseData caseData) {
        log.info("Respondent1Represented =={}== AddRespondent2 =={}== AddApplicant2 =={}== for caseId ={}=",
            caseData.getRespondent1Represented(), caseData.getAddRespondent2(), caseData.getAddApplicant2(), caseData.getCcdCaseReference());
        return (caseData.getRespondent1Represented() == NO
            && caseData.getAddRespondent2() == NO
            && caseData.getAddApplicant2() == NO);
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
        var matchedLocations = locations.stream().filter(loc -> loc.getEpimmsId().equals(locationEpimmsId)).toList();
        if (matchedLocations.isEmpty()) {
            throw new CallbackException(String.format(LOCATION_NOT_FOUND_MESSAGE, locationEpimmsId));
        } else {
            return new CaseLocationCivil()
                .setRegion(matchedLocations.get(0).getRegionId())
                .setBaseLocation(matchedLocations.get(0).getEpimmsId());
        }
    }

    private List<LocationRefData> fetchLocationData(String authToken) {
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
