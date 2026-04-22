package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.hearing.PartyRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.OrganisationDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.UnavailabilityRangeModel;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.CLAIMANT_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.DEFENDANT_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.EXPERT_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.LEGAL_REP_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.LITIGATION_FRIEND_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.WITNESS_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.hearing.UnavailabilityType.ALL_DAY;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getOtherReasonableAdjustmentDetails;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getReasonableAdjustments;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getVulnerabilityDetails;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getCustodyStatus;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getInterpreterLanguage;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.hasVulnerableFlag;

public class HearingsPartyMapper {

    private static final String FULL_NAME = "%s %s";

    private HearingsPartyMapper() {
        //NO-OP
    }

    public static List<PartyDetailsModel> buildPartyObjectForHearingPayload(CaseData caseData, OrganisationService organisationService) {

        List<PartyDetailsModel> parties = new ArrayList<>();
        // applicant 1 and related parties
        addApplicant1Objects(caseData, organisationService, parties);
        // applicant 2 and related parties
        addApplicant2Objects(caseData, parties);
        // respondent 1 and related parties
        addRespondent1Objects(caseData, organisationService, parties);
        // respondent 2 and related parties
        addRespondent2Objects(caseData, organisationService, parties);

        addSolicitorOrgIndividuals(caseData, parties);

        return parties;
    }

    private static void addSolicitorOrgIndividuals(CaseData caseData, List<PartyDetailsModel> parties) {

        // applicant 1 solicitor firm individuals
        if (caseData.getApplicant1LRIndividuals() != null) {
            parties.addAll(getDetailsFor(LEGAL_REP_ROLE, caseData.getApplicant1LRIndividuals()));
        }

        // respondent 1 solicitor firm individuals
        if (caseData.getRespondent1LRIndividuals() != null) {
            parties.addAll(getDetailsFor(LEGAL_REP_ROLE, caseData.getRespondent1LRIndividuals()));
        }

        // respondent 2 solicitor firm individuals
        if (caseData.getRespondent2LRIndividuals() != null) {
            parties.addAll(getDetailsFor(LEGAL_REP_ROLE, caseData.getRespondent2LRIndividuals()));
        }
    }

    private static void addRespondent2Objects(CaseData caseData, OrganisationService organisationService, List<PartyDetailsModel> parties) {
        if (!YES.equals(caseData.getAddRespondent2())) {
            return;
        }

        parties.add(getDetailsForPartyObject(caseData.getRespondent2(), DEFENDANT_ROLE.getPartyRoleValue()));
        addOrganisationIndividuals(parties, DEFENDANT_ROLE, caseData.getRespondent2(), caseData.getRespondent2OrgIndividuals());
        addRespondent2Solicitor(caseData, organisationService, parties);
        if (shouldAddRespondent2ExpertsAndWitnesses(caseData)) {
            addExpertsAndWitnesses(parties, caseData.getRespondent2Experts(), caseData.getRespondent2Witnesses());
        }
        addLitigationFriend(parties, caseData.getRespondent2LitigationFriend());
    }

    private static void addRespondent1Objects(CaseData caseData, OrganisationService organisationService, List<PartyDetailsModel> parties) {
        parties.add(getDetailsForPartyObject(caseData.getRespondent1(), DEFENDANT_ROLE.getPartyRoleValue()));
        addOrganisationIndividuals(parties, DEFENDANT_ROLE, caseData.getRespondent1(), caseData.getRespondent1OrgIndividuals());
        addSolicitorOrganisation(parties, caseData.getRespondent1OrganisationPolicy(), organisationService);
        addExpertsAndWitnesses(parties, caseData.getRespondent1Experts(), caseData.getRespondent1Witnesses());
        addLitigationFriend(parties, caseData.getRespondent1LitigationFriend());
    }

