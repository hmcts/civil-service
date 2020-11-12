package uk.gov.hmcts.reform.unspec.utils;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.unspec.model.Party;

import java.time.LocalDate;
import java.util.Optional;

public class PartyUtils {

    private PartyUtils() {
        //NO-OP
    }

    public static String getPartyNameBasedOnType(Party party) {
        switch (party.getType()) {
            case COMPANY:
                return party.getCompanyName();
            case INDIVIDUAL:
                return getTitle(party.getIndividualTitle())
                    + party.getIndividualFirstName()
                    + " "
                    + party.getIndividualLastName();
            case SOLE_TRADER:
                return getTitle(party.getSoleTraderTitle())
                    + party.getSoleTraderFirstName()
                    + " "
                    + party.getSoleTraderLastName();
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
                return Optional.ofNullable(party.getIndividualDateOfBirth());
            case SOLE_TRADER:
                return Optional.ofNullable(party.getSoleTraderDateOfBirth());
            case COMPANY:
            case ORGANISATION:
            default:
                return Optional.empty();
        }
    }
}
