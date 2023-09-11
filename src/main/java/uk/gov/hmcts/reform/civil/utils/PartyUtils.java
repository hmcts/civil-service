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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

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
            case COMPANY:
            case ORGANISATION:
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
                    .map(lf -> getIndividualName(party) + " L/F " + lf.getFullName())
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
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(hasRespondent2Reference ? "Defendant 1 reference: " : "Defendant reference: ");
                stringBuilder.append(solicitorReferences.getRespondentSolicitor1Reference());
            });

        if (hasRespondent2Reference) {
            if (stringBuilder.length() > 0) {
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
            .ifPresent(ref -> {
                stringBuilder.append(solicitorReferences.getApplicantSolicitor1Reference());
            });

        return stringBuilder.toString();
    }

    public static String buildRespondentReference(CaseData caseData, boolean isRespondentSolicitorNumber2) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        StringBuilder stringBuilder = new StringBuilder();

        if (!isRespondentSolicitorNumber2) {
            Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getRespondentSolicitor1Reference)
                .ifPresent(ref -> {
                    stringBuilder.append(solicitorReferences.getRespondentSolicitor1Reference());
                });
        }

        if (isRespondentSolicitorNumber2) {
            Optional.ofNullable(solicitorReferences).map(SolicitorReferences::getRespondentSolicitor2Reference)
                .ifPresent(ref -> {
                    stringBuilder.append(solicitorReferences.getRespondentSolicitor2Reference());
                });
        }
        return stringBuilder.toString();
    }

    public static PartyData respondent1Data(CaseData caseData) {
        return PartyData.builder()
            .role(RESPONDENT_ONE)
            .details(caseData.getRespondent1())
            .timeExtensionDate(caseData.getRespondent1TimeExtensionDate())
            .solicitorAgreedDeadlineExtension(caseData.getRespondentSolicitor1AgreedDeadlineExtension())
            .build();
    }

    public static PartyData respondent2Data(CaseData caseData) {
        return PartyData.builder()
            .role(RESPONDENT_TWO)
            .details(caseData.getRespondent2())
            .timeExtensionDate(caseData.getRespondent2TimeExtensionDate())
            .solicitorAgreedDeadlineExtension(caseData.getRespondentSolicitor2AgreedDeadlineExtension())
            .build();
    }

    private static Predicate<CaseData> defendantSolicitor2Reference = caseData -> caseData
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

    public static String getResponseIntentionForEmail(CaseData caseData) {
        StringBuilder responseIntentions = new StringBuilder();
        responseIntentions.append("The acknowledgement response selected: ");
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    //case where respondent 2 acknowledges first
                    responseIntentions.append(caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                        //case where respondent 2 acknowledges 2nd
                        responseIntentions.append(caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                    } else {
                        //case where respondent 1 acknowledges 2nd
                        responseIntentions.append(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                    }
                } else {
                    //case where respondent 1 acknowledges first
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

    public static String fetchDefendantName(CaseData caseData) {
        StringBuilder defendantNames = new StringBuilder();
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if ((caseData.getRespondent1TimeExtensionDate() == null)
                    && (caseData.getRespondent2TimeExtensionDate() != null)) {
                    //case where respondent 2 extends first
                    defendantNames.append("\nDefendant : ")
                        .append(caseData.getRespondent2().getPartyName());
                } else if ((caseData.getRespondent1TimeExtensionDate() != null)
                    && (caseData.getRespondent2TimeExtensionDate() != null)) {
                    if (caseData.getRespondent2TimeExtensionDate()
                        .isAfter(caseData.getRespondent1TimeExtensionDate())) {
                        //case where respondent 2 extends 2nd
                        defendantNames.append("\nDefendant : ")
                            .append(caseData.getRespondent2().getPartyName());
                    } else {
                        //case where respondent 1 extends 2nd
                        defendantNames.append("\nDefendant : ")
                            .append(caseData.getRespondent1().getPartyName());
                    }
                } else {
                    //case where respondent 1 extends first
                    defendantNames.append("\nDefendant : ")
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
                defendantNames.append("\nDefendant : ")
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

    private static String createPartyId() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    public static Party appendWithNewPartyId(Party party) {
        return party != null && party.getPartyID() == null
            ? party.toBuilder().partyID(createPartyId()).build() : party;
    }

    public static LitigationFriend appendWithNewPartyId(LitigationFriend litigationFriend) {
        return litigationFriend != null && litigationFriend.getPartyID() == null
            ? litigationFriend.toBuilder().partyID(createPartyId()).build() : litigationFriend;
    }

    public static PartyFlagStructure appendWithNewPartyId(PartyFlagStructure partyFlagStructure) {
        return partyFlagStructure != null && partyFlagStructure.getPartyID() == null
            ? partyFlagStructure.toBuilder().partyID(createPartyId()).build() : partyFlagStructure;
    }

    public static List<Element<PartyFlagStructure>> appendWithNewPartyIds(List<Element<PartyFlagStructure>> partyFlagStructures) {
        return partyFlagStructures != null ? partyFlagStructures.stream().map(
            party -> Element.<PartyFlagStructure>builder()
                .id(party.getId()).value(appendWithNewPartyId(party.getValue())).build()
        ).collect(Collectors.toList()) : null;
    }

    public static ExpertDetails appendWithNewPartyIds(ExpertDetails expert) {
        return expert != null && expert.getPartyID() == null ? expert.toBuilder().partyID(createPartyId()).build() : expert;
    }

    public static Expert appendWithNewPartyIds(Expert expert) {
        return expert != null && expert.getPartyID() == null ? expert.toBuilder().partyID(createPartyId()).build() : expert;
    }

    public static Experts appendWithNewPartyIds(Experts experts) {
        if (experts == null || experts.getDetails() == null) {
            return experts;
        }

        return experts.toBuilder().details(
            wrapElements(unwrapElements(
                experts.getDetails()).stream().map(
                    expert ->  appendWithNewPartyIds(expert)).collect(Collectors.toList()))).build();
    }

    public static Witness appendWithNewPartyIds(Witness witness) {
        return witness != null && witness.getPartyID() == null ? witness.toBuilder().partyID(createPartyId()).build() : witness;
    }

    public static Witnesses appendWithNewPartyIds(Witnesses witnesses) {
        if (witnesses == null || witnesses.getDetails() == null) {
            return witnesses;
        }

        return witnesses.toBuilder().details(
            wrapElements(unwrapElements(
                witnesses.getDetails()).stream().map(
                    witness ->  appendWithNewPartyIds(witness)).collect(Collectors.toList()))).build();
    }

    public static Applicant1DQ appendWithNewPartyIds(Applicant1DQ applicant1DQ) {
        return applicant1DQ != null ? applicant1DQ.toBuilder()
            .applicant1DQExperts(appendWithNewPartyIds(applicant1DQ.getApplicant1DQExperts()))
            .applicant1RespondToClaimExperts(appendWithNewPartyIds(applicant1DQ.getApplicant1RespondToClaimExperts()))
            .applicant1DQWitnesses(appendWithNewPartyIds(applicant1DQ.getApplicant1DQWitnesses()))
            .build() : null;
    }

    public static Applicant2DQ appendWithNewPartyIds(Applicant2DQ applicant2DQ) {
        return applicant2DQ != null ? applicant2DQ.toBuilder()
            .applicant2DQExperts(appendWithNewPartyIds(applicant2DQ.getApplicant2DQExperts()))
            .applicant2RespondToClaimExperts(appendWithNewPartyIds(applicant2DQ.getApplicant2RespondToClaimExperts()))
            .applicant2DQWitnesses(appendWithNewPartyIds(applicant2DQ.getApplicant2DQWitnesses()))
            .build() : null;
    }

    public static Respondent1DQ appendWithNewPartyIds(Respondent1DQ respondent1DQ) {
        return respondent1DQ != null ? respondent1DQ.toBuilder()
            .respondent1DQExperts(appendWithNewPartyIds(respondent1DQ.getRespondent1DQExperts()))
            .respondToClaimExperts(appendWithNewPartyIds(respondent1DQ.getRespondToClaimExperts()))
            .respondent1DQWitnesses(appendWithNewPartyIds(respondent1DQ.getRespondent1DQWitnesses()))
            .build() : null;
    }

    public static Respondent2DQ appendWithNewPartyIds(Respondent2DQ respondent2DQ) {
        return respondent2DQ != null ? respondent2DQ.toBuilder()
            .respondent2DQExperts(appendWithNewPartyIds(respondent2DQ.getRespondent2DQExperts()))
            .respondToClaimExperts2(appendWithNewPartyIds(respondent2DQ.getRespondToClaimExperts2()))
            .respondent2DQWitnesses(appendWithNewPartyIds(respondent2DQ.getRespondent2DQWitnesses()))
            .build() : null;
    }

    public static void populateDQPartyIds(CaseData.CaseDataBuilder builder) {
        CaseData caseData = builder.build();
        builder
            .applicant1DQ(appendWithNewPartyIds(caseData.getApplicant1DQ()))
            .applicant2DQ(appendWithNewPartyIds(caseData.getApplicant2DQ()))
            .respondent1DQ(appendWithNewPartyIds(caseData.getRespondent1DQ()))
            .respondent2DQ(appendWithNewPartyIds(caseData.getRespondent2DQ()));
    }

    @SuppressWarnings("unchecked")
    public static void populateWithPartyIds(CaseData.CaseDataBuilder builder) {
        CaseData caseData = builder.build();
        builder
            .applicant1(appendWithNewPartyId(caseData.getApplicant1()))
            .applicant2(appendWithNewPartyId(caseData.getApplicant2()))
            .respondent1(appendWithNewPartyId(caseData.getRespondent1()))
            .respondent2(appendWithNewPartyId(caseData.getRespondent2()))
            .applicant1LitigationFriend(appendWithNewPartyId(caseData.getApplicant1LitigationFriend()))
            .applicant2LitigationFriend(appendWithNewPartyId(caseData.getApplicant2LitigationFriend()))
            .respondent1LitigationFriend(appendWithNewPartyId(caseData.getRespondent1LitigationFriend()))
            .respondent2LitigationFriend(appendWithNewPartyId(caseData.getRespondent2LitigationFriend()));
    }
}