    private static void addApplicant2Objects(CaseData caseData, List<PartyDetailsModel> parties) {
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            parties.add(getDetailsForPartyObject(caseData.getApplicant2(), CLAIMANT_ROLE.getPartyRoleValue()));
            addOrganisationIndividuals(parties, CLAIMANT_ROLE, caseData.getApplicant2(), caseData.getApplicant2OrgIndividuals());
            if (shouldAddApplicant2ExpertsAndWitnesses(caseData)) {
                addExpertsAndWitnesses(parties, caseData.getApplicantExperts(), caseData.getApplicantWitnesses());
            }
            addLitigationFriend(parties, caseData.getApplicant2LitigationFriend());
        }
    }

    private static void addApplicant1Objects(CaseData caseData, OrganisationService organisationService, List<PartyDetailsModel> parties) {
        parties.add(getDetailsForPartyObject(caseData.getApplicant1(), CLAIMANT_ROLE.getPartyRoleValue()));
        addOrganisationIndividuals(parties, CLAIMANT_ROLE, caseData.getApplicant1(), caseData.getApplicant1OrgIndividuals());
        addSolicitorOrganisation(parties, caseData.getApplicant1OrganisationPolicy(), organisationService);
        if (caseData.getApplicant1DQ() != null) {
            addExpertsAndWitnesses(parties, caseData.getApplicantExperts(), caseData.getApplicantWitnesses());
        }
        addLitigationFriend(parties, caseData.getApplicant1LitigationFriend());
    }

    private static PartyDetailsModel getDetailsForPartyObject(Party party, String partyRole) {
        if (INDIVIDUAL.equals(party.getType())
            || SOLE_TRADER.equals(party.getType())) {
            String firstName = party.getIndividualFirstName() == null
                ? party.getSoleTraderFirstName() : party.getIndividualFirstName();
            String lastName = party.getIndividualLastName() == null
                ? party.getSoleTraderLastName() : party.getIndividualLastName();

            return buildIndividualPartyObject(
                party.getPartyID(),
                firstName,
                lastName,
                party.getPartyName(),
                partyRole,
                party.getPartyEmail(),
                party.getPartyPhone(),
                party.getFlags(),
                party.getUnavailableDates()
            );
        } else {
            return buildOrganisationPartyObject(party.getPartyID(), party.getPartyName(),
                                                partyRole, null, party.getUnavailableDates());
        }
    }

    private static PartyDetailsModel getDetailsForLitigationFriendObject(LitigationFriend litigationFriend) {
        return buildIndividualPartyObject(litigationFriend.getPartyID(),
                                          litigationFriend.getFirstName(),
                                          litigationFriend.getLastName(),
                                          String.format(FULL_NAME, litigationFriend.getFirstName(),
                                                        litigationFriend.getLastName()),
                                          LITIGATION_FRIEND_ROLE.getPartyRoleValue(),
                                          litigationFriend.getEmailAddress(),
                                          litigationFriend.getPhoneNumber(),
                                          litigationFriend.getFlags(),
                                          null);
    }

    private static List<PartyDetailsModel> getDetailsFor(PartyRole partyRole,
                                                         List<Element<PartyFlagStructure>> hearingIndividuals) {
        List<PartyDetailsModel> partyDetails = new ArrayList<>();
        List<PartyFlagStructure> filteredList = unwrapElements(hearingIndividuals);

        if (!filteredList.isEmpty()) {
            for (PartyFlagStructure partyFlagStructure : filteredList) {
                partyDetails.add(buildIndividualPartyObject(
                    partyFlagStructure.getPartyID(),
                    partyFlagStructure.getFirstName(),
                    partyFlagStructure.getLastName(),
                    String.format(FULL_NAME, partyFlagStructure.getFirstName(), partyFlagStructure.getLastName()),
                    partyRole.getPartyRoleValue(),
                    partyFlagStructure.getEmail(),
                    partyFlagStructure.getPhone(),
                    partyFlagStructure.getFlags(),
                    null
                ));
            }
        }
        return partyDetails;
    }

    private static PartyDetailsModel getDetailsForSolicitorOrganisation(OrganisationPolicy organisationPolicy,
                                                                        OrganisationService organisationService) {
        String organisationID = organisationPolicy.getOrganisation().getOrganisationID();
        String orgName = organisationService.findOrganisationById(organisationID)
            .map(Organisation::getName)
            .orElse("");
        return buildOrganisationPartyObject(
            organisationID, orgName,
            LEGAL_REP_ROLE.getPartyRoleValue(), organisationID,
            null);
    }

    private static void addOrganisationIndividuals(List<PartyDetailsModel> parties,
                                                   PartyRole partyRole,
                                                   Party party,
                                                   List<Element<PartyFlagStructure>> hearingIndividuals) {
        if (party.isCompanyOROrganisation() && hearingIndividuals != null) {
            parties.addAll(getDetailsFor(partyRole, hearingIndividuals));
        }
    }

    private static void addSolicitorOrganisation(List<PartyDetailsModel> parties,
                                                 OrganisationPolicy organisationPolicy,
                                                 OrganisationService organisationService) {
        if (organisationPolicy.getOrganisation() != null) {
            parties.add(getDetailsForSolicitorOrganisation(organisationPolicy, organisationService));
        }
    }

    private static void addExpertsAndWitnesses(List<PartyDetailsModel> parties,
                                               List<Element<PartyFlagStructure>> experts,
                                               List<Element<PartyFlagStructure>> witnesses) {
        if (experts != null && !experts.isEmpty()) {
            parties.addAll(getDetailsFor(EXPERT_ROLE, experts));
        }
        if (witnesses != null && !witnesses.isEmpty()) {
            parties.addAll(getDetailsFor(WITNESS_ROLE, witnesses));
        }
    }

    private static void addLitigationFriend(List<PartyDetailsModel> parties, LitigationFriend litigationFriend) {
        if (litigationFriend != null) {
            parties.add(getDetailsForLitigationFriendObject(litigationFriend));
        }
    }

    private static boolean shouldAddApplicant2ExpertsAndWitnesses(CaseData caseData) {
        return YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
            && NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            && caseData.getApplicant2DQ() != null;
    }

    private static void addRespondent2Solicitor(CaseData caseData,
                                                OrganisationService organisationService,
                                                List<PartyDetailsModel> parties) {
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            addSolicitorOrganisation(parties, caseData.getRespondent2OrganisationPolicy(), organisationService);
        }
    }

    private static boolean shouldAddRespondent2ExpertsAndWitnesses(CaseData caseData) {
        return isRespondent2RepresentationEligible(caseData) && isRespondent2FullDefence(caseData);
    }

    private static boolean isRespondent2RepresentationEligible(CaseData caseData) {
        return (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && NO.equals(caseData.getDefendantSingleResponseToBothClaimants()))
            || ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData));
    }

    private static boolean isRespondent2FullDefence(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            : FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType());
    }

    @SuppressWarnings("java:S107")
    public static PartyDetailsModel buildIndividualPartyObject(String partyId, String firstName, String lastName,
                                                               String partyName, String partyRole,
                                                               String email, String phone,
                                                               Flags flags, List<Element<UnavailableDate>> unavailableDates) {

        List<FlagDetail> flagDetails = flags != null &&  flags.getDetails() != null
            ? flags.getDetails().stream().map(Element::getValue).toList() : List.of();
        List<String> hearingChannelEmail = email == null ? emptyList() : List.of(email);
        List<String> hearingChannelPhone = phone == null ? emptyList() : List.of(phone);
        IndividualDetailsModel individualDetails = new IndividualDetailsModel();
        individualDetails.setFirstName(firstName);
        individualDetails.setLastName(lastName);
        individualDetails.setInterpreterLanguage(getInterpreterLanguage(flagDetails));
        individualDetails.setReasonableAdjustments(getReasonableAdjustments(flagDetails));
        individualDetails.setVulnerableFlag(hasVulnerableFlag(flagDetails));
        individualDetails.setVulnerabilityDetails(getVulnerabilityDetails(flagDetails));
        individualDetails.setHearingChannelEmail(hearingChannelEmail);
        individualDetails.setHearingChannelPhone(hearingChannelPhone);
        individualDetails.setRelatedParties(emptyList());
        individualDetails.setCustodyStatus(getCustodyStatus(flagDetails));
        individualDetails.setOtherReasonableAdjustmentDetails(getOtherReasonableAdjustmentDetails(flagDetails));

        PartyDetailsModel partyDetails = buildBasePartyDetails(
            partyId,
            IND,
            partyName,
            partyRole,
            unavailableDates
        );
        partyDetails.setIndividualDetails(individualDetails);

        return partyDetails;
    }

    public static PartyDetailsModel buildOrganisationPartyObject(String partyId, String name,
                                                                 String partyRole,
                                                                 String cftOrganisationID,
                                                                 List<Element<UnavailableDate>> unavailableDates) {
        OrganisationDetailsModel organisationDetails = new OrganisationDetailsModel();
        organisationDetails.setName(name);
        organisationDetails.setOrganisationType(ORG.getLabel());
        organisationDetails.setCftOrganisationID(cftOrganisationID);

        PartyDetailsModel partyDetails = buildBasePartyDetails(
            partyId,
            ORG,
            name,
            partyRole,
            unavailableDates
        );
        partyDetails.setOrganisationDetails(organisationDetails);

        return partyDetails;
    }

    static final String DATE_STRING = "yyyy-MM-dd";

    private static PartyDetailsModel buildBasePartyDetails(String partyId,
                                                           uk.gov.hmcts.reform.civil.enums.hearing.PartyType partyType,
                                                           String partyName,
                                                           String partyRole,
                                                           List<Element<UnavailableDate>> unavailableDates) {
        PartyDetailsModel partyDetails = new PartyDetailsModel();
        partyDetails.setPartyID(partyId);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyName(partyName);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setUnavailabilityRanges(getUnavailabilityRanges(unavailableDates));
        return partyDetails;
    }

    private static List<UnavailabilityRangeModel> getUnavailabilityRanges(List<Element<UnavailableDate>> unavailableDates) {
        if (unavailableDates == null) {
            return emptyList();
        }
        return unwrapElements(unavailableDates).stream()
            .map(HearingsPartyMapper::mapUnAvailableDateToRange)
            .toList();
    }

    private static UnavailabilityRangeModel mapUnAvailableDateToRange(UnavailableDate date) {
        String unavailableFrom = SINGLE_DATE.equals(date.getUnavailableDateType()) ? date.getDate()
            .format(DateTimeFormatter.ofPattern(DATE_STRING)) : date.getFromDate()
            .format(DateTimeFormatter.ofPattern(DATE_STRING));
        String unavailableTo = SINGLE_DATE.equals(date.getUnavailableDateType()) ? date.getDate()
            .format(DateTimeFormatter.ofPattern(DATE_STRING)) : date.getToDate()
            .format(DateTimeFormatter.ofPattern(DATE_STRING));

        UnavailabilityRangeModel unavailabilityRangeModel = new UnavailabilityRangeModel();
        unavailabilityRangeModel.setUnavailabilityType(ALL_DAY);
        unavailabilityRangeModel.setUnavailableFromDate(unavailableFrom);
        unavailabilityRangeModel.setUnavailableToDate(unavailableTo);
        return unavailabilityRangeModel;
    }
}
