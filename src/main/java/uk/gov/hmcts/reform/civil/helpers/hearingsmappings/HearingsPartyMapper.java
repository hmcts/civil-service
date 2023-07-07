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
import java.util.stream.Collectors;

import static org.assertj.core.util.Lists.emptyList;
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
        return parties;
    }

    private static void addRespondent2Objects(CaseData caseData, OrganisationService organisationService, List<PartyDetailsModel> parties) {
        // respondent 2
        if (YES.equals(caseData.getAddRespondent2())) {
            parties.add(getDetailsForPartyObject(caseData.getRespondent2(), DEFENDANT_ROLE.getPartyRoleValue()));
            // respondent 2 solicitor
            if (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                && caseData.getRespondent2OrganisationPolicy().getOrganisation() != null) {
                parties.add(getDetailsForSolicitorOrganisation(
                    caseData.getRespondent2OrganisationPolicy(),
                    organisationService
                ));
            }
            // 1v2 Same sol and defs file different response
            // or 1v2 diff sol
            // only def 2 files full defence
            if (((ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                && NO.equals(caseData.getDefendantSingleResponseToBothClaimants()))
                || ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData)))
                && (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
                : FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()))) {
                // respondent 2 expert
                if (caseData.getRespondent2Experts() != null
                    && !caseData.getRespondent2Experts().isEmpty()) {
                    parties.addAll(getDetailsFor(EXPERT_ROLE, caseData.getRespondent2Experts()));
                }

                // respondent 2 witness
                if (caseData.getRespondent2Witnesses() != null
                    && !caseData.getRespondent2Witnesses().isEmpty()) {
                    parties.addAll(getDetailsFor(WITNESS_ROLE, caseData.getRespondent2Witnesses()));
                }
            }
            // respondent 2 lit friend
            if (caseData.getRespondent2LitigationFriend() != null) {
                parties.add(getDetailsForLitigationFriendObject(caseData.getRespondent2LitigationFriend()));
            }
        }
    }

    private static void addRespondent1Objects(CaseData caseData, OrganisationService organisationService, List<PartyDetailsModel> parties) {
        // respondent 1
        parties.add(getDetailsForPartyObject(caseData.getRespondent1(), DEFENDANT_ROLE.getPartyRoleValue()));
        // respondent 1 solicitor
        if (caseData.getRespondent1OrganisationPolicy().getOrganisation() != null) {
            parties.add(getDetailsForSolicitorOrganisation(
                caseData.getRespondent1OrganisationPolicy(),
                organisationService
            ));
        }
        // respondent 1 expert
        if (caseData.getRespondent1Experts() != null
            && !caseData.getRespondent1Experts().isEmpty()) {
            parties.addAll(getDetailsFor(EXPERT_ROLE, caseData.getRespondent1Experts()));
        }

        // respondent 1 witness
        if (caseData.getRespondent1Witnesses() != null
            && !caseData.getRespondent1Witnesses().isEmpty()) {
            parties.addAll(getDetailsFor(WITNESS_ROLE, caseData.getRespondent1Witnesses()));
        }

        // respondent 1 lit friend
        if (caseData.getRespondent1LitigationFriend() != null) {
            parties.add(getDetailsForLitigationFriendObject(caseData.getRespondent1LitigationFriend()));
        }
    }

    private static void addApplicant2Objects(CaseData caseData, List<PartyDetailsModel> parties) {
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            // applicant 2
            parties.add(getDetailsForPartyObject(caseData.getApplicant2(), CLAIMANT_ROLE.getPartyRoleValue()));
            // 2v1 claimant response - if different response and app 2 proceeds
            if (YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
                && NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())) {
                if (caseData.getApplicant2DQ() != null) {
                    // applicant 2 expert
                    if (caseData.getApplicantExperts() != null
                        && !caseData.getApplicantExperts().isEmpty()) {
                        parties.addAll(getDetailsFor(EXPERT_ROLE, caseData.getApplicantExperts()));
                    }
                    // applicant 2 witness
                    if (caseData.getApplicantWitnesses() != null
                        && !caseData.getApplicantWitnesses().isEmpty()) {
                        parties.addAll(getDetailsFor(WITNESS_ROLE, caseData.getApplicantWitnesses()));
                    }
                }
            }
            // applicant 2 lit friend
            if (caseData.getApplicant2LitigationFriend() != null) {
                parties.add(getDetailsForLitigationFriendObject(caseData.getApplicant2LitigationFriend()));
            }
        }
    }

    private static void addApplicant1Objects(CaseData caseData, OrganisationService organisationService, List<PartyDetailsModel> parties) {
        // applicant 1
        parties.add(getDetailsForPartyObject(caseData.getApplicant1(), CLAIMANT_ROLE.getPartyRoleValue()));
        // applicant 1 solicitor
        parties.add(getDetailsForSolicitorOrganisation(
            caseData.getApplicant1OrganisationPolicy(),
            organisationService
        ));

        if (caseData.getApplicant1DQ() != null) {
            // applicant 1 expert
            if (caseData.getApplicantExperts() != null
                && !caseData.getApplicantExperts().isEmpty()) {
                parties.addAll(getDetailsFor(EXPERT_ROLE, caseData.getApplicantExperts()));
            }

            // applicant 1 witness
            if (caseData.getApplicantWitnesses() != null
                && !caseData.getApplicantWitnesses().isEmpty()) {
                parties.addAll(getDetailsFor(WITNESS_ROLE, caseData.getApplicantWitnesses()));
            }
        }

        // applicant 1 lit friend
        if (caseData.getApplicant1LitigationFriend() != null) {
            parties.add(getDetailsForLitigationFriendObject(caseData.getApplicant1LitigationFriend()));
        }
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

    private static List<PartyDetailsModel> getDetailsFor(PartyRole partyRole, List<Element<PartyFlagStructure>> experts) {
        List<PartyDetailsModel> partyDetails = new ArrayList<>();
        List<PartyFlagStructure> filteredList = unwrapElements(experts);
        if (!filteredList.isEmpty()) {
            for (PartyFlagStructure partyFlagStructure : filteredList) {
                partyDetails.add(buildIndividualPartyObject(
                    partyFlagStructure.getPartyID(),
                    partyFlagStructure.getFirstName(),
                    partyFlagStructure.getLastName(),
                    String.format(FULL_NAME, partyFlagStructure.getFirstName(),
                                  partyFlagStructure.getLastName()),
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

    public static PartyDetailsModel buildIndividualPartyObject(String partyId, String firstName, String lastName,
                                                               String partyName, String partyRole,
                                                               String email, String phone,
                                                               Flags flags, List<Element<UnavailableDate>> unavailableDates) {

        List<FlagDetail> flagDetails = flags != null &&  flags.getDetails() != null
            ? flags.getDetails().stream().map(Element::getValue).collect(Collectors.toList()) : List.of();
        List<String> hearingChannelEmail = email == null ? emptyList() : List.of(email);
        List<String> hearingChannelPhone = phone == null ? emptyList() : List.of(phone);
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName(firstName)
            .lastName(lastName)
            .interpreterLanguage(getInterpreterLanguage(flagDetails))
            .reasonableAdjustments(getReasonableAdjustments(flagDetails))
            .vulnerableFlag(hasVulnerableFlag(flagDetails))
            .vulnerabilityDetails(getVulnerabilityDetails(flagDetails))
            .hearingChannelEmail(hearingChannelEmail)
            .hearingChannelPhone(hearingChannelPhone)
            .relatedParties(emptyList())
            .custodyStatus(getCustodyStatus(flagDetails))
            .build();

        return PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType(IND)
            .partyName(partyName)
            .partyRole(partyRole)
            .individualDetails(individualDetails)
            .unavailabilityDOW(null)
            .unavailabilityRanges(unavailableDates != null ? unwrapElements(unavailableDates).stream().map(date -> mapUnAvailableDateToRange(date)).collect(
                Collectors.toList()) : null)
            .hearingSubChannel(null)
            .build();
    }

    public static PartyDetailsModel buildOrganisationPartyObject(String partyId, String name,
                                                                 String partyRole,
                                                                 String cftOrganisationID,
                                                                 List<Element<UnavailableDate>> unavailableDates) {
        OrganisationDetailsModel organisationDetails = OrganisationDetailsModel.builder()
            .name(name)
            .organisationType(ORG.getLabel())
            .cftOrganisationID(cftOrganisationID)
            .build();

        return PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType(ORG)
            .partyName(name)
            .partyRole(partyRole)
            .organisationDetails(organisationDetails)
            .unavailabilityDOW(null)
            .unavailabilityRanges(unavailableDates != null ? unwrapElements(unavailableDates).stream().map(date -> mapUnAvailableDateToRange(date)).collect(
                Collectors.toList()) : null)
            .hearingSubChannel(null)
            .build();
    }

    private static UnavailabilityRangeModel mapUnAvailableDateToRange(UnavailableDate date) {
        return UnavailabilityRangeModel.builder()
            .unavailabilityType(ALL_DAY)
            .unavailableFromDate(SINGLE_DATE.equals(date.getUnavailableDateType()) ? date.getDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : date.getFromDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .unavailableToDate(SINGLE_DATE.equals(date.getUnavailableDateType()) ? date.getDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : date.getToDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .build();
    }
}
