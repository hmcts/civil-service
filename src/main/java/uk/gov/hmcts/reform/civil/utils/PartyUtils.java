package uk.gov.hmcts.reform.civil.utils;

import org.apache.commons.lang.StringUtils;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class PartyUtils {

    private PartyUtils() {
        //NO-OP
    }

    public static String getPartyNameBasedOnType(Party party, boolean omitTitle) {
        switch (party.getType()) {
            case COMPANY:
                return party.getCompanyName();
            case INDIVIDUAL:
                return getIndividualName(party, omitTitle);
            case SOLE_TRADER:
                return getSoleTraderName(party, omitTitle);
            case ORGANISATION:
                return party.getOrganisationName();
            default:
                throw new IllegalArgumentException("Invalid Party type in " + party);
        }
    }

    public static String getPartyNameBasedOnType(Party party) {
        return getPartyNameBasedOnType(party, false);
    }

    private static String getTitle(String title) {
        return StringUtils.isBlank(title) ? "" : title + " ";
    }

    public static Optional<LocalDate> getDateOfBirth(Party party) {
        switch (party.getType()) {
            case INDIVIDUAL:
                return ofNullable(party.getIndividualDateOfBirth());
            case SOLE_TRADER:
                return ofNullable(party.getSoleTraderDateOfBirth());
            case COMPANY, ORGANISATION:
            default:
                return Optional.empty();
        }
    }

    public static String getLitigiousPartyName(Party party, LitigationFriend litigationFriend) {
        switch (party.getType()) {
            case COMPANY:
                return party.getCompanyName();
            case ORGANISATION:
                return party.getOrganisationName();
            case INDIVIDUAL:
                return ofNullable(litigationFriend)
                    .map(lf -> getIndividualName(party) + " L/F " + lf.getFirstName() + " " + lf.getLastName())
                    .orElse(getIndividualName(party));
            case SOLE_TRADER:
                return ofNullable(party.getSoleTraderTradingAs())
                    .map(ta -> getSoleTraderName(party) + " T/A " + ta)
                    .orElse(getSoleTraderName(party));
            default:
                throw new IllegalArgumentException("Invalid Party type in " + party);
        }
    }

    private static String getSoleTraderName(Party party, boolean omitTitle) {
        return (omitTitle ? "" : getTitle(party.getSoleTraderTitle()))
            + party.getSoleTraderFirstName()
            + " "
            + party.getSoleTraderLastName();
    }

    private static String getSoleTraderName(Party party) {
        return getSoleTraderName(party, false);
    }

    private static String getIndividualName(Party party, boolean omitTitle) {
        if (party.getBulkClaimPartyName() != null) {
            return party.getBulkClaimPartyName();
        }
        return (omitTitle ? "" : getTitle(party.getIndividualTitle()))
                + party.getIndividualFirstName()
                + " "
                + party.getIndividualLastName();

    }

    private static String getIndividualName(Party party) {
        return getIndividualName(party, false);
    }

    public static String buildPartiesReferences(CaseData caseData) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasRespondent2Reference = defendantSolicitor2Reference.test(caseData);

        stringBuilder.append(buildClaimantReference(caseData));

        Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getRespondentSolicitor1Reference)
            .ifPresent(ref -> {
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(hasRespondent2Reference ? "Defendant 1 reference: " : "Defendant reference: ");
                stringBuilder.append(solicitorReferences.getRespondentSolicitor1Reference());
            });

        if (hasRespondent2Reference) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("Defendant 2 reference: ");
            stringBuilder.append(caseData.getRespondentSolicitor2Reference());
        }
        return stringBuilder.toString();
    }

    public static String buildClaimantReference(CaseData caseData) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();

        Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getApplicantSolicitor1Reference)
            .ifPresent(ref -> {
                stringBuilder.append("Claimant reference: ");
                stringBuilder.append(solicitorReferences.getApplicantSolicitor1Reference());
            });

        return stringBuilder.toString();
    }

    public static String addTrialOrHearing(CaseData caseData) {

        if (caseData.getAllocatedTrack() == AllocatedTrack.FAST_CLAIM) {
            return "trial";
        } else {
            return "hearing";
        }
    }

    public static String buildClaimantReferenceOnly(CaseData caseData) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();

        Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getApplicantSolicitor1Reference)
            .ifPresent(ref -> stringBuilder.append(solicitorReferences.getApplicantSolicitor1Reference()));

        return stringBuilder.toString();
    }

    public static String buildRespondentReference(CaseData caseData, boolean isRespondentSolicitorNumber2) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();

        if (!isRespondentSolicitorNumber2) {
            Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getRespondentSolicitor1Reference)
                .ifPresent(ref -> stringBuilder.append(solicitorReferences.getRespondentSolicitor1Reference()));
        }

        if (isRespondentSolicitorNumber2) {
            Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getRespondentSolicitor2Reference)
                .ifPresent(ref -> stringBuilder.append(solicitorReferences.getRespondentSolicitor2Reference()));
        }
        return stringBuilder.toString();
    }

    public static PartyData respondent1Data(CaseData caseData) {
        return new PartyData()
            .setRole(RESPONDENT_ONE)
            .setDetails(caseData.getRespondent1())
            .setTimeExtensionDate(caseData.getRespondent1TimeExtensionDate())
            .setSolicitorAgreedDeadlineExtension(caseData.getRespondentSolicitor1AgreedDeadlineExtension());
    }

    public static PartyData respondent2Data(CaseData caseData) {
        return new PartyData()
            .setRole(RESPONDENT_TWO)
            .setDetails(caseData.getRespondent2())
            .setTimeExtensionDate(caseData.getRespondent2TimeExtensionDate())
            .setSolicitorAgreedDeadlineExtension(caseData.getRespondentSolicitor2AgreedDeadlineExtension());
    }

    private static final Predicate<CaseData> defendantSolicitor2Reference = caseData -> caseData
        .getRespondentSolicitor2Reference() != null;

    public static RespondentResponseType getResponseTypeForRespondent(CaseData caseData, Party respondent) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (caseData.getRespondent1().equals(respondent)) {
                return Optional.ofNullable(caseData.getRespondent1ClaimResponseTypeForSpec())
                    .map(RespondentResponseTypeSpec::translate).orElse(null);
            } else {
                return Optional.ofNullable(caseData.getRespondent2ClaimResponseTypeForSpec())
                    .map(RespondentResponseTypeSpec::translate).orElse(null);
            }
        } else {
            if (caseData.getRespondent1().equals(respondent)) {
                return caseData.getRespondent1ClaimResponseType();
            } else {
                return caseData.getRespondent2ClaimResponseType();
            }
        }
    }

    public static RespondentResponseTypeSpec getResponseTypeForRespondentSpec(CaseData caseData, Party respondent) {
        if (caseData.getRespondent1().equals(respondent)) {
            return caseData.getRespondent1ClaimResponseTypeForSpec();
        } else {
            return caseData.getRespondent2ClaimResponseTypeForSpec();
        }
    }

    public static boolean isAcknowledgeUserRespondentTwo(CaseData caseData) {
        boolean isAcknowledgeUserRespondentTwo;
        final LocalDateTime resp1AcknowledgedDate =  caseData.getRespondent1AcknowledgeNotificationDate();
        final LocalDateTime resp2AcknowledgedDate =  caseData.getRespondent2AcknowledgeNotificationDate();
        if (resp1AcknowledgedDate == null &&  resp2AcknowledgedDate != null) {
            //case where respondent 2 acknowledges first
            isAcknowledgeUserRespondentTwo = true;
        } else if (resp1AcknowledgedDate != null && resp2AcknowledgedDate != null) {
            if (resp2AcknowledgedDate.isAfter(resp1AcknowledgedDate)) {
                //case where respondent 2 acknowledges 2nd
                isAcknowledgeUserRespondentTwo = true;
            } else {
                //case where respondent 1 acknowledges 2nd
                isAcknowledgeUserRespondentTwo = false;
            }
        } else {
            //case where respondent 1 acknowledges first
            isAcknowledgeUserRespondentTwo = false;
        }
        return isAcknowledgeUserRespondentTwo;
    }

    public static String getResponseIntentionForEmail(CaseData caseData) {
        StringBuilder responseIntentions = new StringBuilder();
        responseIntentions.append("The acknowledgement response selected: ");
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (isAcknowledgeUserRespondentTwo(caseData)) {
                    responseIntentions.append(caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                } else {
                    responseIntentions.append(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                responseIntentions.append("\nDefendant 1: ")
                    .append(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                responseIntentions.append("\n");
                responseIntentions.append("Defendant 2: ")
                    .append(caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                break;
            case TWO_V_ONE:
                responseIntentions.append("\nAgainst Claimant 1: ")
                    .append(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                responseIntentions.append("\n");
                responseIntentions.append("Against Claimant 2: ")
                    .append(caseData.getRespondent1ClaimResponseIntentionTypeApplicant2().getLabel());
                break;
            default:
                responseIntentions.append(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
        }

        return responseIntentions.toString();
    }

    static final String DEFENDANT_STRING = "\nDefendant : ";

    public static String fetchDefendantName(CaseData caseData) {
        StringBuilder defendantNames = new StringBuilder();
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if ((caseData.getRespondent1TimeExtensionDate() == null)
                    && (caseData.getRespondent2TimeExtensionDate() != null)) {
                    //case where respondent 2 extends first
                    defendantNames.append(DEFENDANT_STRING)
                        .append(caseData.getRespondent2().getPartyName());
                } else if ((caseData.getRespondent1TimeExtensionDate() != null)
                    && (caseData.getRespondent2TimeExtensionDate() != null)) {
                    if (caseData.getRespondent2TimeExtensionDate()
                        .isAfter(caseData.getRespondent1TimeExtensionDate())) {
                        //case where respondent 2 extends 2nd
                        defendantNames.append(DEFENDANT_STRING)
                            .append(caseData.getRespondent2().getPartyName());
                    } else {
                        //case where respondent 1 extends 2nd
                        defendantNames.append(DEFENDANT_STRING)
                            .append(caseData.getRespondent1().getPartyName());
                    }
                } else {
                    //case where respondent 1 extends first
                    defendantNames.append(DEFENDANT_STRING)
                        .append(caseData.getRespondent1().getPartyName());
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                defendantNames.append("\nDefendant 1: ")
                    .append(caseData.getRespondent1().getPartyName());
                defendantNames.append("\n");
                defendantNames.append("Defendant 2: ")
                    .append(caseData.getRespondent2().getPartyName());
                break;
            default:
                defendantNames.append(DEFENDANT_STRING)
                    .append(caseData.getRespondent1().getPartyName());
                break;
        }
        return defendantNames.toString();
    }

    public static String getAllPartyNames(CaseData caseData) {
        return format("%s%s V %s%s",
                      caseData.getApplicant1().getPartyName(),
                      YES.equals(caseData.getAddApplicant2())
                          ? ", " + caseData.getApplicant2().getPartyName() : "",
                      caseData.getRespondent1().getPartyName(),
                      YES.equals(caseData.getAddRespondent2())
                          && NO.equals(caseData.getRespondent2SameLegalRepresentative())
                          ? ", " + caseData.getRespondent2().getPartyName() : "");
    }

    public static String createPartyId() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    public static Party appendWithNewPartyId(Party party) {
        if (party != null && party.getPartyID() == null) {
            party.setPartyID(createPartyId());
        }
        return party;
    }

    public static LitigationFriend appendWithNewPartyId(LitigationFriend litigationFriend) {
        if (litigationFriend != null && litigationFriend.getPartyID() == null) {
            litigationFriend.setPartyID(createPartyId());
        }
        return litigationFriend;
    }

    public static PartyFlagStructure appendWithNewPartyId(PartyFlagStructure partyFlagStructure) {
        if (partyFlagStructure != null && partyFlagStructure.getPartyID() == null) {
            partyFlagStructure.setPartyID(createPartyId());
        }
        return partyFlagStructure;
    }

    public static List<Element<PartyFlagStructure>> appendWithNewPartyIds(List<Element<PartyFlagStructure>> partyFlagStructures) {
        return partyFlagStructures != null ? partyFlagStructures.stream().map(
            party -> Element.<PartyFlagStructure>builder()
                .id(party.getId()).value(appendWithNewPartyId(party.getValue())).build()
        ).toList() : null;
    }

    public static ExpertDetails appendWithNewPartyIds(ExpertDetails expert) {
        if (expert != null && expert.getPartyID() == null) {
            expert.setPartyID(createPartyId());
        }
        return expert;
    }

    public static Expert appendWithNewPartyIds(Expert expert) {
        if (expert != null && expert.getPartyID() == null) {
            expert.setPartyID(createPartyId());
        }
        return expert;
    }

    public static Experts appendWithNewPartyIds(Experts experts) {
        if (experts == null || experts.getDetails() == null) {
            return experts;
        }

        experts.getDetails().forEach(listElement -> appendWithNewPartyIds(listElement.getValue()));
        return experts;
    }

    public static Witness appendWithNewPartyIds(Witness witness) {
        if (witness != null && witness.getPartyID() == null) {
            witness.setPartyID(createPartyId());
        }
        return witness;
    }

    public static Witnesses appendWithNewPartyIds(Witnesses witnesses) {
        if (witnesses == null || witnesses.getDetails() == null) {
            return witnesses;
        }

        witnesses.getDetails().forEach(listElement -> appendWithNewPartyIds(listElement.getValue()));
        return witnesses;
    }

    public static Applicant1DQ appendWithNewPartyIds(Applicant1DQ applicant1DQ) {
        if (applicant1DQ == null) {
            return null;
        }
        appendWithNewPartyIds(applicant1DQ.getApplicant1DQExperts());
        appendWithNewPartyIds(applicant1DQ.getApplicant1RespondToClaimExperts());
        appendWithNewPartyIds(applicant1DQ.getApplicant1DQWitnesses());
        return applicant1DQ;
    }

    public static Applicant2DQ appendWithNewPartyIds(Applicant2DQ applicant2DQ) {
        if (applicant2DQ == null) {
            return null;
        }
        appendWithNewPartyIds(applicant2DQ.getApplicant2DQExperts());
        appendWithNewPartyIds(applicant2DQ.getApplicant2RespondToClaimExperts());
        appendWithNewPartyIds(applicant2DQ.getApplicant2DQWitnesses());
        return applicant2DQ;
    }

    public static Respondent1DQ appendWithNewPartyIds(Respondent1DQ respondent1DQ) {
        if (respondent1DQ == null) {
            return null;
        }
        appendWithNewPartyIds(respondent1DQ.getRespondent1DQExperts());
        appendWithNewPartyIds(respondent1DQ.getRespondToClaimExperts());
        appendWithNewPartyIds(respondent1DQ.getRespondent1DQWitnesses());
        return respondent1DQ;
    }

    public static Respondent2DQ appendWithNewPartyIds(Respondent2DQ respondent2DQ) {
        if (respondent2DQ == null) {
            return null;
        }
        appendWithNewPartyIds(respondent2DQ.getRespondent2DQExperts());
        appendWithNewPartyIds(respondent2DQ.getRespondToClaimExperts2());
        appendWithNewPartyIds(respondent2DQ.getRespondent2DQWitnesses());
        return respondent2DQ;
    }

    public static void populateDQPartyIds(CaseData caseData) {
        caseData.setApplicant1DQ(appendWithNewPartyIds(caseData.getApplicant1DQ()));
        caseData.setApplicant2DQ(appendWithNewPartyIds(caseData.getApplicant2DQ()));
        caseData.setRespondent1DQ(appendWithNewPartyIds(caseData.getRespondent1DQ()));
        caseData.setRespondent2DQ(appendWithNewPartyIds(caseData.getRespondent2DQ()));
    }

    @SuppressWarnings("unchecked")
    public static void populateWithPartyIds(CaseData caseData) {
        caseData.setApplicant1(appendWithNewPartyId(caseData.getApplicant1()));
        caseData.setApplicant2(appendWithNewPartyId(caseData.getApplicant2()));
        caseData.setRespondent1(appendWithNewPartyId(caseData.getRespondent1()));
        caseData.setRespondent2(appendWithNewPartyId(caseData.getRespondent2()));
        caseData.setApplicant1LitigationFriend(appendWithNewPartyId(caseData.getApplicant1LitigationFriend()));
        caseData.setApplicant2LitigationFriend(appendWithNewPartyId(caseData.getApplicant2LitigationFriend()));
        caseData.setRespondent1LitigationFriend(appendWithNewPartyId(caseData.getRespondent1LitigationFriend()));
        caseData.setRespondent2LitigationFriend(appendWithNewPartyId(caseData.getRespondent2LitigationFriend()));
    }

    public static void populateWitnessAndExpertsPartyIds(CaseData caseData) {
        caseData.setApplicantExperts(appendWithNewPartyIds(caseData.getApplicantExperts()));
        caseData.setRespondent1Experts(appendWithNewPartyIds(caseData.getRespondent1Experts()));
        caseData.setRespondent2Experts(appendWithNewPartyIds(caseData.getRespondent2Experts()));
        caseData.setApplicantWitnesses(appendWithNewPartyIds(caseData.getApplicantWitnesses()));
        caseData.setRespondent1Witnesses(appendWithNewPartyIds(caseData.getRespondent1Witnesses()));
        caseData.setRespondent2Witnesses(appendWithNewPartyIds(caseData.getRespondent2Witnesses()));
    }

    @SuppressWarnings("unchecked")
    public static void populatePartyIndividuals(CaseData caseData) {
        caseData.setApplicant1LRIndividuals(appendWithNewPartyIds(caseData.getApplicant1LRIndividuals()));
        caseData.setRespondent1LRIndividuals(appendWithNewPartyIds(caseData.getRespondent1LRIndividuals()));
        caseData.setRespondent2LRIndividuals(appendWithNewPartyIds(caseData.getRespondent2LRIndividuals()));
        caseData.setApplicant1OrgIndividuals(appendWithNewPartyIds(caseData.getApplicant1OrgIndividuals()));
        caseData.setApplicant2OrgIndividuals(appendWithNewPartyIds(caseData.getApplicant2OrgIndividuals()));
        caseData.setRespondent1OrgIndividuals(appendWithNewPartyIds(caseData.getRespondent1OrgIndividuals()));
        caseData.setRespondent2OrgIndividuals(appendWithNewPartyIds(caseData.getRespondent2OrgIndividuals()));
    }
}
