package uk.gov.hmcts.reform.unspec.utils;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.Party;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Optional.ofNullable;

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
}
