package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.OrganisationDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.RelatedPartiesModel;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.util.Lists.emptyList;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.CLAIMANT_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.DEFENDANT_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.EXPERT_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.LEGAL_REP_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.LITIGATION_FRIEND_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyRole.WITNESS_ROLE;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.getCustodyStatus;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.hasLanguageMisinterpretationFlag;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.hasVulnerableFlag;

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
                && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType())) {
                // respondent 2 expert
                if (caseData.getRespondent2DQ().getExperts() != null
                    && caseData.getRespondent2DQ().getExperts().getDetails() != null) {
                    parties.addAll(getDetailsForExperts(caseData.getApplicantExperts()));
                }
                // respondent 2 witness
                if (caseData.getRespondent2DQ().getWitnesses() != null
                    && caseData.getRespondent2DQ().getWitnesses().getDetails() != null) {
                    parties.addAll(getDetailsForWitnesses(caseData.getApplicantWitnesses()));
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
        if (caseData.getRespondent1DQ() != null) {
            // respondent 1 expert
            if (caseData.getRespondent1DQ().getExperts() != null
                && caseData.getRespondent1DQ().getExperts().getDetails() != null) {
                parties.addAll(getDetailsForExperts(caseData.getApplicantExperts()));
            }
            // respondent 1 witness
            if (caseData.getRespondent1DQ().getWitnesses() != null
                && caseData.getRespondent1DQ().getWitnesses().getDetails() != null) {
                parties.addAll(getDetailsForWitnesses(caseData.getApplicantWitnesses()));
            }
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
                    // applicant 2 witness
                    if (caseData.getApplicant2DQ().getExperts() != null
                        && caseData.getApplicant2DQ().getExperts().getDetails() != null) {
                        parties.addAll(getDetailsForExperts(caseData.getApplicantExperts()));
                    }
                    // applicant 2 expert
                    if (caseData.getApplicant2DQ().getWitnesses() != null
                        && caseData.getApplicant2DQ().getWitnesses().getDetails() != null) {
                        parties.addAll(getDetailsForWitnesses(caseData.getApplicantWitnesses()));
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
            // applicant 1 witness
            if (caseData.getApplicant1DQ().getExperts() != null
                && caseData.getApplicant1DQ().getExperts().getDetails() != null) {
                parties.addAll(getDetailsForExperts(caseData.getApplicantWitnesses()));
            }
            // applicant 1 expert
            if (caseData.getApplicant1DQ().getWitnesses() != null
                && caseData.getApplicant1DQ().getWitnesses().getDetails() != null) {
                parties.addAll(getDetailsForWitnesses(caseData.getApplicantExperts()));
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

            return buildIndividualPartyObject(firstName,
                                       lastName,
                                       party.getPartyName(),
                                       partyRole,
                                       party.getPartyEmail(),
                                       party.getPartyPhone(),
                                       party.getFlags()
            );
        } else {
            return buildOrganisationPartyObject(party.getPartyName(), partyRole, null);
        }
    }

    private static PartyDetailsModel getDetailsForLitigationFriendObject(LitigationFriend litigationFriend) {
        return buildIndividualPartyObject(litigationFriend.getFirstName(),
                                          litigationFriend.getLastName(),
                                          String.format(FULL_NAME, litigationFriend.getFirstName(),
                                                        litigationFriend.getLastName()),
                                          LITIGATION_FRIEND_ROLE.getPartyRoleValue(),
                                          litigationFriend.getEmailAddress(),
                                          litigationFriend.getPhoneNumber(),
                                          litigationFriend.getFlags());
    }

    private static List<PartyDetailsModel> getDetailsForExperts(List<Element<PartyFlagStructure>> expertDetails) {
        ArrayList<PartyDetailsModel> expertPartyDetails = new ArrayList<>();
        for (Element<PartyFlagStructure> expert : expertDetails) {
            expertPartyDetails.add(buildIndividualPartyObject(
                expert.getValue().getFirstName(),
                expert.getValue().getLastName(),
                String.format(FULL_NAME, expert.getValue().getFirstName(), expert.getValue().getLastName()),
                EXPERT_ROLE.getPartyRoleValue(),
                null,
                null,
                expert.getValue().getFlags())
            );
        }
        return expertPartyDetails;
    }

    private static List<PartyDetailsModel> getDetailsForWitnesses(List<Element<PartyFlagStructure>> witnessDetails) {
        ArrayList<PartyDetailsModel> expertPartyDetails = new ArrayList<>();
        for (Element<PartyFlagStructure> witness : witnessDetails) {
            expertPartyDetails.add(buildIndividualPartyObject(
                witness.getValue().getFirstName(),
                witness.getValue().getLastName(),
                WITNESS_ROLE.getPartyRoleValue(),
                null,
                null,
                null,
                witness.getValue().getFlags())
            );
        }
        return expertPartyDetails;
    }

    private static PartyDetailsModel getDetailsForSolicitorOrganisation(OrganisationPolicy organisationPolicy,
                                                                        OrganisationService organisationService) {
        String organisationID = organisationPolicy.getOrganisation().getOrganisationID();
        String orgName = organisationService.findOrganisationById(organisationID)
            .map(Organisation::getName)
            .orElse("");
        return buildOrganisationPartyObject(orgName, LEGAL_REP_ROLE.getPartyRoleValue(), organisationID);
    }

    public static PartyDetailsModel buildIndividualPartyObject(String firstName, String lastName,
                                                               String partyName, String partyRole,
                                                               String email, String phone, Flags flags) {

        List<FlagDetail> flagDetails = flags != null &&  flags.getDetails() != null ?
            flags.getDetails().stream().map(Element::getValue).collect(Collectors.toList()) : List.of();

        List<String> hearingChannelEmail = email == null ? emptyList() : List.of(email);
        List<String> hearingChannelPhone = phone == null ? emptyList() : List.of(phone);
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName(firstName)
            .lastName(lastName)
            .interpreterLanguage(null) //todo civ-6888
            .reasonableAdjustments(null)//todo civ-6888
            .vulnerableFlag(false)//todo civ-6888
            .vulnerabilityDetails(null) //todo civ-6888
            .hearingChannelEmail(hearingChannelEmail)
            .hearingChannelPhone(hearingChannelPhone)
            .relatedParties(List.of(RelatedPartiesModel.builder().build()))
            .interpreterLanguage(hasLanguageMisinterpretationFlag(flagDetails))
            .reasonableAdjustments(null)
            .vulnerableFlag(hasVulnerableFlag(flagDetails))
            .custodyStatus(getCustodyStatus(flagDetails))
            .build();

        return PartyDetailsModel.builder()
            .partyID("") //todo civ-7690
            .partyType(IND)
            .partyName(partyName)
            .partyRole(partyRole)
            .individualDetails(individualDetails)
            .organisationDetails(null)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }

    public static PartyDetailsModel buildOrganisationPartyObject(String name,
                                                                 String partyRole,
                                                                 String cftOrganisationID) {
        OrganisationDetailsModel organisationDetails = OrganisationDetailsModel.builder()
            .name(name)
            .organisationType(null)
            .cftOrganisationID(cftOrganisationID)
            .build();

        return PartyDetailsModel.builder()
            .partyID("") //todo civ-7690
            .partyType(ORG)
            .partyName(name)
            .partyRole(partyRole)
            .individualDetails(null)
            .organisationDetails(organisationDetails)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }
}
