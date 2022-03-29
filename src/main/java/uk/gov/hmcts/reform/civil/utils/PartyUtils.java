package uk.gov.hmcts.reform.civil.utils;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;

public class PartyUtils {

    private PartyUtils() {
        //NO-OP
    }

    public static String getPartyNameBasedOnType(Party party) {
        switch (party.getType()) {
            case COMPANY:
                return party.getCompanyName();
            case INDIVIDUAL:
                return getIndividualName(party);
            case SOLE_TRADER:
                return getSoleTraderName(party);
            case ORGANISATION:
                return party.getOrganisationName();
            default:
                throw new IllegalArgumentException("Invalid Party type in " + party);
        }
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

    private static String getSoleTraderName(Party party) {
        return getTitle(party.getSoleTraderTitle())
            + party.getSoleTraderFirstName()
            + " "
            + party.getSoleTraderLastName();
    }

    private static String getIndividualName(Party party) {
        return getTitle(party.getIndividualTitle())
            + party.getIndividualFirstName()
            + " "
            + party.getIndividualLastName();
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
}
